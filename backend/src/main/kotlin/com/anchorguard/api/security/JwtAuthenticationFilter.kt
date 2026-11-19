package com.anchorguard.api.security

import com.anchorguard.api.auth.ParentRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val parents: ParentRepository
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ").trim()
            val claims = jwtService.parse(token)
            if (claims != null && jwtService.isAccessToken(claims)) {
                val parentId = UUID.fromString(claims.subject)
                val parent = parents.findById(parentId).orElse(null)
                if (parent != null) {
                    val role = claims["role"] as? String ?: parent.roleName
                    val authority = if (role == "admin") "ROLE_ADMIN" else "ROLE_PARENT"
                    val principal = AuthPrincipal(parent.id, parent.email, role)
                    val auth = UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        listOf(SimpleGrantedAuthority(authority))
                    )
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}

data class AuthPrincipal(val parentId: UUID, val email: String, val role: String = "parent") {
    val isAdmin: Boolean get() = role == "admin"
}