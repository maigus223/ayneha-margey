// SPDX-License-Identifier: GPL-3.0-or-later
// Tata-Set : méthode de comptage des membres sans serveur pour réseau maillé.
// Auteur de la méthode : Mahamadou Issiaka MAIGA (MAIGUS), Margey, 2026 (voir docs/TATA-SET.md).
//
// Chaque ligne de la liste est un « fait » signé (Ed25519) par la personne qui a le droit de le faire :
//   - entrer, revenir, quitter : la personne elle-même (et l'invitation porte la signature de celui qui invite) ;
//   - exclure, réintégrer un exclu, constater 45 jours d'absence : l'administrateur en fonction ;
//   - réintégrer un absent de 45 jours : n'importe quel membre actif.
// Le surplus (plafond dépassé) n'est pas signé : chaque téléphone le calcule de la même façon.
package com.maigus.margey.tata

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/** Raisons d'un état. */
object Reason {
    const val JOINED = 0
    const val LEFT = 1
    const val EXCLUDED = 2   // exclu par l'administrateur
    const val OFFLINE = 3    // exclu d'office : 45 jours sans être vu
    const val SURPLUS = 4    // retiré : plafond dépassé (calculé, jamais enregistré)
}

/**
 * Une ligne de la liste des membres.
 * @param fp code public Ed25519 du membre (son identifiant)
 * @param ver compteur de changements (horloge logique : max(ancien + 1, heure))
 * @param rank rang d'entrée : le plus ancien actif est l'administrateur
 * @param seen dernière fois qu'on l'a vu en ligne (indication, non signée)
 * @param by code de celui qui a signé ce changement (ou qui a invité, pour une entrée)
 * @param ti heure de l'invitation, [tok] signature de l'invitation par [by]
 * @param sig signature du changement
 */
data class Member(
    val fp: String,
    val nick: String,
    val active: Boolean,
    val ver: Long,
    val rank: Long,
    val seen: Long,
    val reason: Int,
    val by: String,
    val at: Long,
    val ti: Long = 0,
    val tok: String = "",
    val sig: String = "",
)

/** Événement à afficher en petit message d'information. */
sealed class TataEvent {
    data class Joined(val member: Member, val byNick: String) : TataEvent()
    data class Left(val member: Member) : TataEvent()
    data class Excluded(val member: Member) : TataEvent()
    data class Offline(val member: Member) : TataEvent()
    data class Surplus(val member: Member) : TataEvent()
    data class NewAdmin(val member: Member) : TataEvent()
}

object TataSet {
    const val OFFLINE_MS = 45L * 24 * 3600 * 1000

    fun nextVer(old: Long, now: Long) = maxOf(old + 1, now)

    // ---------- Signatures ----------

    private fun payload(scope: String, m: Member) =
        "tata1|$scope|${m.fp}|${m.nick}|${if (m.active) 1 else 0}|${m.ver}|${m.rank}|${m.reason}|${m.by}|${m.at}|${m.ti}|${m.tok}"

    fun invitePayload(scope: String, ti: Long) = "tata1-invite|$scope|$ti"

    /** Signe un changement avec la clé de ce téléphone (qui doit être [m].by, ou [m].fp pour soi-même). */
    fun signed(scope: String, m: Member): Member = m.copy(sig = TataKeys.sign(payload(scope, m)))

    private fun signedBy(scope: String, m: Member, key: String) = TataKeys.verify(key, payload(scope, m), m.sig)

    /** L'invitation (QR) porte-t-elle bien la signature de [m].by ? */
    fun inviteValid(scope: String, m: Member) =
        m.by.isNotBlank() && m.by != m.fp && TataKeys.verify(m.by, invitePayload(scope, m.ti), m.tok)

    // ---------- Vue calculée ----------

    /** Liste vue par l'interface : les entrées au-delà du plafond absolu sont retirées (surplus). */
    fun view(m: Map<String, Member>, hardCap: Int): Map<String, Member> {
        val ordered = m.values.filter { it.active }.sortedWith(compareBy<Member>({ it.rank }, { it.fp }))
        if (ordered.size <= hardCap) return m
        val out = m.toMutableMap()
        ordered.drop(hardCap).forEach { out[it.fp] = it.copy(active = false, reason = Reason.SURPLUS) }
        return out
    }

    fun actives(m: Map<String, Member>) = m.values.filter { it.active }
    fun activeCount(m: Map<String, Member>) = m.values.count { it.active }

    /** L'administrateur : le membre actif au plus ancien rang d'entrée (à égalité, le plus petit code). */
    fun admin(m: Map<String, Member>): String? =
        actives(m).minWithOrNull(compareBy<Member>({ it.rank }, { it.fp }))?.fp

    // ---------- Fusion avec vérification des signatures ----------

    /**
     * Règles d'un groupe ou d'un salon.
     * @param scope identifiant du groupe ou nom du salon (une signature ne vaut que pour lui)
     * @param founder code du créateur (groupes : connu par l'invitation ; salons : null)
     * @param open accès libre (salons) : on entre et on revient soi-même
     */
    data class Rules(val scope: String, val founder: String?, val open: Boolean, val softCap: Int, val hardCap: Int)

    /** Ce changement est-il signé par quelqu'un qui en avait le droit ? */
    fun authorized(r: Rules, cand: Member, cur: Member?, local: Map<String, Member>): Boolean {
        val v = view(local, r.hardCap)
        val adminFp = admin(v)
        val scope = r.scope
        return when (cand.reason) {
            Reason.JOINED -> when {
                signedBy(scope, cand, cand.fp) -> selfJoinAllowed(r, cand, cur, v, adminFp)
                cand.by != cand.fp && signedBy(scope, cand, cand.by) -> {
                    // Réintégration signée par un autre : jamais avant la sortie (pas de rang antidaté)
                    if (cur == null || cur.active || cand.rank < cur.at) false
                    else when (cur.reason) {
                        Reason.OFFLINE -> v[cand.by]?.active == true
                        else -> cand.by == adminFp
                    }
                }
                else -> false
            }
            Reason.LEFT -> signedBy(scope, cand, cand.fp)
            Reason.EXCLUDED -> cand.by == adminFp && cand.by != cand.fp && signedBy(scope, cand, cand.by)
            Reason.OFFLINE -> {
                // L'administrateur constate l'absence ; si c'est l'administrateur qui est absent, son suivant
                val allowed = if (cand.fp == adminFp) admin(v - cand.fp) else adminFp
                cand.by == allowed && signedBy(scope, cand, cand.by)
            }
            else -> false
        }
    }

    private fun selfJoinAllowed(r: Rules, cand: Member, cur: Member?, v: Map<String, Member>, adminFp: String?): Boolean {
        if (cur != null && cur.active) return cand.rank == cur.rank          // simple mise à jour (pseudo)
        if (cur != null && cur.reason == Reason.EXCLUDED) return false        // seul l'administrateur réintègre
        if (cur != null && cand.rank < cur.at) return false                   // pas de rang antidaté
        if (r.open) {
            // Salon : entrée libre, mais personne ne peut se placer avant les membres déjà connus
            val minRank = v.values.filter { it.fp != cand.fp }.minOfOrNull { it.rank }
            return cur != null || minRank == null || cand.rank > minRank
        }
        // Groupe : le créateur, ou une invitation valide signée par un membre actif
        if (cur == null && cand.fp == r.founder && cand.by == cand.fp) return true
        if (cur != null && cur.reason == Reason.OFFLINE) return false        // un membre doit le réintégrer
        if (!inviteValid(r.scope, cand)) return false
        val inviter = v[cand.by] ?: return false
        if (!inviter.active || cand.ti < inviter.rank || cand.rank < cand.ti) return false
        // Au-delà de 80, seul l'administrateur invite (la marge absorbe les entrées simultanées)
        return activeCount(v) < r.softCap || cand.by == adminFp || activeCount(v) < r.hardCap && cand.ti < (v.values.filter { it.active }.sortedBy { it.rank }.getOrNull(r.softCap - 1)?.rank ?: Long.MAX_VALUE)
    }

    /** Entre deux versions valides d'une même ligne : le compteur le plus grand gagne ; à égalité, l'état inactif. */
    private fun newer(a: Member, b: Member): Member = when {
        a.ver != b.ver -> if (a.ver > b.ver) a else b
        a.active != b.active -> if (!a.active) a else b
        else -> if (a.sig <= b.sig) a else b
    }

    /** Fusionne une liste reçue dans la liste locale, en refusant tout changement mal signé. */
    fun merge(r: Rules, local: Map<String, Member>, incoming: Map<String, Member>): Map<String, Member> {
        val out = local.toMutableMap()
        // Plusieurs passes : une invitation n'est valable qu'une fois celui qui invite connu
        var todo = incoming.values.sortedWith(compareBy<Member>({ it.rank }, { it.ver }))
        var progress = true
        while (todo.isNotEmpty() && progress) {
            progress = false
            val retry = mutableListOf<Member>()
            for (cand in todo) {
                val cur = out[cand.fp]
                if (cur != null && cand.ver == cur.ver && cand.sig == cur.sig) {
                    if (cand.seen > cur.seen) out[cand.fp] = cur.copy(seen = cand.seen)
                    continue
                }
                val seen = maxOf(cur?.seen ?: 0, cand.seen)
                val win = if (cur == null) cand else newer(cur, cand)
                if (win !== cand) { if (cur != null) out[cand.fp] = cur.copy(seen = seen); continue }
                if (authorized(r, cand, cur, out)) { out[cand.fp] = cand.copy(seen = seen); progress = true }
                else retry += cand
            }
            todo = retry
        }
        return out
    }

    /** Membres absents depuis plus de 45 jours que CE téléphone a le droit de constater (administrateur ou suivant). */
    fun offlineToSign(m: Map<String, Member>, hardCap: Int, myFp: String, now: Long): List<Member> {
        val v = view(m, hardCap)
        val adminFp = admin(v)
        return actives(v).filter { x ->
            x.fp != myFp && x.seen > 0 && now - x.seen > OFFLINE_MS &&
                myFp == (if (x.fp == adminFp) admin(v - x.fp) else adminFp)
        }
    }

    /** Différences entre l'ancienne et la nouvelle vue, pour les messages d'information. */
    fun events(old: Map<String, Member>, new: Map<String, Member>): List<TataEvent> {
        val ev = mutableListOf<TataEvent>()
        for ((fp, n) in new) {
            val o = old[fp]
            if (n.active && (o == null || !o.active)) {
                ev += TataEvent.Joined(n, if (n.by == n.fp) "" else (new[n.by]?.nick ?: old[n.by]?.nick ?: ""))
            } else if (!n.active && o != null && o.active) {
                ev += when (n.reason) {
                    Reason.EXCLUDED -> TataEvent.Excluded(n)
                    Reason.OFFLINE -> TataEvent.Offline(n)
                    Reason.SURPLUS -> TataEvent.Surplus(n)
                    else -> TataEvent.Left(n)
                }
            }
        }
        val a0 = admin(old); val a1 = admin(new)
        if (old.isNotEmpty() && a1 != null && a0 != a1) new[a1]?.let { ev += TataEvent.NewAdmin(it) }
        return ev
    }

    /** Empreinte courte de la liste (faits signés seulement). */
    fun hash(m: Map<String, Member>): String {
        val s = m.values.sortedBy { it.fp }.joinToString("|") { "${it.fp}:${it.ver}:${it.sig.take(16)}" }
        return MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).take(6).joinToString("") { "%02x".format(it) }
    }

    fun toJson(m: Map<String, Member>): JSONArray = JSONArray().apply {
        m.values.forEach { x ->
            put(JSONObject().apply {
                put("f", x.fp); put("n", x.nick); put("a", if (x.active) 1 else 0); put("v", x.ver)
                put("r", x.rank); put("s", x.seen); put("w", x.reason); put("b", x.by); put("t", x.at)
                if (x.ti != 0L) put("i", x.ti)
                if (x.tok.isNotEmpty()) put("k", x.tok)
                put("g", x.sig)
            })
        }
    }

    fun fromJson(a: JSONArray?): Map<String, Member> {
        val out = mutableMapOf<String, Member>()
        if (a == null) return out
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            val fp = o.optString("f").ifBlank { null } ?: continue
            out[fp] = Member(fp, o.optString("n"), o.optInt("a") == 1, o.optLong("v"), o.optLong("r"),
                o.optLong("s"), o.optInt("w"), o.optString("b"), o.optLong("t"), o.optLong("i"), o.optString("k"), o.optString("g"))
        }
        return out
    }
}
