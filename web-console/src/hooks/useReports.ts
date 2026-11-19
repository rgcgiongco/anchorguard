import { useCallback, useState } from 'react'
import { reportsApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import { useToast } from '@/components/ui/Toast'
import type { UsageAggregate } from '@/api/types'

export type UseReportsReturn = {
  usage: UsageAggregate[]
  loading: boolean
  error: string | null
  fetchUsage: (params: { deviceId: string; from?: string; to?: string; packageName?: string }) => Promise<void>
  exportReport: (deviceId: string) => Promise<string | null>
  clearError: () => void
}

export function useReports(): UseReportsReturn {
  const { show } = useToast()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [usage, setUsage] = useState<UsageAggregate[]>([])

  const clearError = useCallback(() => setError(null), [])

  const fetchUsage = useCallback(
    async (params: { deviceId: string; from?: string; to?: string; packageName?: string }) => {
      setLoading(true)
      setError(null)
      try {
        const data = await reportsApi.usage(params)
        setUsage(data)
      } catch (err) {
        const msg = getErrorMessage(err)
        setError(msg)
        show(msg, 'error')
      } finally {
        setLoading(false)
      }
    },
    [show]
  )

  const exportReport = useCallback(async (deviceId: string) => {
    setLoading(true)
    setError(null)
    try {
      const url = reportsApi.exportUrl(deviceId)
      return url
    } catch (err) {
      const msg = getErrorMessage(err)
      setError(msg)
      show(msg, 'error')
      return null
    } finally {
      setLoading(false)
    }
  }, [show])

  return { usage, loading, error, fetchUsage, exportReport, clearError }
}
