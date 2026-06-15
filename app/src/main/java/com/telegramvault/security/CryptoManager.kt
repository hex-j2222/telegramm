package com.telegramvault.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEY_ALIAS        = "TelegramVaultMasterKey_v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_GCM          = "AES/GCM/NoPadding"
        private const val GCM_TAG_LEN      = 128
        private const val KEY_SIZE         = 256
        private const val PREFS_NAME       = "vault_secure_prefs"
        private const val DB_KEY_PREF      = "db_enc_key"
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    init { ensureKeyExists() }

    // ── Key management ───────────────────────────────────────────────────

    private fun ensureKeyExists() {
        if (keyStore.containsAlias(KEY_ALIAS)) return
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setRandomizedEncryptionRequired(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setUnlockedDeviceRequired(false)
                }
            }
            .build()
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }

    private fun masterKey(): SecretKey =
        (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey

    // ── AES-256-GCM encrypt / decrypt ────────────────────────────────────

    fun encrypt(plainText: String): String = try {
        val cipher = Cipher.getInstance(AES_GCM).also { it.init(Cipher.ENCRYPT_MODE, masterKey()) }
        val iv        = cipher.iv                               // 12 bytes random
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val out       = ByteArrayOutputStream()
        out.write(iv.size)          // 1 byte: iv length
        out.write(iv)               // 12 bytes
        out.write(encrypted)        // ciphertext + 16-byte GCM tag
        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) { "" }

    fun decrypt(encBase64: String): String = try {
        val bytes     = Base64.decode(encBase64, Base64.NO_WRAP)
        val ivLen     = bytes[0].toInt() and 0xFF
        val iv        = bytes.copyOfRange(1, 1 + ivLen)
        val encrypted = bytes.copyOfRange(1 + ivLen, bytes.size)
        val cipher    = Cipher.getInstance(AES_GCM)
            .also { it.init(Cipher.DECRYPT_MODE, masterKey(), GCMParameterSpec(GCM_TAG_LEN, iv)) }
        String(cipher.doFinal(encrypted), Charsets.UTF_8)
    } catch (e: Exception) { "" }

    // ── Password / PIN hashing (SHA-256 + static salt) ───────────────────

    fun hashPassword(input: String): String {
        val salted  = "TGVault::${input}::2024"
        val digest  = MessageDigest.getInstance("SHA-256")
        return digest.digest(salted.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    // ── SQLCipher database key ────────────────────────────────────────────

    fun getDatabaseKey(): ByteArray {
        val prefs  = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(DB_KEY_PREF, null)
        return if (stored != null) {
            // decrypt the stored base64 key
            val decoded = decrypt(stored)
            Base64.decode(decoded, Base64.NO_WRAP)
        } else {
            // generate a new 32-byte random key, encrypt it, store it
            val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
            val b64 = Base64.encodeToString(raw, Base64.NO_WRAP)
            prefs.edit().putString(DB_KEY_PREF, encrypt(b64)).apply()
            raw
        }
    }
}
