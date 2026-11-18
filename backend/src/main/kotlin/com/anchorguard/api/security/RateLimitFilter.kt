package com.anchorguard.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitFilter : OncePerRequestFilter() {
    private data class Bucket(var count: Int, var windowStart: Instant)
    private val buckets = ConcurrentHashMap<String, Bucket>()
    private val limit = 20
    private val windowSeconds = 60L

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        if (request.method != "POST") return true
        return !(path == "/v1/auth/login" || path == "/v1/devices/pair")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val key = (request.remoteAddr ?: "unknown") + ":" + request.requestURI
        val now = Instant.now()
        val bucket = buckets.compute(key) { _, existing ->
            if (existing == null || existing.windowStart.plusSeconds(windowSeconds).isBefore(now)) {
                Bucket(1, now)
            } else {
                existing.count += 1
                existing
            }
        }!!
        if (bucket.count > limit) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write("{\"error\":\"Rate limit exceeded\"}")
            return
        }
        filterChain.doFilter(request, response)
    }
}
