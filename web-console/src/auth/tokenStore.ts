import type { AuthTokens } from '@/api/types'

const STORAGE_KEY = 'anchorguard.session.tokens'

let tokens: AuthTokens | null = null
let hydrated = false

function hydrate() {
  if (hydrated) return
  hydrated = true
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    tokens = raw ? (JSON.parse(raw) as AuthTokens) : null
  } catch {
    tokens = null
  }
}

export function getTokens(): AuthTokens | null {
  hydrate()
  return tokens
}

export function setTokens(next: AuthTokens | null) {
  hydrate()
  tokens = next
  try {
    if (next) sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next))
    else sessionStorage.removeItem(STORAGE_KEY)
  } catch {
    // ignore storage errors (private mode/quota)
  }
}

export function getAccessToken(): string | null {
  return getTokens()?.accessToken ?? null
}

export function getRefreshToken(): string | null {
  return getTokens()?.refreshToken ?? null
}

export function clearTokens() {
  setTokens(null)
}
