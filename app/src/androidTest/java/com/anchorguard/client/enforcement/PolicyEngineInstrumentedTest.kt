package com.anchorguard.client.enforcement

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PolicyEngineInstrumentedTest {

    private val context: Context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `isAllowed returns true for own package`() {
        val engine = PolicyEngine(context)

        val allowed = engine.isAllowed("com.anchorguard.client")

        assertTrue(allowed)
    }

    @Test
    fun `isAllowed returns true for systemui`() {
        val engine = PolicyEngine(context)

        val allowed = engine.isAllowed("com.android.systemui")

        assertTrue(allowed)
    }

    @Test
    fun `isAllowed returns true for blocked package in parent mode`() {
        val engine = PolicyEngine(context)
        engine.setBlockedPackages(setOf("com.android.chrome"))
        engine.setParentMode(true)

        val allowed = engine.isAllowed("com.android.chrome")

        assertTrue(allowed)
    }

    @Test
    fun `isAllowed returns false for blocked package outside parent mode`() {
        val engine = PolicyEngine(context)
        engine.setBlockedPackages(setOf("com.android.chrome"))
        engine.setParentMode(false)

        val allowed = engine.isAllowed("com.android.chrome")

        assertFalse(allowed)
    }

    @Test
    fun `isAllowed returns true for non-blocked package`() {
        val engine = PolicyEngine(context)
        engine.setBlockedPackages(setOf("com.android.chrome"))
        engine.setParentMode(false)

        val allowed = engine.isAllowed("com.unknown.app")

        assertTrue(allowed)
    }

    @Test
    fun `setBlockedPackages and blockedPackages round-trip`() {
        val engine = PolicyEngine(context)
        engine.setBlockedPackages(setOf("pkg-a", "pkg-b"))

        val blocked = engine.blockedPackages()

        assertEquals(2, blocked.size)
        assertTrue("pkg-a" in blocked)
        assertTrue("pkg-b" in blocked)
    }

    @Test
    fun `setParentMode and isParentMode round-trip`() {
        val engine = PolicyEngine(context)

        engine.setParentMode(true)
        assertTrue(engine.isParentMode())

        engine.setParentMode(false)
        assertFalse(engine.isParentMode())
    }

    @Test
    fun `blockIntervalMs default is 500ms`() {
        val engine = PolicyEngine(context)

        assertEquals(500L, engine.blockIntervalMs())
    }

    @Test
    fun `setBlockIntervalMs enforces minimum`() {
        val engine = PolicyEngine(context)
        engine.setBlockIntervalMs(50L)

        assertEquals(100L, engine.blockIntervalMs())
    }
}
