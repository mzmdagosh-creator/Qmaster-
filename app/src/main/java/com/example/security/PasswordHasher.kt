package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {
    private val secureRandom = SecureRandom()

    fun generateSalt(): String {
        val saltBytes = ByteArray(16)
        secureRandom.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val combined = "$salt:$password"
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(combined.toByteArray(Charsets.UTF_8))
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val calculated = hashPassword(password, salt)
        return calculated.equals(expectedHash, ignoreCase = true)
    }
}
