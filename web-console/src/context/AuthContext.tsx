import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { accountApi, authApi } from '@/api/endpoints'
import { clearTokens, getRefreshToken, getTokens, setTokens } from '@/auth/tokenStore'
import type { AuthTokens, Parent } from '@/api/types'

type AuthState = {
  user: Parent | null
  loading: boolean
  login: (email: string, password: string) => Promise<AuthTokens>
  signup: (payload: { name: string; email: string; password: string }) => Promise<AuthTokens>
  logout: () => Promise<void>
  refreshMe: () => Promise<void>
  setSession: (tokens: AuthTokens, user?: Parent | null) => void
  isAdmin: boolean
}

const AuthContext = createContext<AuthState | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const qc = useQueryClient()
  const [user, setUser] = useState<Parent | null>(null)
  const [loading, setLoading] = useState(true)

  async function refreshMe() {
    const me = await accountApi.me()
    setUser(me)
  }

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        if (getTokens()) {
          const me = await accountApi.me()
          if (!cancelled) setUser(me)
        }
      } catch {
        clearTokens()
        if (!cancelled) setUser(null)
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    const onExpired = () => {
      clearTokens()
      setUser(null)
      qc.clear()
    }
    window.addEventListener('auth:expired', onExpired)
    return () => {
      cancelled = true
      window.removeEventListener('auth:expired', onExpired)
    }
  }, [qc])

  const value = useMemo<AuthState>(() => ({
    user,
    loading,
    login: async (email, password): Promise<AuthTokens> => {
      const tokens = await authApi.login(email, password)
      setTokens(tokens)
      const me = await accountApi.me()
      setUser(me)
      return tokens
    },
    signup: async ({ name, email, password }): Promise<AuthTokens> => {
      const tokens = await authApi.signup({ name, email, password })
      setTokens(tokens)
      const me = await accountApi.me()
      setUser(me)
      return tokens
    },
    logout: async () => {
      const rt = getRefreshToken()
      try {
        if (rt) await authApi.logout(rt)
      } catch {
        // ignore server logout failure; clear local session anyway
      }
      clearTokens()
      setUser(null)
      qc.clear()
    },
    refreshMe,
    setSession: (t, u = null) => {
      setTokens(t)
      setUser(u)
    },
    isAdmin: user?.role === 'admin'
  }), [user, loading, qc])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
