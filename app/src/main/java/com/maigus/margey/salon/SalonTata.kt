// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - liste des membres des salons (Tata-Set) : accès libre jusqu'à 500 actifs, marge de 10 %
// (plafond absolu 550) pour les entrées simultanées, administrateur = membre actif le plus ancien.
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.salon

import android.content.Context
import com.bitchat.android.mesh.MeshService
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.services.AppStateStore
import com.maigus.margey.tata.Member
import com.maigus.margey.tata.Reason
import com.maigus.margey.tata.TataEvent
import com.maigus.margey.tata.TataKeys
import com.maigus.margey.tata.TataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.Date
import java.util.UUID

object SalonTata {
    const val OPEN_MEMBERS = 500
    const val MAX_MEMBERS = 550
    /** Auteur des petits messages d'information (caractère invisible). */
    const val INFO_SENDER = "⁣"
    private const val FILE = "margey_salon_tata.json"
    private const val LEDGER = "\u0002MGL#"
    private const val DELETE = "\u0002MGD#"
    private const val SEP = '\u0001'
    private const val END = '\u0003'

    enum class JoinResult { OK, FULL, EXCLUDED }

    private val _ledgers = MutableStateFlow<Map<String, Map<String, Member>>>(emptyMap())
    val ledgers: StateFlow<Map<String, Map<String, Member>>> = _ledgers.asStateFlow()
    /** Auteur (code) de chaque message de salon, pour l'effacement par son auteur. */
    private val authors = mutableMapOf<String, String>()

    var infoText: (TataEvent) -> String = { "" }

    private var file: File? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mesh: MeshService? = null
    private var myFp = ""
    private var myPeerID = ""
    private val lastSent = mutableMapOf<String, Long>()
    private val pending = mutableSetOf<String>()
    /** Salons qu'on vient de rejoindre : pas d'avis pour les membres déjà présents. */
    private val awaitingFirst = mutableSetOf<String>()

    fun init(context: Context) {
        if (file != null) return
        val f = File(context.filesDir, FILE)
        file = f
        try {
            if (f.exists()) {
                val root = JSONObject(f.readText())
                val lo = root.optJSONObject("ledgers")
                val map = mutableMapOf<String, Map<String, Member>>()
                lo?.keys()?.forEach { ch -> map[ch] = TataSet.fromJson(lo.optJSONArray(ch)) }
                _ledgers.value = map
                root.optJSONObject("authors")?.let { a -> a.keys().forEach { authors[it] = a.getString(it) } }
            }
        } catch (_: Exception) { }
        scope.launch {
            while (isActive) {
                delay(30 * 60_000L)
                _ledgers.value.keys.forEach { ch -> checkOffline(ch) }
            }
        }
    }

    fun attach(meshService: MeshService, @Suppress("UNUSED_PARAMETER") fingerprint: String, peerID: String) {
        mesh = meshService; myFp = TataKeys.publicHex; myPeerID = peerID
    }

    private fun rules(channel: String) = TataSet.Rules(channel, null, true, OPEN_MEMBERS, MAX_MEMBERS)

    @Synchronized
    private fun save() {
        val f = file ?: return
        try {
            val root = JSONObject()
            root.put("ledgers", JSONObject().apply { _ledgers.value.forEach { (ch, l) -> put(ch, TataSet.toJson(l)) } })
            root.put("authors", JSONObject().apply { authors.entries.toList().takeLast(5000).forEach { (k, v) -> put(k, v) } })
            val tmp = File(f.parentFile, "$FILE.tmp")
            tmp.writeText(root.toString())
            tmp.renameTo(f)
        } catch (_: Exception) { }
    }

    private fun saveLater() = scope.launch { save() }

    fun ledger(channel: String): Map<String, Member> = _ledgers.value[channel].orEmpty()
    /** Liste vue par l'interface (surplus calculé). */
    fun view(channel: String): Map<String, Member> = TataSet.view(ledger(channel), MAX_MEMBERS)
    fun admin(channel: String): String? = TataSet.admin(view(channel))
    fun myFingerprint() = myFp

    private fun applyLocal(channel: String, change: (Map<String, Member>) -> Map<String, Member>) {
        val old = ledger(channel)
        val now = System.currentTimeMillis()
        var next = change(old)
        next[myFp]?.let { me -> next = next + (myFp to me.copy(seen = now)) }
        commit(channel, old, next, notify = true)
        if (TataSet.hash(next) != TataSet.hash(old)) sendLedger(channel, force = true)
    }

    private fun commit(channel: String, old: Map<String, Member>, next: Map<String, Member>, notify: Boolean) {
        if (notify) TataSet.events(TataSet.view(old, MAX_MEMBERS), TataSet.view(next, MAX_MEMBERS)).forEach { addInfo(channel, infoText(it)) }
        _ledgers.value = _ledgers.value + (channel to next)
        saveLater()
    }

    /** L'administrateur (ou son suivant) constate les absences de plus de 45 jours, en les signant. */
    private fun checkOffline(channel: String) {
        val now = System.currentTimeMillis()
        val todo = TataSet.offlineToSign(ledger(channel), MAX_MEMBERS, myFp, now)
        if (todo.isEmpty()) return
        applyLocal(channel) { l ->
            var out = l
            todo.forEach { x -> out = out + (x.fp to TataSet.signed(channel, x.copy(active = false, reason = Reason.OFFLINE, by = myFp, ver = TataSet.nextVer(x.ver, now), at = now))) }
            out
        }
    }

    private fun addInfo(channel: String, text: String) {
        if (text.isBlank()) return
        AppStateStore.addChannelMessage(channel, BitchatMessage(
            id = UUID.randomUUID().toString(), sender = INFO_SENDER, content = text, timestamp = Date(), channel = channel
        ))
    }

    private fun sendLedger(channel: String, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - (lastSent[channel] ?: 0L) < 60_000) {
            if (pending.add(channel)) scope.launch { delay(60_000); pending.remove(channel); sendLedger(channel) }
            return
        }
        val m = mesh ?: return
        lastSent[channel] = now
        val l = ledger(channel)
        m.sendMessage(LEDGER + channel.removePrefix("#") + SEP + TataSet.hash(l) + END + TataSet.toJson(l).toString(), emptyList(), null)
    }

    /** Entrer dans un salon (accès libre), sauf s'il est complet ou si l'administrateur nous a exclu. */
    fun join(channel: String, nick: String): JoinResult {
        if (myFp.isBlank()) myFp = TataKeys.publicHex
        val l = view(channel)
        val me = l[myFp]
        if (me?.active == true) return JoinResult.OK
        if (me?.reason == Reason.EXCLUDED) return JoinResult.EXCLUDED
        if (TataSet.activeCount(l) >= OPEN_MEMBERS) return JoinResult.FULL
        val now = maxOf(System.currentTimeMillis(), (me?.at ?: 0) + 1)
        if (l.keys.none { it != myFp }) awaitingFirst += channel
        applyLocal(channel) { it + (myFp to TataSet.signed(channel, Member(myFp, nick, true, TataSet.nextVer(me?.ver ?: 0, now), now, now, Reason.JOINED, myFp, now))) }
        return JoinResult.OK
    }

    fun leave(channel: String) {
        val me = ledger(channel)[myFp] ?: return
        val now = System.currentTimeMillis()
        applyLocal(channel) { it + (myFp to TataSet.signed(channel, me.copy(active = false, reason = Reason.LEFT, by = myFp, ver = TataSet.nextVer(me.ver, now), at = now))) }
    }

    fun exclude(channel: String, fp: String) {
        if (admin(channel) != myFp || fp == myFp) return
        val now = System.currentTimeMillis()
        applyLocal(channel) { l -> l[fp]?.let { x -> l + (fp to TataSet.signed(channel, x.copy(active = false, reason = Reason.EXCLUDED, by = myFp, ver = TataSet.nextVer(x.ver, now), at = now))) } ?: l }
    }

    /** Seul l'administrateur refait entrer un exclu ; les autres reviennent d'eux-mêmes. */
    fun canReinstate(channel: String, x: Member) =
        !x.active && x.reason == Reason.EXCLUDED && admin(channel) == myFp && TataSet.activeCount(view(channel)) < OPEN_MEMBERS

    fun reinstate(channel: String, fp: String) {
        val x = ledger(channel)[fp] ?: return
        if (!canReinstate(channel, x)) return
        val now = maxOf(System.currentTimeMillis(), x.at)
        applyLocal(channel) { l -> l + (fp to TataSet.signed(channel, x.copy(active = true, reason = Reason.JOINED, by = myFp, rank = now, seen = now, ver = TataSet.nextVer(x.ver, now), at = now))) }
    }

    /** Écrire dans un salon (avec un identifiant commun pour pouvoir l'effacer partout). */
    fun send(channel: String, nick: String, text: String) {
        val m = mesh ?: return
        val id = UUID.randomUUID().toString().uppercase()
        authors[id] = myFp
        AppStateStore.addChannelMessage(channel, BitchatMessage(
            id = id, sender = nick, content = text, timestamp = Date(), senderPeerID = myPeerID, channel = channel
        ))
        m.sendMessage(SalonCodec.encode(channel, id, myFp, TataSet.hash(ledger(channel)), text), emptyList(), null)
        saveLater()
    }

    /** Envoyer une photo ou un vocal dans le salon. */
    fun sendMedia(channel: String, nick: String, type: String, path: String) {
        val bytes = runCatching { java.io.File(path).readBytes() }.getOrNull() ?: return
        val id = UUID.randomUUID().toString().uppercase()
        authors[id] = myFp
        AppStateStore.addChannelMessage(channel, BitchatMessage(
            id = id, sender = nick, content = path, timestamp = Date(), senderPeerID = myPeerID, channel = channel,
            type = if (type == "image") com.bitchat.android.model.BitchatMessageType.Image else com.bitchat.android.model.BitchatMessageType.Audio
        ))
        val h = JSONObject().apply { put("k", "sm"); put("ch", channel); put("i", id); put("f", myFp); put("y", type); put("h", TataSet.hash(ledger(channel))) }
        com.maigus.margey.features.Envelope.send(mesh, h, bytes)
        saveLater()
    }

    /** Photo ou vocal de salon reçu. */
    fun onMedia(h: JSONObject, body: ByteArray, senderPeerID: String, senderNick: String) {
        val channel = h.optString("ch")
        val d = SalonCodec.Decoded(channel, h.optString("i"), h.optString("f"), h.optString("h"), "")
        if (!accept(d)) return
        val type = h.optString("y")
        val path = com.maigus.margey.features.MediaFiles.save(d.id ?: return, type, body) ?: return
        AppStateStore.addChannelMessage(channel, BitchatMessage(
            id = d.id, sender = senderNick, content = path, timestamp = Date(), senderPeerID = senderPeerID, channel = channel,
            type = if (type == "image") com.bitchat.android.model.BitchatMessageType.Image else com.bitchat.android.model.BitchatMessageType.Audio
        ))
    }

    fun isMine(id: String) = authors[id] == myFp

    fun deleteMine(channel: String, id: String) {
        if (!isMine(id)) return
        AppStateStore.removeChannelMessage(channel, id)
        // Ordre d'effacement signé : personne ne peut effacer le message d'un autre
        mesh?.sendMessage(DELETE + channel.removePrefix("#") + SEP + id + SEP + myFp + SEP + TataKeys.sign("del|$channel|$id") + END, emptyList(), null)
    }

    /** Complet pour [fp] : 500 actifs atteints et [fp] n'en fait pas partie. */
    fun isFull(channel: String, fp: String): Boolean {
        val l = view(channel)
        return l[fp]?.active != true && TataSet.activeCount(l) >= OPEN_MEMBERS
    }

    fun isExcluded(channel: String, fp: String) = view(channel)[fp]?.let { !it.active && it.reason == Reason.EXCLUDED } == true

    /** Messages de contrôle (liste des membres, effacement). Renvoie true s'il a été traité. */
    fun onControl(raw: String): Boolean {
        if (SalonDirectory.onControl(raw)) return true
        when {
            raw.startsWith(LEDGER) -> {
                val end = raw.indexOf(END)
                if (end < 0) return true
                val head = raw.substring(LEDGER.length, end).split(SEP)
                val channel = "#" + head[0]
                val incoming = try { TataSet.fromJson(org.json.JSONArray(raw.substring(end + 1))) } catch (_: Exception) { return true }
                val old = ledger(channel)
                val known = old.isNotEmpty()
                val next = TataSet.merge(rules(channel), old, incoming)
                commit(channel, old, next, notify = known && channel !in awaitingFirst)
                if (incoming.keys.any { it != myFp }) awaitingFirst -= channel
                if (known && TataSet.hash(next) != head.getOrNull(1)) sendLedger(channel)
                return true
            }
            raw.startsWith(DELETE) -> {
                val end = raw.indexOf(END)
                if (end < 0) return true
                val head = raw.substring(DELETE.length, end).split(SEP)
                val channel = "#" + head[0]
                val id = head.getOrNull(1) ?: return true
                val fp = head.getOrNull(2) ?: return true
                val sig = head.getOrNull(3) ?: return true
                if (authors[id] == fp && TataKeys.verify(fp, "del|$channel|$id", sig)) AppStateStore.removeChannelMessage(channel, id)
                return true
            }
        }
        return false
    }

    /**
     * Message de salon reçu. Renvoie false s'il doit être ignoré (auteur exclu, ou salon plein au
     * plafond absolu pour un inconnu). Met aussi à jour la présence et l'auteur du message.
     */
    fun accept(d: SalonCodec.Decoded): Boolean {
        val fp = d.fp ?: return true
        val l = ledger(d.channel)
        val x = view(d.channel)[fp]
        if (x != null && !x.active && x.reason == Reason.EXCLUDED) return false
        if (x == null && TataSet.activeCount(l) >= MAX_MEMBERS) return false
        d.id?.let { authors[it] = fp }
        val now = System.currentTimeMillis()
        if (x != null && x.seen < now) _ledgers.value = _ledgers.value + (d.channel to (l + (fp to x.copy(seen = now))))
        if (l.isNotEmpty() && d.hash != null && d.hash != TataSet.hash(l)) sendLedger(d.channel)
        saveLater()
        return true
    }

    fun wipe() {
        _ledgers.value = emptyMap()
        authors.clear()
        try { file?.delete() } catch (_: Exception) { }
    }
}
