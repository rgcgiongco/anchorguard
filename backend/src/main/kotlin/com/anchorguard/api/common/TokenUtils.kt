package com.anchorguard.api.common

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object TokenUtils {
    private val random = SecureRandom()

    fun randomToken(bytes: Int = 32): String {
        val buf = ByteArray(bytes)
        random.nextBytes(buf)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf)
    }

    fun pairingCode(length: Int = 6): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..length).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    fun sha256(value: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
