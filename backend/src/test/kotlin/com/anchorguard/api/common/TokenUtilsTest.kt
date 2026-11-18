package com.anchorguard.api.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class TokenUtilsTest {

    @Test
    fun `randomToken returns non-empty base64url string`() {
        val token = TokenUtils.randomToken(32)
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(Pattern.matches("[A-Za-z0-9_-]+", token))
    }

    @Test
    fun `randomToken consistent length across calls`() {
        val t1 = TokenUtils.randomToken(16)
        val t2 = TokenUtils.randomToken(16)
        assertEquals(t1.length, t2.length)
        assertNotEquals(t1, t2)
    }

    @Test
    fun `pairingCode returns exact length`() {
        val code = TokenUtils.pairingCode(6)
        assertEquals(6, code.length)
    }

    @Test
    fun `pairingCode uses allowed charset only`() {
        val allowed = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = TokenUtils.pairingCode(100)
        assertTrue(code.all { it in allowed })
    }

    @Test
    fun `sha256 returns 64-char lowercase hex`() {
        val hash = TokenUtils.sha256("test")
        assertEquals(64, hash.length)
        assertTrue(Pattern.matches("[0-9a-f]+", hash))
    }

    @Test
    fun `sha256 is deterministic`() {
        assertEquals(TokenUtils.sha256("same-input"), TokenUtils.sha256("same-input"))
    }

    @Test
    fun `sha256 differs for different inputs`() {
        assertNotEquals(TokenUtils.sha256("a"), TokenUtils.sha256("b"))
    }
}
