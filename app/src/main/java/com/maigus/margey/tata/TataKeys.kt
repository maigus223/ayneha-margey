// SPDX-License-Identifier: GPL-3.0-or-later
// Tata-Set : clé de signature Ed25519 propre à chaque téléphone. Son code public est l'identifiant
// du membre dans les listes Tata-Set : personne ne peut signer à sa place.
package com.maigus.margey.tata

import android.content.Context
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object TataKeys {
    private const val PREF = "margey_tata_key"
    private var priv: Ed25519PrivateKeyParameters? = null
    private var boxPriv: X25519PrivateKeyParameters? = null
    /** Clé publique de chiffrement X25519 (64 caractères hexadécimaux) : sert à fermer les courriers. */
    var boxPublicHex: String = ""; private set
    private val rnd = SecureRandom()
    /** Code public (64 caractères hexadécimaux) : identifiant Tata-Set de ce téléphone. */
    var publicHex: String = ""; private set

    fun init(ctx: Context) {
        if (priv != null) return
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val saved = p.getString("k", null)
        val key = if (saved != null) Ed25519PrivateKeyParameters(hexToBytes(saved), 0)
        else Ed25519PrivateKeyParameters(SecureRandom()).also { p.edit().putString("k", bytesToHex(it.encoded)).apply() }
        priv = key
        publicHex = bytesToHex(key.generatePublicKey().encoded)
        val savedBox = p.getString("x", null)
        val box = if (savedBox != null) X25519PrivateKeyParameters(hexToBytes(savedBox), 0)
        else X25519PrivateKeyParameters(rnd).also { p.edit().putString("x", bytesToHex(it.encoded)).apply() }
        boxPriv = box
        boxPublicHex = bytesToHex(box.generatePublicKey().encoded)
    }

    /** Pour les tests : une clé en mémoire. */
    fun initForTest(seed: Int) {
        val bytes = ByteArray(32) { (it + seed).toByte() }
        priv = Ed25519PrivateKeyParameters(bytes, 0)
        publicHex = bytesToHex(priv!!.generatePublicKey().encoded)
        boxPriv = X25519PrivateKeyParameters(ByteArray(32) { (it * 3 + seed).toByte() }, 0)
        boxPublicHex = bytesToHex(boxPriv!!.generatePublicKey().encoded)
    }

    private fun boxKey(shared: ByteArray, ephPub: ByteArray, recipientPub: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(shared + ephPub + recipientPub)

    /**
     * Ferme des données pour le seul propriétaire de [recipientBoxHex] (X25519 éphémère + AES-256-GCM).
     * Résultat : clé éphémère (32 octets) + nonce (12) + données chiffrées.
     */
    fun seal(recipientBoxHex: String, plain: ByteArray): ByteArray {
        val recipient = hexToBytes(recipientBoxHex)
        val eph = X25519PrivateKeyParameters(rnd)
        val ephPub = eph.generatePublicKey().encoded
        val shared = ByteArray(32).also { eph.generateSecret(X25519PublicKeyParameters(recipient, 0), it, 0) }
        val nonce = ByteArray(12).also { rnd.nextBytes(it) }
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, SecretKeySpec(boxKey(shared, ephPub, recipient), "AES"), GCMParameterSpec(128, nonce))
        return ephPub + nonce + c.doFinal(plain)
    }

    /** Ouvre des données fermées pour ce téléphone, ou null si elles ne lui sont pas destinées. */
    fun open(sealed: ByteArray): ByteArray? = try {
        val k = boxPriv ?: return null
        val ephPub = sealed.copyOfRange(0, 32)
        val nonce = sealed.copyOfRange(32, 44)
        val shared = ByteArray(32).also { k.generateSecret(X25519PublicKeyParameters(ephPub, 0), it, 0) }
        val me = k.generatePublicKey().encoded
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, SecretKeySpec(boxKey(shared, ephPub, me), "AES"), GCMParameterSpec(128, nonce))
        c.doFinal(sealed, 44, sealed.size - 44)
    } catch (_: Exception) { null }

    fun sign(message: String): String {
        val k = priv ?: return ""
        val s = Ed25519Signer()
        s.init(true, k)
        val b = message.toByteArray(Charsets.UTF_8)
        s.update(b, 0, b.size)
        return bytesToHex(s.generateSignature())
    }

    fun verify(publicHex: String, message: String, signatureHex: String): Boolean = try {
        if (publicHex.length != 64 || signatureHex.length != 128) false else {
            val s = Ed25519Signer()
            s.init(false, Ed25519PublicKeyParameters(hexToBytes(publicHex), 0))
            val b = message.toByteArray(Charsets.UTF_8)
            s.update(b, 0, b.size)
            s.verifySignature(hexToBytes(signatureHex))
        }
    } catch (_: Exception) { false }

    /** Effacement d'urgence : une nouvelle identité sera créée au prochain démarrage. */
    fun wipe(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
        priv = null; publicHex = ""; boxPriv = null; boxPublicHex = ""
        init(ctx)
    }

    fun bytesToHex(b: ByteArray) = b.joinToString("") { "%02x".format(it) }
    fun hexToBytes(h: String) = ByteArray(h.length / 2) { h.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
