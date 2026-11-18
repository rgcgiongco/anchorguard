package com.anchorguard.client.auth.security

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class PinLockoutState(
    val failedAttempts: Int,
    val lockoutUntilMs: Long
) {
    fun isLocked(nowMs: Long = System.currentTimeMillis()): Boolean = lockoutUntilMs > nowMs
    fun remainingSeconds(nowMs: Long = System.currentTimeMillis()): Long =
        ((lockoutUntilMs - nowMs).coerceAtLeast(0L) + 999L) / 1000L
}

sealed interface PinVerifyResult {
    data object Success : PinVerifyResult
    data class Failure(val state: PinLockoutState) : PinVerifyResult
    data class Locked(val state: PinLockoutState) : PinVerifyResult
    data object NotSet : PinVerifyResult
    data object InvalidFormat : PinVerifyResult
}

class PinSecurityManager(context: Context) {
    private val prefs = SecureSessionStore(context).raw()

    fun isPinSet(): Boolean = prefs.getString(KEY_PIN_HASH, null) != null && prefs.getString(KEY_PIN_SALT, null) != null

    fun isSetupComplete(): Boolean = prefs.getBoolean(KEY_SETUP_COMPLETE, false)

    fun setSetupComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETE, complete).apply()
    }

    fun createPin(pin: String): Boolean {
        if (!isValidPin(pin)) return false
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_PIN_SALT, Base64.getEncoder().encodeToString(salt))
            .putBoolean(KEY_SETUP_COMPLETE, true)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
        return true
    }

    fun changePin(oldPin: String, newPin: String): PinVerifyResult {
        val verify = verifyInternal(oldPin, countFailure = true)
        if (verify !is PinVerifyResult.Success) return verify
        if (!isValidPin(newPin)) return PinVerifyResult.InvalidFormat
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(newPin, salt)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_PIN_SALT, Base64.getEncoder().encodeToString(salt))
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
        return PinVerifyResult.Success
    }

    fun verifyPin(pin: String): PinVerifyResult = verifyInternal(pin, countFailure = true)

    fun verifyPinWithoutPenalty(pin: String): PinVerifyResult = verifyInternal(pin, countFailure = false)

    private fun verifyInternal(pin: String, countFailure: Boolean): PinVerifyResult {
        if (!isPinSet()) return PinVerifyResult.NotSet
        val state = lockoutState()
        val now = System.currentTimeMillis()
        if (state.isLocked(now)) return PinVerifyResult.Locked(state)
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return PinVerifyResult.NotSet
        val saltB64 = prefs.getString(KEY_PIN_SALT, null) ?: return PinVerifyResult.NotSet
        val salt = try { Base64.getDecoder().decode(saltB64) } catch (_: Throwable) { return PinVerifyResult.NotSet }
        val ok = constantTimeEquals(storedHash, hashPin(pin, salt))
        return if (ok) {
            prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).putLong(KEY_LOCKOUT_UNTIL, 0L).apply()
            PinVerifyResult.Success
        } else {
            if (countFailure) recordFailure() else PinVerifyResult.Failure(lockoutState())
        }
    }

    fun recordBiometricSuccess() {
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).putLong(KEY_LOCKOUT_UNTIL, 0L).apply()
        setParentSessionValid(SESSION_DURATION_MS)
    }

    fun lockoutState(): PinLockoutState = PinLockoutState(
        failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0),
        lockoutUntilMs = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
    )

    fun setParentSessionValid(durationMs: Long = SESSION_DURATION_MS) {
        prefs.edit().putLong(KEY_PARENT_SESSION_UNTIL, System.currentTimeMillis() + durationMs).apply()
    }

    fun isParentSessionValid(nowMs: Long = System.currentTimeMillis()): Boolean =
        prefs.getLong(KEY_PARENT_SESSION_UNTIL, 0L) > nowMs

    fun clearParentSession() {
        prefs.edit().putLong(KEY_PARENT_SESSION_UNTIL, 0L).apply()
    }

    fun clearPinState() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .remove(KEY_SETUP_COMPLETE)
            .remove(KEY_FAILED_ATTEMPTS)
            .remove(KEY_LOCKOUT_UNTIL)
            .remove(KEY_PARENT_SESSION_UNTIL)
            .apply()
    }

    private fun recordFailure(): PinVerifyResult {
        val now = System.currentTimeMillis()
        val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        val lockoutUntil = if (attempts >= MAX_FAILED_ATTEMPTS) now + LOCKOUT_DURATION_MS else 0L
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, if (lockoutUntil > 0L) 0 else attempts)
            .putLong(KEY_LOCKOUT_UNTIL, lockoutUntil)
            .apply()
        val state = lockoutState()
        return if (state.isLocked(now)) PinVerifyResult.Locked(state) else PinVerifyResult.Failure(state)
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        // PBKDF2-HMAC-SHA256, client-side at-rest hash (AnchorGuard original usage).
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val bytes = skf.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))

    private fun isValidPin(pin: String): Boolean = pin.length >= 4 && pin.length <= 8 && pin.all { it.isDigit() }

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 300_000L
        const val SESSION_DURATION_MS = 5 * 60 * 1000L
        private const val PBKDF2_ITERATIONS = 120_000
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_SETUP_COMPLETE = "pin_setup_complete"
        private const val KEY_FAILED_ATTEMPTS = "pin_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "pin_lockout_until"
        private const val KEY_PARENT_SESSION_UNTIL = "parent_session_until"
    }
}
