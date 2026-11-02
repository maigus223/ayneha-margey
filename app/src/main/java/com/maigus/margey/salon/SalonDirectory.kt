// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - identité des salons : chaque salon a un code unique de 4 chiffres en plus de son nom,
// une fiche signée par son créateur (nom, code, durée de conservation), et il est annoncé aux
// téléphones à portée pour qu'on puisse y entrer d'un simple toucher.
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.salon

import android.content.Context
import com.bitchat.android.mesh.MeshService
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

/** Identifiant interne d'un salon : « #nom~1234 » (nom en minuscules, code de 4 chiffres). */
object SalonId {
    const val SEP = '~'
    private val rnd = SecureRandom()

    /** Nom tel qu'il est tapé, nettoyé (espaces réduits, sans caractères réservés). */
    fun cleanName(name: String): String =
        name.filter { it != SEP && it != '#' && it >= ' ' }.trim().replace(Regex("\\s+"), " ")

    fun make(name: String, code: String): String = "#" + cleanName(name).lowercase() + SEP + code

    fun isCoded(id: String) = code(id) != null

    fun code(id: String): String? =
        id.substringAfterLast(SEP, "").takeIf { it.length == 4 && it.all(Char::isDigit) && id.contains(SEP) }

    /** Nom affiché à défaut de fiche (ancien salon sans code : le nom entier). */
    fun rawName(id: String): String = id.removePrefix("#").let { if (code(id) != null) it.substringBeforeLast(SEP) else it }

    fun newCode(avoid: Set<String>): String {
        repeat(50) {
            val c = "%04d".format(rnd.nextInt(10_000))
            if (c !in avoid) return c
        }
        return "%04d".format(rnd.nextInt(10_000))
    }

    /** Chiffres tapés (AYNEHA ou latins) -> chiffres latins. */
    fun digits(typed: String): String = buildString {
        typed.codePoints().forEach { cp ->
            when (cp) {
                in '0'.code..'9'.code -> append(cp.toChar())
                in 0x10FF7C..0x10FF85 -> append('0' + (cp - 0x10FF7C))
            }
        }
    }
}

/** Fiche d'un salon, signée par son créateur. */
data class SalonInfo(
    val id: String,
    val name: String,
    val founder: String,
    val founderNick: String,
    val days: Int,
    val createdAt: Long,
    val sig: String,
) {
    val code: String get() = SalonId.code(id) ?: ""

    fun signedText() = "salon|$id|$days|$createdAt"

    fun valid(): Boolean =
        SalonId.isCoded(id) && days in MIN_DAYS..MAX_DAYS && name.isNotBlank() && name.length <= 60 &&
            SalonId.make(name, code) == id && TataKeys.verify(founder, signedText(), sig)

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("nm", name); put("f", founder); put("fn", founderNick)
        put("d", days); put("t", createdAt); put("s", sig)
    }

    companion object {
        const val MIN_DAYS = 3
        const val MAX_DAYS = 90
        fun fromJson(o: JSONObject): SalonInfo? = runCatching {
            SalonInfo(o.getString("id"), o.getString("nm"), o.getString("f"), o.optString("fn").take(40),
                o.getInt("d"), o.getLong("t"), o.getString("s"))
        }.getOrNull()
    }
}

/** Salon annoncé par un téléphone à portée. */
data class NearbySalon(val info: SalonInfo, val members: Int, val lastSeen: Long)

object SalonDirectory {
    private const val FILE = "margey_salon_dir.json"
    private const val ANNOUNCE = "\u0002MGS"
    private const val PERIOD_MS = 4 * 60_000L
    private const val MIN_GAP_MS = 30_000L
    /** Un salon disparaît de la liste « à portée » s'il n'a plus été annoncé depuis 15 minutes. */
    const val NEARBY_MS = 15 * 60_000L

    private val _known = MutableStateFlow<Map<String, SalonInfo>>(emptyMap())
    val known: StateFlow<Map<String, SalonInfo>> = _known.asStateFlow()
    private val _nearby = MutableStateFlow<Map<String, NearbySalon>>(emptyMap())
    val nearby: StateFlow<Map<String, NearbySalon>> = _nearby.asStateFlow()

    private var file: File? = null
    private var mesh: MeshService? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastAnnounce = 0L

    fun init(context: Context) {
        if (file != null) return
        val f = File(context.filesDir, FILE)
        file = f
        try {
            if (f.exists()) {
                val arr = JSONArray(f.readText())
                val map = mutableMapOf<String, SalonInfo>()
                for (i in 0 until arr.length()) SalonInfo.fromJson(arr.getJSONObject(i))?.let { map[it.id] = it }
                _known.value = map
            }
        } catch (_: Exception) { }
    }

    fun attach(meshService: MeshService) {
        if (mesh != null) { mesh = meshService; return }
        mesh = meshService
        scope.launch {
            while (isActive) {
                delay(PERIOD_MS)
                announce(force = true)
                prune()
            }
        }
    }

    @Synchronized
    private fun save() {
        val f = file ?: return
        try {
            val arr = JSONArray().apply { _known.value.values.forEach { put(it.toJson()) } }
            val tmp = File(f.parentFile, "$FILE.tmp")
            tmp.writeText(arr.toString())
            tmp.renameTo(f)
        } catch (_: Exception) { }
    }

    fun info(id: String): SalonInfo? = _known.value[id] ?: _nearby.value[id]?.info

    /** Nom à afficher (avec sa casse d'origine quand la fiche est connue). */
    fun name(id: String): String = info(id)?.name ?: SalonId.rawName(id)

    /** Durée de conservation du salon, choisie par son créateur (null : salon sans fiche). */
    fun days(id: String): Int? = info(id)?.days

    /** Créer un salon : nouveau code (différent des salons connus du même nom) et fiche signée. */
    fun create(name: String, days: Int, nick: String): SalonInfo {
        val clean = SalonId.cleanName(name)
        val prefix = "#" + clean.lowercase() + SalonId.SEP
        val taken = (_known.value.keys + _nearby.value.keys).filter { it.startsWith(prefix) }.mapNotNull { SalonId.code(it) }.toSet()
        val id = SalonId.make(clean, SalonId.newCode(taken))
        val now = System.currentTimeMillis()
        val d = days.coerceIn(SalonInfo.MIN_DAYS, SalonInfo.MAX_DAYS)
        val unsigned = SalonInfo(id, clean, TataKeys.publicHex, nick.take(40), d, now, "")
        val info = unsigned.copy(sig = TataKeys.sign(unsigned.signedText()))
        remember(info)
        return info
    }

    /** Garder la fiche d'un salon où l'on entre (ou qu'on a créé). */
    fun remember(info: SalonInfo) {
        if (!info.valid()) return
        val cur = _known.value[info.id]
        if (cur != null && !prefer(info, cur)) return
        _known.value = _known.value + (info.id to info)
        scope.launch { save() }
    }

    /** Entre deux fiches valides du même salon : la plus ancienne gagne (même choix sur tous les téléphones). */
    private fun prefer(a: SalonInfo, b: SalonInfo): Boolean =
        a.createdAt < b.createdAt || (a.createdAt == b.createdAt && a.founder < b.founder)

    fun forget(id: String) {
        if (_known.value.containsKey(id)) { _known.value = _known.value - id; scope.launch { save() } }
    }

    fun onPeersChanged() = announce(force = false)

    /** Annoncer les salons où l'on est membre actif (fiche + nombre de membres). */
    fun announce(force: Boolean) {
        val m = mesh ?: return
        val now = System.currentTimeMillis()
        if (!force && now - lastAnnounce < MIN_GAP_MS) return
        val me = TataKeys.publicHex
        val mine = _known.value.values.filter { SalonTata.view(it.id)[me]?.active == true }.take(20)
        if (mine.isEmpty()) return
        lastAnnounce = now
        val arr = JSONArray()
        mine.forEach { arr.put(it.toJson().put("n", TataSet.activeCount(SalonTata.view(it.id)))) }
        m.sendMessage(ANNOUNCE + arr.toString(), emptyList(), null)
    }

    private fun prune() {
        val now = System.currentTimeMillis()
        val keep = _nearby.value.filterValues { now - it.lastSeen < NEARBY_MS }
        if (keep.size != _nearby.value.size) _nearby.value = keep
    }

    /** Annonce reçue. Renvoie true si le message était une annonce de salons. */
    fun onControl(raw: String): Boolean {
        if (!raw.startsWith(ANNOUNCE)) return false
        val arr = try { JSONArray(raw.substring(ANNOUNCE.length)) } catch (_: Exception) { return true }
        val now = System.currentTimeMillis()
        val next = _nearby.value.toMutableMap()
        for (i in 0 until minOf(arr.length(), 20)) {
            val o = arr.optJSONObject(i) ?: continue
            val info = SalonInfo.fromJson(o) ?: continue
            if (!info.valid()) continue
            val n = o.optInt("n", 0).coerceIn(0, SalonTata.MAX_MEMBERS)
            val cur = next[info.id]
            val best = if (cur == null || prefer(info, cur.info)) info else cur.info
            next[info.id] = NearbySalon(best, if (best == info) n else cur!!.members, now)
            // Salon où l'on est déjà : on apprend sa fiche (durée, nom exact) si on ne l'avait pas
            if (_known.value[info.id]?.let { prefer(info, it) } != false && SalonTata.ledger(info.id).isNotEmpty()) remember(info)
        }
        _nearby.value = next
        return true
    }

    fun wipe() {
        _known.value = emptyMap()
        _nearby.value = emptyMap()
        try { file?.delete() } catch (_: Exception) { }
    }
}
