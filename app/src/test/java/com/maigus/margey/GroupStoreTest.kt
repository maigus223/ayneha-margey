package com.maigus.margey

import android.app.Application
import com.bitchat.android.mesh.MeshService
import com.maigus.margey.group.GroupStore
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.atLeastOnce
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class GroupStoreTest {
    @Test fun inviteSendReceive() {
        GroupStore.wipe()
        com.maigus.margey.tata.TataKeys.initForTest(1)
        val mesh = mock<MeshService>()
        // Téléphone A crée le groupe et envoie un message
        val g = GroupStore.create("gaawo", "fpA", "ture")
        GroupStore.attach(mesh)
        val qr = GroupStore.inviteQr(g, "ture")
        GroupStore.send(g, mesh, "fpA", "ture", "salam")
        val cap = argumentCaptor<String>()
        verify(mesh, atLeastOnce()).sendMessage(cap.capture(), any(), org.mockito.kotlin.anyOrNull())
        val payload = cap.lastValue
        assertFalse(payload.contains("salam")) // chiffré
        // Téléphone B (simulé) : rejoint par le QR puis reçoit le paquet
        GroupStore.wipe()
        com.maigus.margey.tata.TataKeys.initForTest(2)
        val inv = GroupStore.parseInvite(qr)!!
        assertEquals("gaawo", inv.name)
        assertTrue(GroupStore.join(inv, mesh, "fpB", "sise"))
        assertTrue(GroupStore.onPayload(payload, "P1"))
        val msgs = GroupStore.messages.value[g.id].orEmpty()
        assertEquals(1, msgs.size); assertEquals("salam", msgs[0].text); assertEquals("ture", msgs[0].sender)
        assertEquals(64, inv.inviterFp.length)
        // Un téléphone qui n'est pas membre absorbe le paquet sans pouvoir le lire
        GroupStore.wipe()
        assertTrue(GroupStore.onPayload(payload, "P2"))
        assertTrue(GroupStore.messages.value.isEmpty())
        // Message public ordinaire : non concerné
        assertFalse(GroupStore.onPayload("bonjour", "P3"))
    }
}
