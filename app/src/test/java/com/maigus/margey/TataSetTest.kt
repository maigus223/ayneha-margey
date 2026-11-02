package com.maigus.margey

import com.maigus.margey.tata.Member
import com.maigus.margey.tata.Reason
import com.maigus.margey.tata.TataEvent
import com.maigus.margey.tata.TataKeys
import com.maigus.margey.tata.TataSet
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Trois téléphones simulés : A (créateur), B, C ; M est malveillant. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class TataSetTest {
    private val g = "groupe1"
    private fun key(seed: Int): String { TataKeys.initForTest(seed); return TataKeys.publicHex }
    private val A = key(1); private val B = key(2); private val C = key(3); private val M = key(4)
    private fun rules(founder: String = A) = TataSet.Rules(g, founder, false, 80, 100)

    /** Signe en tant que [who]. */
    private fun asKey(who: String, block: () -> Member): Member {
        TataKeys.initForTest(listOf(A, B, C, M).indexOf(who) + 1); return block()
    }
    private fun invite(inviter: String, ti: Long): String = asKey(inviter) { Member("", "", true, 0, 0, 0, 0, "", 0) }
        .let { TataKeys.sign(TataSet.invitePayload(g, ti)) }

    private fun founded(): Map<String, Member> =
        mapOf(A to asKey(A) { TataSet.signed(g, Member(A, "A", true, 10, 10, 10, Reason.JOINED, A, 10)) })

    private fun joined(who: String, inviter: String, t: Long): Member {
        val tok = invite(inviter, t)
        return asKey(who) { TataSet.signed(g, Member(who, who.take(4), true, t, t, t, Reason.JOINED, inviter, t, t, tok)) }
    }

    @Test fun invitedMembersAreAccepted() {
        var l = founded()
        l = TataSet.merge(rules(), l, mapOf(B to joined(B, A, 20)))
        l = TataSet.merge(rules(), l, mapOf(C to joined(C, B, 30)))
        assertEquals(3, TataSet.activeCount(l))
        assertEquals(A, TataSet.admin(l))
    }

    @Test fun noFakeInvitationAndNoBackdatedRank() {
        val l = TataSet.merge(rules(), founded(), mapOf(B to joined(B, A, 20)))
        // M se fabrique une invitation de A : signature fausse
        val fake = asKey(M) { TataSet.signed(g, Member(M, "M", true, 5, 5, 5, Reason.JOINED, A, 5, 5, "00".repeat(64))) }
        assertNull(TataSet.merge(rules(), l, mapOf(M to fake))[M])
        // M invité par B mais se donne un rang plus ancien que A pour devenir administrateur
        val tok = invite(B, 30)
        val early = asKey(M) { TataSet.signed(g, Member(M, "M", true, 1, 1, 1, Reason.JOINED, B, 1, 30, tok)) }
        assertNull(TataSet.merge(rules(), l, mapOf(M to early))[M])
    }

    @Test fun onlyTheAdminCanExclude() {
        var l = founded()
        l = TataSet.merge(rules(), l, mapOf(B to joined(B, A, 20), C to joined(C, A, 30)))
        // B (simple membre) signe l'exclusion de C : refusé
        val byB = asKey(B) { TataSet.signed(g, l[C]!!.copy(active = false, reason = Reason.EXCLUDED, by = B, ver = 40, at = 40)) }
        assertTrue(TataSet.merge(rules(), l, mapOf(C to byB))[C]!!.active)
        // B prétend que l'exclusion vient de A, mais ne peut pas signer avec la clé de A : refusé
        val forged = asKey(B) { TataSet.signed(g, l[C]!!.copy(active = false, reason = Reason.EXCLUDED, by = A, ver = 41, at = 41)) }
        assertTrue(TataSet.merge(rules(), l, mapOf(C to forged))[C]!!.active)
        // A (administrateur) exclut C : accepté
        val byA = asKey(A) { TataSet.signed(g, l[C]!!.copy(active = false, reason = Reason.EXCLUDED, by = A, ver = 42, at = 42)) }
        val after = TataSet.merge(rules(), l, mapOf(C to byA))
        assertFalse(after[C]!!.active)
        // C revient tout seul : refusé (seul l'administrateur réintègre)
        val back = asKey(C) { TataSet.signed(g, after[C]!!.copy(active = true, reason = Reason.JOINED, by = C, ver = 50, rank = 50, at = 50)) }
        assertFalse(TataSet.merge(rules(), after, mapOf(C to back))[C]!!.active)
    }

    @Test fun adminSuccessionAndReturningAdmin() {
        var l = founded()
        l = TataSet.merge(rules(), l, mapOf(B to joined(B, A, 20), C to joined(C, A, 30)))
        val old = l
        val left = asKey(A) { TataSet.signed(g, l[A]!!.copy(active = false, reason = Reason.LEFT, by = A, ver = 60, at = 60)) }
        l = TataSet.merge(rules(), l, mapOf(A to left))
        assertEquals(B, TataSet.admin(l))
        assertTrue(TataSet.events(old, l).any { it is TataEvent.NewAdmin && it.member.fp == B })
    }

    @Test fun surplusLatestEntriesAreRemoved() {
        val l = (1..103).associate { val k = "%064x".format(it); k to Member(k, "m$it", true, it.toLong(), it.toLong(), 0, Reason.JOINED, k, 0) }
        val v = TataSet.view(l, 100)
        assertEquals(100, TataSet.activeCount(v))
        assertEquals(Reason.SURPLUS, v["%064x".format(103)]!!.reason)
    }

    @Test fun offlineIsSignedByTheAdmin() {
        val day = 24L * 3600 * 1000
        var l = founded().mapValues { it.value.copy(seen = 100 * day) }
        l = TataSet.merge(rules(), l, mapOf(B to joined(B, A, 20).copy(seen = 10 * day)))
        assertEquals(listOf(B), TataSet.offlineToSign(l, 100, A, 100 * day).map { it.fp })
        assertTrue(TataSet.offlineToSign(l, 100, B, 100 * day).isEmpty())
    }
}
