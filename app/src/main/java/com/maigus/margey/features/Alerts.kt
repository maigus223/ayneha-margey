// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - alertes d'urgence : diffusées à tous les téléphones de la maille, horodatées, signées,
// et redonnées par chaque téléphone aux nouveaux venus pendant 72 heures.
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.features

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bitchat.android.mesh.MeshService
import com.maigus.margey.tata.TataKeys
import com.maigus.margey.text.Lang
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Alert(
    val id: String, val nick: String, val text: String, val ts: Long, val fp: String, val sig: String,
    val voiceHash: String = "",      // empreinte du message vocal (couverte par la signature)
    val voicePath: String = "",      // fichier du vocal sur ce téléphone
    val closedBy: String = "",       // pseudo de la personne qui a déclaré la prise en charge
    val closedAt: Long = 0,
    val closeFp: String = "", val closeSig: String = "",
) { val closed: Boolean get() = closedAt > 0 }

object Alerts {
    private const val PREFIX = "\u0002MGA"
    private const val CLOSE = "\u0002MGX"
    const val VOICE_MAX_MS = 60_000L
    private const val FILE = "margey_alerts.json"
    private const val KEEP_MS = 7L * 24 * 3600 * 1000      // conservées 7 jours
    private const val RELAY_MS = 72L * 3600 * 1000         // redonnées aux nouveaux venus pendant 72 h
    private const val CHANNEL = "margey_alerts"

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()
    private var file: File? = null
    private var appContext: Context? = null
    private var mesh: MeshService? = null
    private var lastRelay = 0L
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(ctx: Context) {
        if (file != null) return
        appContext = ctx.applicationContext
        val f = File(ctx.filesDir, FILE); file = f
        try {
            if (f.exists()) {
                val a = JSONArray(f.readText())
                _alerts.value = (0 until a.length()).mapNotNull { fromJson(a.getJSONObject(it)) }
                    .filter { System.currentTimeMillis() - it.ts < KEEP_MS }
            }
        } catch (_: Exception) { }
    }

    fun attach(meshService: MeshService) { mesh = meshService }

    private fun signPayload(a: Alert) = "alert1|${a.id}|${a.nick}|${a.text}|${a.ts}|${a.voiceHash}"
    private fun closePayload(id: String, nick: String, at: Long) = "close1|$id|$nick|$at"

    /** Format partagé (sans le chemin local du vocal). */
    private fun toJson(a: Alert) = JSONObject().apply {
        put("i", a.id); put("n", a.nick); put("t", a.text); put("ts", a.ts); put("f", a.fp); put("g", a.sig); put("vh", a.voiceHash)
    }

    /** Format enregistré sur le téléphone. */
    private fun toLocalJson(a: Alert) = toJson(a).apply {
        put("vp", a.voicePath); put("cb", a.closedBy); put("ca", a.closedAt); put("cf", a.closeFp); put("cg", a.closeSig)
    }

    private fun fromJson(o: JSONObject) = runCatching {
        Alert(o.getString("i"), o.getString("n"), o.getString("t"), o.getLong("ts"), o.optString("f"), o.optString("g"),
            o.optString("vh"), o.optString("vp"), o.optString("cb"), o.optLong("ca"), o.optString("cf"), o.optString("cg"))
    }.getOrNull()

    private fun sha(b: ByteArray) = java.security.MessageDigest.getInstance("SHA-256").digest(b).joinToString("") { "%02x".format(it) }

    private fun save() = scope.launch {
        try { file?.writeText(JSONArray().apply { _alerts.value.forEach { put(toLocalJson(it)) } }.toString()) } catch (_: Exception) { }
    }

    /** Envoyer une alerte à tous les téléphones (texte, vocal de 60 secondes au plus, ou les deux). */
    fun send(nick: String, text: String, voicePath: String? = null) {
        val voice = voicePath?.let { runCatching { File(it).readBytes() }.getOrNull() }
        val base = Alert(UUID.randomUUID().toString(), nick, text, System.currentTimeMillis(), TataKeys.publicHex, "",
            voiceHash = voice?.let { sha(it) } ?: "", voicePath = voicePath ?: "")
        val a = base.copy(sig = TataKeys.sign(signPayload(base)))
        add(a, notify = false)
        broadcast(a)
    }

    /** Diffuse l'alerte : le texte et l'heure d'abord, puis le vocal (plus lourd) ; ou sa fin de prise en charge. */
    private fun broadcast(a: Alert) {
        mesh?.sendMessage(PREFIX + toJson(a).toString(), emptyList(), null)
        if (a.closed) {
            sendClose(a)
        } else if (a.voicePath.isNotBlank()) {
            val bytes = runCatching { File(a.voicePath).readBytes() }.getOrNull() ?: return
            Envelope.send(mesh, JSONObject().apply { put("k", "av"); put("i", a.id) }, bytes)
        }
    }

    private fun sendClose(a: Alert) {
        val o = JSONObject().apply { put("i", a.id); put("n", a.closedBy); put("ts", a.closedAt); put("f", a.closeFp); put("g", a.closeSig) }
        mesh?.sendMessage(CLOSE + o.toString(), emptyList(), null)
    }

    /** Déclarer la prise en charge : l'auteur ou toute personne arrivée sur place, avec son pseudo. */
    fun close(id: String, nick: String) {
        val a = _alerts.value.firstOrNull { it.id == id } ?: return
        if (a.closed) return
        val at = System.currentTimeMillis()
        val closed = a.copy(closedBy = nick, closedAt = at, closeFp = TataKeys.publicHex, closeSig = TataKeys.sign(closePayload(id, nick, at)))
        replace(closed)
        sendClose(closed)
    }

    private fun replace(a: Alert) { _alerts.value = _alerts.value.map { if (it.id == a.id) a else it }; save() }

    /** Vocal d'une alerte reçu : gardé seulement s'il correspond à l'empreinte signée par l'auteur. */
    fun onVoice(h: JSONObject, body: ByteArray) {
        val a = _alerts.value.firstOrNull { it.id == h.optString("i") } ?: return
        if (a.voicePath.isNotBlank() || a.voiceHash != sha(body)) return
        val path = MediaFiles.save("alert_" + a.id, "audio", body) ?: return
        replace(a.copy(voicePath = path))
    }

    private fun add(a: Alert, notify: Boolean) {
        if (_alerts.value.any { it.id == a.id }) return
        _alerts.value = (_alerts.value + a).filter { System.currentTimeMillis() - it.ts < KEEP_MS }.sortedByDescending { it.ts }
        save()
        if (notify) notifyUser(a)
    }

    /** Message reçu du maillage. Renvoie true si c'était une alerte. */
    fun onPayload(raw: String): Boolean {
        if (raw.startsWith(CLOSE)) {
            runCatching {
                val o = JSONObject(raw.removePrefix(CLOSE))
                val id = o.getString("i"); val nick = o.getString("n"); val at = o.getLong("ts"); val fp = o.getString("f"); val sig = o.getString("g")
                val a = _alerts.value.firstOrNull { it.id == id }
                // Fin signée par la personne qui l'a déclarée (son pseudo est affiché)
                if (a != null && !a.closed && TataKeys.verify(fp, closePayload(id, nick, at), sig)) {
                    replace(a.copy(closedBy = nick, closedAt = at, closeFp = fp, closeSig = sig))
                }
            }
            return true
        }
        if (!raw.startsWith(PREFIX)) return false
        val a = runCatching { fromJson(JSONObject(raw.removePrefix(PREFIX))) }.getOrNull() ?: return true
        // Alerte signée par son auteur : personne ne peut en envoyer au nom d'un autre
        if (!TataKeys.verify(a.fp, signPayload(a), a.sig)) return true
        if (System.currentTimeMillis() - a.ts > KEEP_MS) return true
        add(a, notify = true)
        return true
    }

    /** Un nouveau téléphone est à portée : on lui redonne les alertes récentes (au plus toutes les 5 minutes). */
    fun onPeersChanged() {
        val now = System.currentTimeMillis()
        if (now - lastRelay < 5 * 60_000L) return
        lastRelay = now
        _alerts.value.filter { now - it.ts < RELAY_MS }.forEach { broadcast(it) }
    }

    fun dismiss(id: String) { _alerts.value = _alerts.value.filterNot { it.id == id }; save() }

    fun timeLabel(ts: Long): String = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT).format(Date(ts))

    private fun notifyUser(a: Alert) {
        val ctx = appContext ?: return
        try {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= 26 && nm.getNotificationChannel(CHANNEL) == null) {
                nm.createNotificationChannel(NotificationChannel(CHANNEL, "Margey - Alertes / Alerts", NotificationManager.IMPORTANCE_HIGH))
            }
            // Les notifications d'Android n'ont pas la police AYNEHA : titre en français ou en anglais
            val title = if (Lang.current == Lang.EN) "Margey - Alert from ${a.nick}" else "Margey - Alerte de ${a.nick}"
            val n = NotificationCompat.Builder(ctx, CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(title)
                .setContentText(timeLabel(a.ts) + " - " + a.text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(timeLabel(a.ts) + "\n" + a.text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            nm.notify(a.id.hashCode(), n)
        } catch (_: Exception) { }
    }

    fun wipe() { _alerts.value = emptyList(); try { file?.delete() } catch (_: Exception) { } }
}
