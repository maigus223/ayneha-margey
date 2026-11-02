// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - salons de proximité : étiquetage sur le maillage et conservation locale (72 h minimum)
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.salon

import android.content.Context
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.services.AppStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Date

/**
 * Le message public du maillage ne transporte que du texte. Pour qu'un message arrive dans le bon
 * salon chez les autres téléphones, on le préfixe par le nom du salon entre deux caractères de
 * contrôle invisibles.
 */
object SalonCodec {
    private const val START = "\u0002MGY#"
    private const val SEP = '\u0001'
    private const val END = '\u0003'

    /** Message de salon reçu : salon (avec « # »), identifiant, code de l'auteur, empreinte de sa liste, texte. */
    data class Decoded(val channel: String, val id: String?, val fp: String?, val hash: String?, val content: String)

    /** Ancien format (sans identifiant) : encore utilisé par le maillage d'origine. */
    fun encode(channel: String?, content: String): String =
        if (channel.isNullOrBlank() || channel.startsWith("geo:")) content
        else START + channel.removePrefix("#") + END + content

    /** Format Margey : identifiant commun (pour l'effacement), code de l'auteur et empreinte Tata-Set. */
    fun encode(channel: String, id: String, fp: String, hash: String, content: String): String =
        START + channel.removePrefix("#") + SEP + id + SEP + fp + SEP + hash + END + content

    fun decodeFull(payload: String): Decoded? {
        if (!payload.startsWith(START)) return null
        val end = payload.indexOf(END, START.length)
        if (end <= START.length || end - START.length > 400) return null
        val head = payload.substring(START.length, end).split(SEP)
        val content = payload.substring(end + 1)
        return Decoded("#" + head[0], head.getOrNull(1), head.getOrNull(2), head.getOrNull(3), content)
    }

    /** Renvoie (salon avec « # », texte) ou null si le message n'est pas un message de salon. */
    fun decode(payload: String): Pair<String, String>? = decodeFull(payload)?.let { it.channel to it.content }
}

/** Conserve les messages des salons sur le téléphone pendant la durée choisie (72 h minimum). */
object SalonStore {
    private const val FILE = "margey_salons.json"
    private const val PREF = "margey_salons"
    const val MIN_HOURS = 72          // 3 jours minimum
    const val MAX_HOURS = 90 * 24     // 90 jours maximum
    private const val MAX_PER_SALON = 1_000

    private var file: File? = null
    private var appContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun hours(ctx: Context): Int =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt("hours", MIN_HOURS).coerceIn(MIN_HOURS, MAX_HOURS)

    fun setHours(ctx: Context, h: Int) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putInt("hours", h.coerceIn(MIN_HOURS, MAX_HOURS)).apply()
        scope.launch { save(AppStateStore.channelMessages.value) }
    }

    /** Durée de conservation d'un salon : celle choisie par son créateur, sinon le réglage par défaut. */
    fun hoursFor(ctx: Context, channel: String): Int =
        SalonDirectory.days(channel)?.let { it * 24 } ?: hours(ctx)

    fun isAlive(timestampMs: Long, hours: Int): Boolean =
        System.currentTimeMillis() - timestampMs < hours * 3_600_000L

    @OptIn(FlowPreview::class)
    fun init(context: Context) {
        if (file != null) return
        appContext = context.applicationContext
        val f = File(context.filesDir, FILE)
        file = f
        // 1. Recharger ce qui est encore dans la durée de conservation
        try {
            if (f.exists()) {
                val arr = JSONArray(f.readText())
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val ts = o.getLong("ts")
                    val ch = o.getString("ch")
                    if (!isAlive(ts, hoursFor(context, ch))) continue
                    AppStateStore.addChannelMessage(
                        ch,
                        BitchatMessage(
                            id = o.getString("id"),
                            sender = o.getString("s"),
                            content = o.getString("c"),
                            timestamp = Date(ts),
                            senderPeerID = o.optString("p").ifBlank { null },
                            channel = ch,
                            type = runCatching { com.bitchat.android.model.BitchatMessageType.valueOf(o.optString("y", "Message")) }.getOrDefault(com.bitchat.android.model.BitchatMessageType.Message)
                        )
                    )
                }
            }
        } catch (_: Exception) { }
        // 2. Enregistrer à chaque changement
        scope.launch {
            AppStateStore.channelMessages.debounce(1_500).collect { save(it) }
        }
        // 3. Purger régulièrement
        scope.launch {
            while (isActive) {
                delay(30 * 60_000L)
                save(AppStateStore.channelMessages.value)
            }
        }
    }

    @Synchronized
    private fun save(byChannel: Map<String, List<BitchatMessage>>) {
        val f = file ?: return
        val ctx = appContext ?: return
        val arr = JSONArray()
        for ((ch, list) in byChannel) {
            if (ch.startsWith("geo:")) continue
            val h = hoursFor(ctx, ch)
            list.asSequence()
                .filter { it.type != com.bitchat.android.model.BitchatMessageType.File && isAlive(it.timestamp.time, h) }
                .toList().takeLast(MAX_PER_SALON)
                .forEach { m ->
                    arr.put(JSONObject().apply {
                        put("id", m.id); put("ch", ch); put("s", m.sender); put("c", m.content)
                        put("ts", m.timestamp.time); put("p", m.senderPeerID ?: ""); put("y", m.type.name)
                    })
                }
        }
        try {
            val tmp = File(f.parentFile, "$FILE.tmp")
            tmp.writeText(arr.toString())
            tmp.renameTo(f)
        } catch (_: Exception) { }
    }

    /** Effacement d'urgence. */
    fun wipe() {
        try { file?.delete() } catch (_: Exception) { }
    }
}
