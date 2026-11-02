package com.maigus.margey

import com.maigus.margey.salon.SalonDirectory
import com.maigus.margey.salon.SalonId
import com.maigus.margey.salon.SalonInfo
import com.maigus.margey.tata.TataKeys
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Salons : code unique, fiche signée, annonces aux téléphones à portée. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class SalonDirectoryTest {

    @Test fun idsAndDigits() {
        assertEquals("#marché de gao~0421", SalonId.make("  Marché   de Gao ", "0421"))
        assertEquals("0421", SalonId.code("#marché de gao~0421"))
        assertNull(SalonId.code("#ancien-salon"))
        assertEquals("ancien-salon", SalonId.rawName("#ancien-salon"))
        // Chiffres AYNEHA tapés -> chiffres latins
        val ay = String(Character.toChars(0x10FF7C + 4)) + String(Character.toChars(0x10FF7C + 2)) + "1"
        assertEquals("421", SalonId.digits(ay))
    }

    @Test fun sameNameGivesDifferentSalons() {
        SalonDirectory.wipe()
        TataKeys.initForTest(1)
        val a = SalonDirectory.create("Marché", 7, "Ali")
        val b = SalonDirectory.create("Marché", 30, "Ali")
        assertNotEquals(a.id, b.id)
        assertTrue(a.valid() && b.valid())
        assertEquals(7, SalonDirectory.days(a.id))
        assertEquals(30, SalonDirectory.days(b.id))
        assertEquals("Marché", SalonDirectory.name(a.id))
    }

    @Test fun forgedOrChangedInfoIsRejected() {
        SalonDirectory.wipe()
        TataKeys.initForTest(1)
        val a = SalonDirectory.create("Puits", 3, "Ali")
        // Durée changée par quelqu'un d'autre : signature invalide
        assertFalse(a.copy(days = 90).valid())
        // Fiche signée par un autre que le créateur annoncé
        TataKeys.initForTest(2)
        val forged = a.copy(sig = TataKeys.sign(a.signedText()))
        assertFalse(forged.valid())
        // Nom qui ne correspond pas au code interne
        assertFalse(a.copy(name = "Autre").valid())
    }

    @Test fun announcementsFillNearbyList() {
        SalonDirectory.wipe()
        TataKeys.initForTest(3)
        val s = SalonDirectory.create("Jardin", 30, "Moussa")
        SalonDirectory.wipe()
        val bad = s.copy(days = 90)
        val raw = "\u0002MGS" + JSONArray().put(s.toJson().put("n", 12)).put(bad.toJson().put("n", 99)).toString()
        assertTrue(SalonDirectory.onControl(raw))
        val near = SalonDirectory.nearby.value
        assertEquals(1, near.size)
        assertEquals(12, near[s.id]?.members)
        assertEquals("Moussa", near[s.id]?.info?.founderNick)
        assertFalse(SalonDirectory.onControl("bonjour"))
    }

    @Test fun oldestInfoWinsOnCollision() {
        SalonDirectory.wipe()
        TataKeys.initForTest(1)
        val first = SalonDirectory.create("Fleuve", 3, "A")
        TataKeys.initForTest(2)
        val unsigned = SalonInfo(first.id, "Fleuve", TataKeys.publicHex, "B", 90, first.createdAt + 1000, "")
        val later = unsigned.copy(sig = TataKeys.sign(unsigned.signedText()))
        assertTrue(later.valid())
        SalonDirectory.remember(later)
        assertEquals(3, SalonDirectory.days(first.id))
    }
}
