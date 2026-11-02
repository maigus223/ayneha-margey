// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - les « facteurs » : un message privé (texte, photo, vocal) est fermé avec la clé du
// destinataire et confié à tous les téléphones, qui le transportent sans pouvoir le lire et le
// donnent à ceux qu'ils croisent, jusqu'à ce que le destinataire le reçoive (7 jours, 100 Mo).
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.features

import android.content.Context
import android.util.Base64
import com.bitchat.android.mesh.MeshService
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.model.BitchatMessageType
import com.bitchat.android.services.AppStateStore
import com.bitchat.android.services.ContactDirectory
import com.maigus.margey.tata.TataKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date

object Courier {
    const val KEEP_MS = 7L * 24 * 3600 * 1000            // 7 jours
    const val MAX_BYTES = 100L * 1024 * 1024             // 100 Mo par téléphone
    private const val CARD = "\u0002MGC"                 // carte : clés Margey d'un téléphone
    private const val INVENTORY = "\u0002MGI"            // liste des courriers qu'un téléphone porte
    private const val WANT = "\u0002MGW"                 // demande de courriers
    private const val ACK = "\u0002MGK"                  // accusé de réception (efface le courrier partout)

    private data class Stored(val id: String, val tag: String, val exp: Long, val ackHash: String, val size: Long)

    private var dir: File? = null
    private var mesh: MeshService? = null
    private var appCtx: Context? = null
    private val index = mutableMapOf<String, Stored>()
    private val acks = mutableMapOf<String, Long>()           // courriers déjà remis (id -> fin de validité)
    private val cards = mutableMapOf<String, Pair<String, String>>() // conversation -> (clé Ed25519, clé X25519)
    private val nicks = mutableMapOf<String, String>()
    private var lastCard = 0L
    private var lastInventory = 0L
    private val lastServed = mutableMapOf<String, Long>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val rnd = SecureRandom()

    fun init(ctx: Context) {
        if (dir != null) return
        appCtx = ctx.applicationContext
        dir = File(ctx.filesDir, "margey_courier").apply { mkdirs() }
        try {
            val f = File(dir, "index.json")
            if (f.exists()) {
                val o = JSONObject(f.readText())
                o.optJSONArray("items")?.let { a -> for (i in 0 until a.length()) a.getJSONObject(i).let { x ->
                    index[x.getString("i")] = Stored(x.getString("i"), x.getString("t"), x.getLong("e"), x.getString("h"), x.getLong("s"))
                } }
                o.optJSONObject("acks")?.let { a -> a.keys().forEach { acks[it] = a.getLong(it) } }
                o.optJSONObject("cards")?.let { a -> a.keys().forEach { k -> a.getJSONArray(k).let { cards[k] = it.getString(0) to it.getString(1) } } }
                o.optJSONObject("nicks")?.let { a -> a.keys().forEach { nicks[it] = a.getString(it) } }
            }
        } catch (_: Exception) { }
        cleanup()
    }

    fun attach(meshService: MeshService) { mesh = meshService }

    @Synchronized
    private fun saveIndex() {
        val d = dir ?: return
        try {
            val o = JSONObject()
            o.put("items", JSONArray().apply { index.values.forEach { put(JSONObject().apply { put("i", it.id); put("t", it.tag); put("e", it.exp); put("h", it.ackHash); put("s", it.size) }) } })
            o.put("acks", JSONObject().apply { acks.forEach { (k, v) -> put(k, v) } })
            o.put("cards", JSONObject().apply { cards.forEach { (k, v) -> put(k, JSONArray().put(v.first).put(v.second)) } })
            o.put("nicks", JSONObject().apply { nicks.forEach { (k, v) -> put(k, v) } })
            File(d, "index.json").writeText(o.toString())
        } catch (_: Exception) { }
    }

    private fun cleanup() {
        val now = System.currentTimeMillis()
        index.values.filter { it.exp < now || acks.containsKey(it.id) }.forEach { remove(it.id) }
        acks.entries.removeAll { it.value < now }
        // Place maximale : on retire les courriers les plus proches de leur fin
        var total = index.values.sumOf { it.size }
        index.values.sortedBy { it.exp }.forEach { s -> if (total > MAX_BYTES) { remove(s.id); total -= s.size } }
        saveIndex()
    }

    private fun remove(id: String) { index.remove(id); File(dir, "$id.bin").delete() }

    private fun sha(s: String) = MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    /** Code anonyme du destinataire, change chaque jour : les facteurs ne savent pas à qui est le courrier. */
    private fun tagFor(boxHex: String, day: Long) = sha("margey-tag|$boxHex|$day").take(16)
    private fun today() = System.currentTimeMillis() / 86_400_000L
    private fun myTags(): Set<String> = (0..7).map { tagFor(TataKeys.boxPublicHex, today() - it) }.toSet()

    // ---------- Cartes : relier un contact à ses clés Margey ----------

    /** Ce téléphone annonce ses clés aux téléphones proches (au plus toutes les 10 minutes). */
    fun announce(nick: String) {
        val now = System.currentTimeMillis()
        if (now - lastCard < 10 * 60_000L) return
        lastCard = now
        val o = JSONObject().apply { put("e", TataKeys.publicHex); put("x", TataKeys.boxPublicHex); put("n", nick) }
        mesh?.sendMessage(CARD + o.toString(), emptyList(), null)
    }

    fun keyFor(peerOrConversation: String): String? {
        val conv = runCatching { ContactDirectory.canonicalConversationId(peerOrConversation) }.getOrDefault(peerOrConversation).lowercase()
        return cards[conv]?.second
    }

    // ---------- Envoyer ----------

    /**
     * Confie un message privé aux facteurs. [type] : "text", "image" ou "audio" ; [data] : texte ou octets.
     * Renvoie false si la clé du destinataire n'est pas encore connue.
     */
    fun send(toPeer: String, messageId: String, myNick: String, type: String, text: String = "", data: ByteArray? = null): Boolean {
        val box = keyFor(toPeer) ?: return false
        val ackSecret = ByteArray(16).also { rnd.nextBytes(it) }.joinToString("") { "%02x".format(it) }
        val now = System.currentTimeMillis()
        val inner = JSONObject().apply {
            put("i", messageId); put("f", TataKeys.publicHex); put("n", myNick); put("ts", now); put("y", type); put("t", text)
            put("a", ackSecret)
            if (data != null) put("d", Base64.encodeToString(data, Base64.NO_WRAP))
        }
        inner.put("g", TataKeys.sign("courier1|$messageId|${TataKeys.publicHex}|$now|$type|$text"))
        val sealed = TataKeys.seal(box, inner.toString().toByteArray(Charsets.UTF_8))
        val header = JSONObject().apply { put("k", "c"); put("i", messageId); put("t", tagFor(box, today())); put("e", now + KEEP_MS); put("h", sha(ackSecret)) }
        store(header, sealed)
        Envelope.send(mesh, header, sealed)
        return true
    }

    private fun store(h: JSONObject, sealed: ByteArray) {
        val id = h.optString("i"); if (id.isBlank() || index.containsKey(id) || acks.containsKey(id)) return
        val exp = h.optLong("e"); if (exp < System.currentTimeMillis()) return
        try { File(dir, "$id.bin").writeBytes(sealed) } catch (_: Exception) { return }
        index[id] = Stored(id, h.optString("t"), exp, h.optString("h"), sealed.size.toLong())
        cleanup()
    }

    // ---------- Recevoir ----------

    /** Courrier reçu : pour moi, je l'ouvre ; sinon je le garde pour le transmettre. */
    fun onCourier(h: JSONObject, sealed: ByteArray) {
        val id = h.optString("i")
        if (acks.containsKey(id)) return
        if (h.optString("t") in myTags()) {
            val plain = TataKeys.open(sealed) ?: return
            deliver(JSONObject(String(plain, Charsets.UTF_8)))
            return
        }
        store(h, sealed)
    }

    private fun deliver(o: JSONObject) {
        val id = o.optString("i"); val from = o.optString("f"); val ts = o.optLong("ts")
        val type = o.optString("y"); val text = o.optString("t")
        if (!TataKeys.verify(from, "courier1|$id|$from|$ts|$type|$text", o.optString("g"))) return
        // Conversation de l'expéditeur, retrouvée par sa carte
        val conv = cards.entries.firstOrNull { it.value.first == from }?.key ?: "margey_$from".take(40)
        val nick = o.optString("n")
        val content = when (type) {
            "image", "audio" -> MediaFiles.save(id, type, Base64.decode(o.optString("d"), Base64.NO_WRAP)) ?: return
            else -> text
        }
        AppStateStore.addPrivateMessage(conv, BitchatMessage(
            id = id, sender = nick, content = content, timestamp = Date(ts), isPrivate = true, senderPeerID = conv,
            type = when (type) { "image" -> BitchatMessageType.Image; "audio" -> BitchatMessageType.Audio; else -> BitchatMessageType.Message }
        ))
        // Accusé : tous les facteurs effacent ce courrier
        val ack = JSONObject().apply { put("i", id); put("a", o.optString("a")) }
        mesh?.sendMessage(ACK + ack.toString(), emptyList(), null)
        acks[id] = System.currentTimeMillis() + KEEP_MS
        saveIndex()
    }

    /** Messages de contrôle des facteurs (cartes, listes, demandes, accusés). */
    fun onControl(raw: String, senderPeerID: String): Boolean {
        when {
            raw.startsWith(CARD) -> runCatching {
                val o = JSONObject(raw.removePrefix(CARD))
                val conv = ContactDirectory.canonicalConversationId(senderPeerID).lowercase()
                cards[conv] = o.getString("e") to o.getString("x"); nicks[conv] = o.optString("n")
                saveIndex()
            }
            raw.startsWith(ACK) -> runCatching {
                val o = JSONObject(raw.removePrefix(ACK)); val id = o.getString("i")
                val s = index[id]
                // Seul le vrai destinataire connaît le secret d'accusé
                if (s == null || sha(o.getString("a")) == s.ackHash) {
                    acks[id] = System.currentTimeMillis() + KEEP_MS; remove(id); saveIndex()
                    // Si c'est mon message : il est remis
                    runCatching { AppStateStore.updatePrivateMessageStatus(id, com.bitchat.android.model.DeliveryStatus.Delivered("", java.util.Date())) }
                }
            }
            raw.startsWith(INVENTORY) -> runCatching {
                val o = JSONObject(raw.removePrefix(INVENTORY))
                val mine = myTags()
                val want = JSONArray()
                val items = o.optJSONArray("c") ?: JSONArray()
                var free = MAX_BYTES - index.values.sumOf { it.size }
                for (i in 0 until items.length()) {
                    val x = items.getJSONObject(i); val id = x.getString("i")
                    if (index.containsKey(id) || acks.containsKey(id)) continue
                    val forMe = x.optString("t") in mine
                    if (forMe || (free > x.optLong("s") && want.length() < 20)) { want.put(id); free -= x.optLong("s") }
                }
                o.optJSONArray("a")?.let { a -> for (i in 0 until a.length()) { val id = a.getString(i); acks[id] = System.currentTimeMillis() + KEEP_MS; remove(id) } }
                if (want.length() > 0) mesh?.sendMessage(WANT + JSONObject().put("w", want).toString(), emptyList(), null)
                saveIndex()
            }
            raw.startsWith(WANT) -> runCatching {
                val w = JSONObject(raw.removePrefix(WANT)).getJSONArray("w")
                val now = System.currentTimeMillis()
                scope.launch {
                    for (i in 0 until w.length()) {
                        val id = w.getString(i); val s = index[id] ?: continue
                        if (now - (lastServed[id] ?: 0) < 60_000) continue
                        lastServed[id] = now
                        val bytes = runCatching { File(dir, "$id.bin").readBytes() }.getOrNull() ?: continue
                        Envelope.send(mesh, JSONObject().apply { put("k", "c"); put("i", s.id); put("t", s.tag); put("e", s.exp); put("h", s.ackHash) }, bytes)
                    }
                }
            }
            else -> return false
        }
        return true
    }

    /** Un nouveau téléphone est à portée : on annonce nos clés et la liste des courriers portés (au plus toutes les 5 minutes). */
    fun onPeersChanged(nick: String) {
        announce(nick)
        val now = System.currentTimeMillis()
        if (now - lastInventory < 5 * 60_000L) return
        lastInventory = now
        cleanup()
        val o = JSONObject()
        o.put("c", JSONArray().apply { index.values.sortedByDescending { it.exp }.take(200).forEach { put(JSONObject().apply { put("i", it.id); put("t", it.tag); put("s", it.size) }) } })
        o.put("a", JSONArray().apply { acks.keys.toList().takeLast(200).forEach { put(it) } })
        mesh?.sendMessage(INVENTORY + o.toString(), emptyList(), null)
    }

    fun wipe() {
        index.clear(); acks.clear(); cards.clear(); nicks.clear()
        dir?.listFiles()?.forEach { it.delete() }
    }
}
