// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - verrou de l'application (E12) et effacement automatique (E10)
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.security

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bitchat.android.services.AppStateStore
import com.bitchat.android.services.ConversationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Code à 4 chiffres. Seule une empreinte salée (SHA-256) est enregistrée, jamais le code lui-même.
 * L'application se verrouille au démarrage et quand elle revient au premier plan.
 */
object AppLock {
    private const val PREF = "margey_lock"
    private var prefs: android.content.SharedPreferences? = null

    var enabled by mutableStateOf(false); private set
    /** true tant que l'utilisateur n'a pas saisi le bon code. */
    var locked by mutableStateOf(false)

    fun init(ctx: Context) {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs = p
        enabled = p.getString("hash", null) != null
        locked = enabled
    }

    private fun hash(code: String, salt: String): String =
        MessageDigest.getInstance("SHA-256").digest((salt + code).toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun setCode(code: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }
        prefs?.edit()?.putString("salt", salt)?.putString("hash", hash(code, salt))?.apply()
        enabled = true
        locked = false
    }

    fun disable() {
        prefs?.edit()?.clear()?.apply()
        enabled = false
        locked = false
    }

    fun check(code: String): Boolean {
        val p = prefs ?: return false
        val salt = p.getString("salt", null) ?: return false
        val ok = hash(code, salt) == p.getString("hash", null)
        if (ok) locked = false
        return ok
    }

    private var stoppedAt = 0L
    /** Appelé quand l'application passe en arrière-plan. */
    fun onStop() { stoppedAt = System.currentTimeMillis() }
    /** Au retour : verrouiller si l'absence a duré plus de 30 secondes (partage, appareil photo...). */
    fun onStart() { if (enabled && stoppedAt > 0 && System.currentTimeMillis() - stoppedAt > 30_000) locked = true }
}

/** Effacement automatique des discussions privées après un nombre de jours (0 = jamais, E11). */
object AutoErase {
    private const val PREF = "margey_erase"
    val CHOICES = listOf(0, 1, 7, 30)
    var days by mutableIntStateOf(0); private set
    private var prefs: android.content.SharedPreferences? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false

    fun init(ctx: Context) {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs = p
        days = p.getInt("days", 0)
        if (started) return
        started = true
        scope.launch {
            delay(20_000) // laisser la base se charger
            while (isActive) {
                runOnce()
                delay(60 * 60_000L)
            }
        }
    }

    fun set(d: Int) {
        days = d
        prefs?.edit()?.putInt("days", d)?.apply()
        scope.launch { runOnce() }
    }

    private suspend fun runOnce() {
        val d = days
        if (d <= 0) return
        val repo = ConversationRepository.tryGetInstance() ?: return
        val cutoff = System.currentTimeMillis() - d * 86_400_000L
        repo.messageIDsOlderThan(cutoff).forEach { id ->
            try { AppStateStore.removePrivateMessage(id) } catch (_: Exception) { }
            try { repo.deleteMessage(id) } catch (_: Exception) { }
        }
    }
}
