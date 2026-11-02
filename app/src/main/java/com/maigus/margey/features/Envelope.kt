// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - enveloppes de données (photos, vocaux, courriers) transportées par le maillage comme
// des fichiers : un en-tête JSON, un saut de ligne, puis les octets.
package com.maigus.margey.features

import com.bitchat.android.mesh.MeshService
import com.bitchat.android.model.BitchatFilePacket
import org.json.JSONObject

object Envelope {
    const val MIME = "application/x-margey"

    fun send(mesh: MeshService?, header: JSONObject, body: ByteArray) {
        val m = mesh ?: return
        val head = (header.toString() + "\n").toByteArray(Charsets.UTF_8)
        val bytes = head + body
        m.sendFileBroadcast(BitchatFilePacket("margey.bin", bytes.size.toLong(), MIME, bytes))
    }

    fun split(bytes: ByteArray): Pair<JSONObject, ByteArray>? {
        val nl = bytes.indexOf('\n'.code.toByte())
        if (nl <= 0) return null
        val header = runCatching { JSONObject(String(bytes, 0, nl, Charsets.UTF_8)) }.getOrNull() ?: return null
        return header to bytes.copyOfRange(nl + 1, bytes.size)
    }

    /** Fichier reçu du maillage : renvoie true s'il s'agissait d'une enveloppe Margey (traitée ici). */
    fun onFile(file: BitchatFilePacket, senderPeerID: String, senderNick: String): Boolean {
        if (file.mimeType != MIME) return false
        val (h, body) = split(file.content) ?: return true
        when (h.optString("k")) {
            "gm" -> com.maigus.margey.group.GroupStore.onMedia(h, body)
            "sm" -> com.maigus.margey.salon.SalonTata.onMedia(h, body, senderPeerID, senderNick)
            "av" -> Alerts.onVoice(h, body)
            "c" -> Courier.onCourier(h, body)
        }
        return true
    }
}

/** Fichiers des photos et vocaux reçus par Margey. */
object MediaFiles {
    private var dir: java.io.File? = null
    fun init(ctx: android.content.Context) { dir = java.io.File(ctx.filesDir, "margey_media").apply { mkdirs() } }
    fun save(id: String, type: String, bytes: ByteArray): String? = try {
        val ext = if (type == "image") "jpg" else "m4a"
        val f = java.io.File(dir ?: return null, id.filter { it.isLetterOrDigit() }.take(40) + "." + ext)
        f.writeBytes(bytes); f.absolutePath
    } catch (_: Exception) { null }
    fun wipe() { dir?.listFiles()?.forEach { it.delete() } }
}
