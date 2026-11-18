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
    fun `createPinStoresHashAndMarksSetupComplete`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()

        val result = manager.createPin("1234")

        assertTrue(result)
        assertTrue(manager.isPinSet())
        assertTrue(manager.isSetupComplete())
    }

    @Test
    fun `createPinRejectsShortPin`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()

        val result = manager.createPin("12")

        assertFalse(result)
        assertFalse(manager.isPinSet())
    }

    @Test
    fun `createPinRejectsNonDigitPin`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()

        val result = manager.createPin("abcd")

        assertFalse(result)
        assertFalse(manager.isPinSet())
    }

    @Test
    fun `verifyPinReturnsSuccessForCorrectPin`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()
        manager.createPin("4321")

        val result = manager.verifyPin("4321")

        assertTrue(result is PinVerifyResult.Success)
    }

    @Test
    fun `verifyPinReturnsFailureForWrongPin`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()
        manager.createPin("4321")

        val result = manager.verifyPin("0000")

        assertTrue(result is PinVerifyResult.Failure)
    }

    @Test
    fun `verifyPinReturnsNotSetWhenNoPinConfigured`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()

        val result = manager.verifyPin("1234")

        assertTrue(result is PinVerifyResult.NotSet)
    }

    @Test
    fun `changePinUpdatesStoredHash`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()
        manager.createPin("1111")

        val result = manager.changePin("1111", "2222")

        assertTrue(result is PinVerifyResult.Success)
        assertTrue(manager.verifyPin("2222") is PinVerifyResult.Success)
        assertFalse(manager.verifyPin("1111") is PinVerifyResult.Success)
    }

    @Test
    fun `changePinRejectsWrongOldPin`() {
        val manager = PinSecurityManager(context)
        manager.clearPinState()
        manager.createPin("1111")

        val result = manager.changePin("9999", "2222")

        assertTrue(result is PinVerifyResult.Failure)
    }

    @Test
    fun `recordFailureTriggersLockoutAfterMaxAttempts`() {
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
    fun `setParentSessionValidAndIsParentSessionValid`() {
        val manager = PinSecurityManager(context)
        manager.clearParentSession()

        assertFalse(manager.isParentSessionValid())

        manager.setParentSessionValid(300_000L)

        assertTrue(manager.isParentSessionValid())
    }

    @Test
    fun `pinManagerIsNotNull`() {
        val manager = PinSecurityManager(context)
        assertNotNull(manager)
    }
}
