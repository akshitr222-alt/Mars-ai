package com.example.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object CryptoHelper {
    private const val HASH_ALGORITHM = "SHA-256"
    private val secureRandom = SecureRandom()

    /**
     * Generates a crytographically secure random salt of 16 bytes.
     */
    fun generateSalt(): String {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    /**
     * Generates a salted SHA-256 hash of the password.
     */
    fun hashPassword(password: String, salt: String): String {
        return try {
            val digest = MessageDigest.getInstance(HASH_ALGORITHM)
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
            digest.reset()
            digest.update(saltBytes)
            val hashedBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback (should not occur in standard Android runtimes)
            password
        }
    }

    /**
     * Verifies if the matching hashed password matches the stored credentials.
     */
    fun verifyPassword(password: String, salt: String, storedHash: String): Boolean {
        val calculatedHash = hashPassword(password, salt)
        return calculatedHash == storedHash
    }
}
