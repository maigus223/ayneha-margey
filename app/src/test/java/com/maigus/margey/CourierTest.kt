package com.maigus.margey

import com.maigus.margey.tata.TataKeys
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class CourierTest {
    @Test fun onlyTheRecipientCanOpen() {
        TataKeys.initForTest(1); val recipientBox = TataKeys.boxPublicHex
        TataKeys.initForTest(2)
        val sealed = TataKeys.seal(recipientBox, "salam mamma".toByteArray())
        // Un facteur (autre téléphone) ne peut pas l'ouvrir
        assertNull(TataKeys.open(sealed))
        TataKeys.initForTest(3)
        assertNull(TataKeys.open(sealed))
        // Le destinataire l'ouvre
        TataKeys.initForTest(1)
        assertEquals("salam mamma", String(TataKeys.open(sealed)!!))
        // Un octet modifié en route : refusé
        val broken = sealed.copyOf().also { it[it.size - 1] = (it[it.size - 1] + 1).toByte() }
        assertNull(TataKeys.open(broken))
    }
}
