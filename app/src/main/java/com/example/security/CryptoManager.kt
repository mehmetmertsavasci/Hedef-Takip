package com.example.security

import android.util.Base64
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_DERIVATION_ALGO = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12
    private const val SALT_LENGTH = 16

    fun encrypt(plainText: String, passphrase: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val iv = ByteArray(IV_LENGTH)
        random.nextBytes(iv)

        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGO)
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val envelope = JSONObject().apply {
            put("version", "AES-256-GCM")
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("cipher", Base64.encodeToString(cipherBytes, Base64.NO_WRAP))
            put("timestamp", System.currentTimeMillis())
        }

        return envelope.toString()
    }

    fun decrypt(encryptedJsonEnvelope: String, passphrase: String): Result<String> {
        return try {
            val envelope = JSONObject(encryptedJsonEnvelope)
            val salt = Base64.decode(envelope.getString("salt"), Base64.NO_WRAP)
            val iv = Base64.decode(envelope.getString("iv"), Base64.NO_WRAP)
            val cipherBytes = Base64.decode(envelope.getString("cipher"), Base64.NO_WRAP)

            val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGO)
            val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            val decryptedBytes = cipher.doFinal(cipherBytes)
            Result.success(String(decryptedBytes, Charsets.UTF_8))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
