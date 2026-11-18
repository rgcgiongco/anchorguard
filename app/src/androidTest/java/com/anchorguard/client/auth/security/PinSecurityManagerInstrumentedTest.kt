package com.anchorguard.client.auth.security

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinSecurityManagerInstrumentedTest {

    private val context: Context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `createPin stores hash and marks setup complete`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()

        val result = manager.createPin("1234")

        assertTrue(result)
        assertTrue(manager.isPinSet())
        assertTrue(manager.isSetupComplete())
    }

    @Test
    fun `createPin rejects short pin`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()

        val result = manager.createPin("12")

        assertFalse(result)
        assertFalse(manager.isPinSet())
    }

    @Test
    fun `createPin rejects non-digit pin`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()

        val result = manager.createPin("abcd")

        assertFalse(result)
        assertFalse(manager.isPinSet())
    }

    @Test
    fun `verifyPin returns Success for correct pin`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()
        manager.createPin("4321")

        val result = manager.verifyPin("4321")

        assertTrue(result is PinVerifyResult.Success)
    }

    @Test
    fun `verifyPin returns Failure for wrong pin`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()
        manager.createPin("4321")

        val result = manager.verifyPin("0000")

        assertTrue(result is PinVerifyResult.Failure)
    }

    @Test
    fun `verifyPin returns NotSet when no pin configured`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()

        val result = manager.verifyPin("1234")

        assertTrue(result is PinVerifyResult.NotSet)
    }

    @Test
    fun `changePin updates stored hash`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()
        manager.createPin("1111")

        val result = manager.changePin("1111", "2222")

        assertTrue(result is PinVerifyResult.Success)
        assertTrue(manager.verifyPin("2222") is PinVerifyResult.Success)
        assertFalse(manager.verifyPin("1111") is PinVerifyResult.Success)
    }

    @Test
    fun `changePin rejects wrong old pin`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()
        manager.createPin("1111")

        val result = manager.changePin("9999", "2222")

        assertTrue(result is PinVerifyResult.Failure)
    }

    @Test
    fun `recordFailure triggers lockout after max attempts`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()
        manager.createPin("1111")

        for (i in 0 until PinSecurityManager.MAX_FAILED_ATTEMPTS) {
            manager.verifyPin("wrong")
        }

        val state = manager.lockoutState()
        assertTrue(state.isLocked())
        val remaining = state.remainingSeconds()
        assertTrue(remaining > 0L)
    }

    @Test
    fun `setParentSessionValid and isParentSessionValid`() {
        val manager = PinSecurityManager(context)
        manager.clearParentSession()

        assertFalse(manager.isParentSessionValid())

        manager.setParentSessionValid(300_000L)

        assertTrue(manager.isParentSessionValid())
    }

    @Test
    fun `pinManager is not null`() {
        val manager = PinSecurityManager(context)
        assertNotNull(manager)
    }
}
