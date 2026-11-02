// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - groupes privés : invitation par QR, chiffrement AES-256-GCM, liste des membres Tata-Set
// (100 maximum, 80 pour tous puis marge de l'administrateur), messages conservés sans limite de durée.
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.group

import android.content.Context
import android.util.Base64
import com.bitchat.android.mesh.MeshService
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class Group(
    val id: String,
    val name: String,
    val key: String,                       // clé AES-256 en Base64
    val ledger: Map<String, Member>,       // liste Tata-Set (faits signés)
    val createdAt: Long,
    val founder: String = "",              // code du créateur (connu par l'invitation)
) {
    /** Liste vue par l'interface (surplus calculé). */
    val view: Map<String, Member> get() = TataSet.view(ledger, GroupStore.MAX_MEMBERS)
    /** Membres actifs : code -> pseudo. */
    val members: Map<String, String> get() = view.values.filter { it.active }.associate { it.fp to it.nick }
    val adminFp: String? get() = TataSet.admin(view)
    val rules: TataSet.Rules get() = TataSet.Rules(id, founder.ifBlank { null }, false, GroupStore.OPEN_MEMBERS, GroupStore.MAX_MEMBERS)
}

/** Message de groupe. [info] = petit message d'information (entrée, départ...), [event] = son type. */
data class GroupMessage(
    val id: String,
    val groupId: String,
    val fingerprint: String,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val info: Boolean = false,
    /** "" = texte ; "image" ou "audio" : [text] contient alors le chemin du fichier */
    val media: String = "",
)

/** Invitation lue dans un QR. */
data class GroupInvite(val id: String, val name: String, val key: String, val count: Int, val inviterFp: String, val inviterNick: String,
    val founder: String = "", val ti: Long = 0, val tok: String = "")

object GroupStore {
    const val MAX_MEMBERS = 100      // plafond absolu
    const val OPEN_MEMBERS = 80      // au-delà, places réservées à l'administrateur (marge)
    private const val FILE = "margey_groups.json"
    private const val QR_PREFIX = "MARGEY-GROUPE:1:"
    private const val START = "\u0002MGG"
    private const val END = '\u0003'

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()
    private val _messages = MutableStateFlow<Map<String, List<GroupMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<GroupMessage>>> = _messages.asStateFlow()

    /** Textes des messages d'information, fournis par l'interface (dans la langue choisie). */
    var infoText: (TataEvent) -> String = { "" }

    private var file: File? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val rnd = SecureRandom()
    private var mesh: MeshService? = null
    private var myFp: String = ""
    private val lastLedgerSent = mutableMapOf<String, Long>()
    private val pendingLedger = mutableSetOf<String>()
    /** Groupes rejoints dont on attend encore la première liste complète (pas d'avis pour l'historique). */
    private val awaitingFirstLedger = mutableSetOf<String>()

    // ---------- Démarrage et enregistrement ----------

    fun init(context: Context) {
        if (file != null) return
        val f = File(context.filesDir, FILE)
        file = f
        try {
            if (f.exists()) {
                val root = JSONObject(f.readText())
                val gs = mutableListOf<Group>()
                val ga = root.optJSONArray("groups") ?: JSONArray()
                for (i in 0 until ga.length()) {
                    val o = ga.getJSONObject(i)
                    val created = o.getLong("created")
                    var ledger = TataSet.fromJson(o.optJSONArray("ledger"))
                    if (ledger.isEmpty()) {
                        // Ancien format : simple liste code -> pseudo
                        val mo = o.optJSONObject("members")
                        val m = mutableMapOf<String, Member>()
                        mo?.keys()?.forEach { k -> m[k] = Member(k, mo.getString(k), true, created, created, created, Reason.JOINED, "", created) }
                        ledger = m
                    }
                    gs += Group(o.getString("id"), o.getString("name"), o.getString("key"), ledger, created, o.optString("founder"))
                }
                val msgs = mutableMapOf<String, MutableList<GroupMessage>>()
                val ma = root.optJSONArray("messages") ?: JSONArray()
                for (i in 0 until ma.length()) {
                    val o = ma.getJSONObject(i)
                    val m = GroupMessage(o.getString("id"), o.getString("g"), o.getString("f"), o.getString("s"), o.getString("t"), o.getLong("ts"), o.optBoolean("i", false), o.optString("m"))
                    msgs.getOrPut(m.groupId) { mutableListOf() } += m
                }
                _groups.value = gs
                _messages.value = msgs
            }
        } catch (_: Exception) { }
        // Toutes les 30 minutes : règles Tata-Set (45 jours, surplus) et présence de ce téléphone
        scope.launch {
            while (isActive) {
                delay(30 * 60_000L)
                _groups.value.forEach { g -> checkOffline(g.id) }
            }
        }
    }

    /** À appeler quand le maillage est prêt : nécessaire pour envoyer les listes et les messages. */
    fun attach(meshService: MeshService, @Suppress("UNUSED_PARAMETER") myFingerprint: String = "") {
        mesh = meshService
        myFp = TataKeys.publicHex
    }

    /** Code Tata-Set de ce téléphone. */
    fun me(): String = TataKeys.publicHex.also { myFp = it }

    @Synchronized
    private fun save() {
        val f = file ?: return
        val root = JSONObject()
        root.put("groups", JSONArray().apply {
            _groups.value.forEach { g ->
                put(JSONObject().apply {
                    put("id", g.id); put("name", g.name); put("key", g.key); put("created", g.createdAt); put("founder", g.founder)
                    put("ledger", TataSet.toJson(g.ledger))
                })
            }
        })
        root.put("messages", JSONArray().apply {
            _messages.value.values.flatten().forEach { m ->
                put(JSONObject().apply {
                    put("id", m.id); put("g", m.groupId); put("f", m.fingerprint); put("s", m.sender)
                    put("t", m.text); put("ts", m.timestamp); put("i", m.info); put("m", m.media)
                })
            }
        })
        try {
            val tmp = File(f.parentFile, "$FILE.tmp")
            tmp.writeText(root.toString())
            tmp.renameTo(f)
        } catch (_: Exception) { }
    }

    private fun saveLater() = scope.launch { save() }

    // ---------- Chiffrement ----------

    private fun encrypt(keyB64: String, plain: String): String {
        val nonce = ByteArray(12).also { rnd.nextBytes(it) }
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, SecretKeySpec(Base64.decode(keyB64, Base64.NO_WRAP), "AES"), GCMParameterSpec(128, nonce))
        return Base64.encodeToString(nonce + c.doFinal(plain.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }

    private fun decrypt(keyB64: String, data: String): String? = try {
        val all = Base64.decode(data, Base64.NO_WRAP)
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, SecretKeySpec(Base64.decode(keyB64, Base64.NO_WRAP), "AES"), GCMParameterSpec(128, all, 0, 12))
        String(c.doFinal(all, 12, all.size - 12), Charsets.UTF_8)
    } catch (_: Exception) { null }

    // ---------- Liste des membres (Tata-Set) ----------

    fun group(id: String) = _groups.value.firstOrNull { it.id == id }

    /** Applique un changement local (déjà signé), affiche les avis et partage la liste. */
    private fun applyLocal(gid: String, change: (Map<String, Member>) -> Map<String, Member>) {
        val g = group(gid) ?: return
        val now = System.currentTimeMillis()
        var next = change(g.ledger)
        next[myFp]?.let { me -> next = next + (myFp to me.copy(seen = now)) }
        commit(g, next, notify = true)
        if (TataSet.hash(next) != TataSet.hash(g.ledger)) sendLedger(gid, force = true)
    }

    private fun commit(g: Group, next: Map<String, Member>, notify: Boolean) {
        val old = g.view
        val updated = g.copy(ledger = next)
        if (notify) TataSet.events(old, updated.view).forEach { addInfo(g.id, infoText(it)) }
        _groups.value = _groups.value.map { if (it.id == g.id) updated else it }
        saveLater()
    }

    /** L'administrateur (ou son suivant) constate les absences de plus de 45 jours, en les signant. */
    private fun checkOffline(gid: String) {
        val g = group(gid) ?: return
        val now = System.currentTimeMillis()
        val todo = TataSet.offlineToSign(g.ledger, MAX_MEMBERS, myFp, now)
        applyLocal(gid) { l ->
            var out = l
            todo.forEach { x -> out = out + (x.fp to TataSet.signed(g.id, x.copy(active = false, reason = Reason.OFFLINE, by = myFp, ver = TataSet.nextVer(x.ver, now), at = now))) }
            out
        }
    }

    private fun addInfo(gid: String, text: String) {
        if (text.isBlank()) return
        addMessage(GroupMessage(UUID.randomUUID().toString(), gid, "", "", text, System.currentTimeMillis(), info = true))
    }

    /** Envoie la liste complète (au plus une fois toutes les 20 secondes par groupe, sauf changement local). */
    private fun sendLedger(gid: String, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - (lastLedgerSent[gid] ?: 0L) < 20_000) {
            if (pendingLedger.add(gid)) scope.launch { delay(20_000); pendingLedger.remove(gid); sendLedger(gid) }
            return
        }
        val g = group(gid) ?: return
        lastLedgerSent[gid] = now
        sendRaw(g, JSONObject().apply { put("k", "L"); put("l", TataSet.toJson(g.ledger)); put("h", TataSet.hash(g.ledger)) })
    }

    // ---------- Actions ----------

    fun create(name: String, @Suppress("UNUSED_PARAMETER") myFingerprint: String, myNickname: String): Group {
        val fp = me()
        val now = System.currentTimeMillis()
        val id = ByteArray(8).also { rnd.nextBytes(it) }.joinToString("") { "%02x".format(it) }
        val g = Group(
            id = id,
            name = name,
            key = Base64.encodeToString(ByteArray(32).also { rnd.nextBytes(it) }, Base64.NO_WRAP),
            ledger = mapOf(fp to TataSet.signed(id, Member(fp, myNickname, true, now, now, now, Reason.JOINED, fp, now))),
            createdAt = now,
            founder = fp
        )
        _groups.value = _groups.value + g
        saveLater()
        return g
    }

    /** Ce téléphone peut-il inviter ? Tous jusqu'à 80 actifs, l'administrateur jusqu'à 100. */
    fun canInvite(g: Group): Boolean {
        val n = TataSet.activeCount(g.view)
        return n < OPEN_MEMBERS || (g.adminFp == me() && n < MAX_MEMBERS)
    }

    fun inviteQr(g: Group, myNickname: String): String {
        val ti = System.currentTimeMillis()
        val o = JSONObject().apply {
            put("g", g.id); put("n", g.name); put("k", g.key); put("c", TataSet.activeCount(g.view))
            put("f", me()); put("s", myNickname); put("o", g.founder)
            // Invitation signée par celui qui invite : elle prouve qui a fait entrer le nouveau membre
            put("ti", ti); put("tk", TataKeys.sign(TataSet.invitePayload(g.id, ti)))
        }
        return QR_PREFIX + Base64.encodeToString(o.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)
    }

    fun parseInvite(qr: String): GroupInvite? = try {
        if (!qr.startsWith(QR_PREFIX)) null else {
            val o = JSONObject(String(Base64.decode(qr.removePrefix(QR_PREFIX), Base64.NO_WRAP or Base64.URL_SAFE), Charsets.UTF_8))
            GroupInvite(o.getString("g"), o.getString("n"), o.getString("k"), o.optInt("c", 0), o.optString("f"), o.optString("s"),
                o.optString("o"), o.optLong("ti"), o.optString("tk"))
        }
    } catch (_: Exception) { null }

    /** Rejoindre un groupe à partir d'une invitation. Renvoie false si le groupe est complet. */
    fun join(inv: GroupInvite, meshService: MeshService, @Suppress("UNUSED_PARAMETER") myFingerprint: String, myNickname: String): Boolean {
        attach(meshService)
        if (inv.count >= MAX_MEMBERS) return false
        val fp = me()
        val now = maxOf(System.currentTimeMillis(), inv.ti)
        val existing = group(inv.id)
        val prev = existing?.ledger?.get(fp)
        if (prev?.active == true) return true
        val entry = TataSet.signed(inv.id, Member(fp, myNickname, true, TataSet.nextVer(prev?.ver ?: 0, now), now, now,
            Reason.JOINED, inv.inviterFp, now, inv.ti, inv.tok))
        if (existing != null) {
            applyLocal(inv.id) { it + (fp to entry) }
            return true
        }
        val g = Group(inv.id, inv.name, inv.key, mapOf(fp to entry), now, inv.founder)
        _groups.value = _groups.value + g
        awaitingFirstLedger += inv.id
        saveLater()
        sendLedger(inv.id, force = true)
        return true
    }

    fun send(g: Group, meshService: MeshService, @Suppress("UNUSED_PARAMETER") myFingerprint: String, myNickname: String, text: String) {
        attach(meshService)
        val myFingerprint = me()
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        addMessage(GroupMessage(id, g.id, myFingerprint, myNickname, text, now))
        sendRaw(g, JSONObject().apply {
            put("k", "m"); put("i", id); put("f", myFingerprint); put("s", myNickname); put("t", text); put("ts", now)
            put("h", TataSet.hash(g.ledger))
        })
    }

    /** Envoyer une photo ou un vocal dans le groupe (chiffré avec la clé du groupe). */
    fun sendMedia(g: Group, myNickname: String, type: String, path: String) {
        val bytes = runCatching { java.io.File(path).readBytes() }.getOrNull() ?: return
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val fp = me()
        addMessage(GroupMessage(id, g.id, fp, myNickname, path, now, media = type))
        val meta = JSONObject().apply { put("i", id); put("f", fp); put("s", myNickname); put("ts", now); put("y", type) }
        val plain = (meta.toString() + "\n").toByteArray(Charsets.UTF_8) + bytes
        val nonce = ByteArray(12).also { rnd.nextBytes(it) }
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, SecretKeySpec(Base64.decode(g.key, Base64.NO_WRAP), "AES"), GCMParameterSpec(128, nonce))
        com.maigus.margey.features.Envelope.send(mesh, JSONObject().apply { put("k", "gm"); put("g", g.id) }, nonce + c.doFinal(plain))
    }

    /** Photo ou vocal de groupe reçu (enveloppe chiffrée). */
    fun onMedia(h: JSONObject, body: ByteArray) {
        val g = group(h.optString("g")) ?: return
        val plain = try {
            val c = Cipher.getInstance("AES/GCM/NoPadding")
            c.init(Cipher.DECRYPT_MODE, SecretKeySpec(Base64.decode(g.key, Base64.NO_WRAP), "AES"), GCMParameterSpec(128, body, 0, 12))
            c.doFinal(body, 12, body.size - 12)
        } catch (_: Exception) { return }
        val (meta, bytes) = com.maigus.margey.features.Envelope.split(plain) ?: return
        val fp = meta.optString("f")
        val x = g.view[fp]
        if (x != null && !x.active) return
        val type = meta.optString("y")
        val id = meta.optString("i")
        val path = com.maigus.margey.features.MediaFiles.save(id, type, bytes) ?: return
        addMessage(GroupMessage(id, g.id, fp, meta.optString("s"), path, meta.optLong("ts"), media = type))
    }

    /** Effacer son propre message (chez soi et chez les autres). */
    fun deleteMine(g: Group, messageId: String) {
        val m = _messages.value[g.id]?.firstOrNull { it.id == messageId } ?: return
        if (m.fingerprint != me()) return
        removeMessage(g.id, messageId)
        // Ordre d'effacement signé : personne ne peut effacer le message d'un autre
        sendRaw(g, JSONObject().apply { put("k", "d"); put("i", messageId); put("f", myFp); put("g", TataKeys.sign("del|${g.id}|$messageId")) })
    }

    fun leave(g: Group, meshService: MeshService, @Suppress("UNUSED_PARAMETER") myFingerprint: String, myNickname: String) {
        attach(meshService)
        val fp = me()
        val now = System.currentTimeMillis()
        val cur = g.ledger[fp]
        val entry = TataSet.signed(g.id, Member(fp, myNickname, false, TataSet.nextVer(cur?.ver ?: 0, now), cur?.rank ?: now, now,
            Reason.LEFT, fp, now))
        _groups.value = _groups.value.map { if (it.id == g.id) it.copy(ledger = it.ledger + (fp to entry)) else it }
        sendLedger(g.id, force = true)
        _groups.value = _groups.value.filterNot { it.id == g.id }
        _messages.value = _messages.value - g.id
        saveLater()
    }

    /** Exclure un membre (administrateur seulement, changement signé). */
    fun exclude(g: Group, fp: String) {
        if (g.adminFp != me() || fp == myFp) return
        val now = System.currentTimeMillis()
        applyLocal(g.id) { l -> l[fp]?.let { x -> l + (fp to TataSet.signed(g.id, x.copy(active = false, reason = Reason.EXCLUDED, by = myFp, ver = TataSet.nextVer(x.ver, now), at = now, ti = 0, tok = ""))) } ?: l }
    }

    /** Réintégrer : l'administrateur pour tous ; un membre pour un exclu d'office tant qu'il reste moins de 80 actifs. */
    fun canReinstate(g: Group, x: Member): Boolean {
        if (x.active || x.reason == Reason.SURPLUS) return false
        val n = TataSet.activeCount(g.view)
        return if (g.adminFp == me()) n < MAX_MEMBERS else x.reason == Reason.OFFLINE && n < OPEN_MEMBERS && g.view[myFp]?.active == true
    }

    fun reinstate(g: Group, fp: String) {
        val x = g.ledger[fp] ?: return
        if (!canReinstate(g, x)) return
        val now = maxOf(System.currentTimeMillis(), x.at)
        // Un retour prend toujours un nouveau rang d'entrée ; le changement est signé par celui qui réintègre
        applyLocal(g.id) { l -> l + (fp to TataSet.signed(g.id, x.copy(active = true, reason = Reason.JOINED, by = myFp, rank = now, seen = now, ver = TataSet.nextVer(x.ver, now), at = now, ti = 0, tok = ""))) }
    }

    private fun sendRaw(g: Group, o: JSONObject) {
        val m = mesh ?: return
        m.sendMessage(START + g.id + END + encrypt(g.key, o.toString()), emptyList(), null)
    }

    private fun addMessage(m: GroupMessage) {
        val list = _messages.value[m.groupId].orEmpty()
        if (list.any { it.id == m.id }) return
        _messages.value = _messages.value + (m.groupId to (list + m).sortedBy { it.timestamp })
        saveLater()
    }

    private fun removeMessage(gid: String, id: String) {
        val list = _messages.value[gid] ?: return
        _messages.value = _messages.value + (gid to list.filterNot { it.id == id })
        saveLater()
    }

    /**
     * Appelé par le maillage pour chaque message public reçu. Renvoie true si c'est un message de
     * groupe (il ne doit alors pas apparaître ailleurs), même si ce téléphone n'est pas membre.
     */
    fun onPayload(raw: String, packetId: String): Boolean {
        if (!raw.startsWith(START)) return false
        val end = raw.indexOf(END, START.length)
        if (end <= START.length) return true
        val gid = raw.substring(START.length, end)
        val g = group(gid) ?: return true
        val plain = decrypt(g.key, raw.substring(end + 1)) ?: return true
        try {
            val o = JSONObject(plain)
            val fp = o.optString("f")
            val now = System.currentTimeMillis()
            if (myFp.isBlank()) myFp = TataKeys.publicHex
            // Présence : ce membre vient d'être vu
            g.ledger[fp]?.let { x -> if (x.seen < now) _groups.value = _groups.value.map { if (it.id == gid) it.copy(ledger = it.ledger + (fp to x.copy(seen = now))) else it } }
            when (o.optString("k")) {
                "m" -> {
                    val x = group(gid)?.view?.get(fp)
                    if (x != null && !x.active) return true // a quitté, a été exclu ou retiré
                    addMessage(GroupMessage(o.optString("i").ifBlank { packetId }, gid, fp, o.optString("s"), o.optString("t"), o.optLong("ts", now)))
                    if (o.optString("h") != TataSet.hash(group(gid)?.ledger ?: emptyMap())) sendLedger(gid)
                }
                "L" -> {
                    val cur = group(gid) ?: return true
                    val first = gid in awaitingFirstLedger
                    val next = TataSet.merge(cur.rules, cur.ledger, TataSet.fromJson(o.optJSONArray("l")))
                    commit(cur, next, notify = !first)
                    awaitingFirstLedger -= gid
                    if (TataSet.hash(next) != o.optString("h")) sendLedger(gid)
                }
                "d" -> {
                    val id = o.optString("i")
                    val m = _messages.value[gid]?.firstOrNull { it.id == id }
                    if (m != null && m.fingerprint == fp && TataKeys.verify(fp, "del|$gid|$id", o.optString("g"))) removeMessage(gid, id)
                }
            }
        } catch (_: Exception) { }
        return true
    }

    /** Effacement d'urgence. */
    fun wipe() {
        _groups.value = emptyList()
        _messages.value = emptyMap()
        try { file?.delete() } catch (_: Exception) { }
    }
}
