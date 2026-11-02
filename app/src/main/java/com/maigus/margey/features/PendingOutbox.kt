// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - messages différés : tout message privé non remis est renvoyé automatiquement
// dès que son destinataire réapparaît à portée, jusqu'à ce qu'il soit reçu.
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.features

import android.content.Context
import com.bitchat.android.mesh.MeshService
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.model.DeliveryStatus
import com.bitchat.android.services.AppStateStore
import com.bitchat.android.services.ContactDirectory
import com.bitchat.android.services.MessageRouter

object PendingOutbox {
    private const val RESEND_EVERY_MS = 10 * 60_000L
    private val lastTry = mutableMapOf<String, Long>()
    private val couriered = mutableSetOf<String>()
    private var known = emptySet<String>()

    /** Un message est en attente tant qu'il n'a pas été remis ou lu. */
    fun isPending(m: BitchatMessage, myNick: String): Boolean =
        m.sender == myNick && m.sender != "system" &&
            (m.deliveryStatus == null || m.deliveryStatus is DeliveryStatus.Sending || m.deliveryStatus is DeliveryStatus.Sent ||
                m.deliveryStatus is DeliveryStatus.Failed)

    /**
     * À appeler à chaque changement de la liste des téléphones à portée : renvoie les messages en attente
     * aux personnes qui viennent d'apparaître (et au plus toutes les 10 minutes aux autres).
     */
    fun onPeers(ctx: Context, mesh: MeshService, connected: List<String>, myNick: String) {
        val now = System.currentTimeMillis()
        val appeared = connected.toSet() - known
        known = connected.toSet()
        if (connected.isEmpty()) return
        val chats = AppStateStore.privateMessages.value
        for (peer in connected) {
            val conv = runCatching { ContactDirectory.canonicalConversationId(peer) }.getOrDefault(peer).lowercase()
            val keys = (runCatching { ContactDirectory.aliasesForConversation(peer) }.getOrDefault(emptySet()) + conv + peer)
                .map { it.lowercase() }.toSet()
            val pending = chats.entries.filter { it.key.lowercase() in keys }.flatMap { it.value }.filter { isPending(it, myNick) }
            for (m in pending) {
                val last = lastTry[m.id] ?: 0L
                if (peer in appeared || now - last > RESEND_EVERY_MS) {
                    lastTry[m.id] = now
                    runCatching {
                        MessageRouter.getInstance(ctx, mesh).sendPrivate(m.content, peer, m.recipientNickname ?: "", m.id)
                    }
                }
            }
        }
    }

    /**
     * Confie aux facteurs chaque message en attente dont le destinataire n'est pas à portée
     * (une seule fois par message, avec le même identifiant pour éviter les doublons).
     */
    fun courierize(connected: List<String>, myNick: String) {
        val here = connected.flatMap { p ->
            listOf(p.lowercase(), runCatching { ContactDirectory.canonicalConversationId(p) }.getOrDefault(p).lowercase())
        }.toSet()
        AppStateStore.privateMessages.value.forEach { (conv, list) ->
            if (conv.lowercase() in here) return@forEach
            list.filter { isPending(it, myNick) && it.id !in couriered }.forEach { m ->
                val ok = when (m.type) {
                    com.bitchat.android.model.BitchatMessageType.Image ->
                        runCatching { java.io.File(m.content).readBytes() }.getOrNull()?.let { Courier.send(conv, m.id, myNick, "image", data = it) } ?: false
                    com.bitchat.android.model.BitchatMessageType.Audio ->
                        runCatching { java.io.File(m.content).readBytes() }.getOrNull()?.let { Courier.send(conv, m.id, myNick, "audio", data = it) } ?: false
                    else -> Courier.send(conv, m.id, myNick, "text", text = m.content)
                }
                if (ok) couriered += m.id
            }
        }
    }
}
