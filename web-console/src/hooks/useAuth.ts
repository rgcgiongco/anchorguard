import { useCallback, useState } from 'react'
import { useAuth as useAuthContext } from '@/context/AuthContext'
import { getErrorMessage } from '@/api/client'
import { useToast } from '@/components/ui/Toast'

export type UseAuthReturn = {
  login: (email: string, password: string) => Promise<void>
  signup: (payload: { name: string; email: string; password: string }) => Promise<void>
  logout: () => Promise<void>
  loading: boolean
  error: string | null
  clearError: () => void
}

export function useAuth(): UseAuthReturn {
  const { login: ctxLogin, signup: ctxSignup, logout: ctxLogout } = useAuthContext()
  const { show } = useToast()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const clearError = useCallback(() => setError(null), [])

  const login = useCallback(
    async (email: string, password: string) => {
      setLoading(true)
      setError(null)
      try {
        await ctxLogin(email, password)
      } catch (err) {
        const msg = getErrorMessage(err)
        setError(msg)
        show(msg, 'error')
        throw err
      } finally {
        setLoading(false)
      }
    },
    [ctxLogin, show]
  )

  const signup = useCallback(
    async (payload: { name: string; email: string; password: string }) => {
      setLoading(true)
      setError(null)
      try {
        await ctxSignup(payload)
      } catch (err) {
        const msg = getErrorMessage(err)
        setError(msg)
        show(msg, 'error')
        throw err
      } finally {
        setLoading(false)
      }
    },
    [ctxSignup, show]
  )

  const logout = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      await ctxLogout()
    } catch (err) {
      const msg = getErrorMessage(err)
      setError(msg)
      show(msg, 'error')
    } finally {
      setLoading(false)
    }
  }, [ctxLogout, show])

  return { login, signup, logout, loading, error, clearError }
}
