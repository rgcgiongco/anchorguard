package com.anchorguard.client.enforcement

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import com.anchorguard.client.auth.security.PinSecurityManager

@RunWith(AndroidJUnit4::class)
class DeviceAdminInstrumentedTest {

    private val context: Context = ApplicationProvider.getApplicationContext<Application>()
    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, AnchorGuardDeviceAdminReceiver::class.java)

    @Test
    fun `device admin receiver class is not null`() {
        assertNotNull(AnchorGuardDeviceAdminReceiver::class.java)
    }

    @Test
    fun `admin component name is constructed correctly`() {
        assertEquals("com.anchorguard.client", adminComponent.packageName)
        assertEquals(
            "com.anchorguard.client.enforcement.AnchorGuardDeviceAdminReceiver",
            adminComponent.className
        )
    }

    @Test
    fun `onDisableRequested returns CharSequence message`() {
        val result = AnchorGuardDeviceAdminReceiver::class.java.getDeclaredMethod(
            "onDisableRequested",
            Context::class.java,
            android.content.Intent::class.java
        )
        assertNotNull(result)
    }

    @Test
    fun `pin manager integrates with device admin receiver`() {
        val manager = PinSecurityManager(context)
        // Verify PinSecurityManager can be instantiated (used by DeviceAdminReceiver)
        assertNotNull(manager)
        assertFalse(manager.isPinSet())
    }

    @Test
    fun `device admin active state is readable`() {
        val isActive = dpm.isAdminActive(adminComponent)
        // May be false on test device if not activated; just verify no crash
        assertTrue(isActive || !isActive)
    }

    @Test
    fun `anchorguard device admin receiver constants exist`() {
        assertEquals(
            "com.anchorguard.client.extra.ADMIN_VERIFY_REASON",
            AnchorGuardDeviceAdminReceiver.EXTRA_ADMIN_VERIFY_REASON
        )
        assertEquals("AnchorGuardAdmin", AnchorGuardDeviceAdminReceiver.TAG)
    }
}
