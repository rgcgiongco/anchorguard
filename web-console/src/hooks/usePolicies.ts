import { useCallback, useState } from 'react'
import { policyApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import { useToast } from '@/components/ui/Toast'
import type { DevicePolicy, DeviceSettings, AppPolicy } from '@/api/types'

export type UsePoliciesReturn = {
  policy: DevicePolicy | null
  loading: boolean
  error: string | null
  fetchPolicy: (deviceId: string) => Promise<void>
  updateSettings: (deviceId: string, settings: Partial<DeviceSettings>) => Promise<void>
  updateAppBlock: (deviceId: string, packageName: string, isAllowed: boolean) => Promise<void>
  updateScreenTime: (deviceId: string, packageName: string, dailyLimitMinutes: number) => Promise<void>
  clearError: () => void
}

export function usePolicies(): UsePoliciesReturn {
  const { show } = useToast()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [policy, setPolicy] = useState<DevicePolicy | null>(null)

  const clearError = useCallback(() => setError(null), [])

  const fetchPolicy = useCallback(async (deviceId: string) => {
    setLoading(true)
    setError(null)
    try {
      const data = await policyApi.get(deviceId)
      setPolicy(data)
    } catch (err) {
      const msg = getErrorMessage(err)
      setError(msg)
      show(msg, 'error')
    } finally {
      setLoading(false)
    }
  }, [show])

  const updateSettings = useCallback(
    async (deviceId: string, settings: Partial<DeviceSettings>) => {
      setLoading(true)
      setError(null)
      try {
        await policyApi.patch(deviceId, { settings })
        await fetchPolicy(deviceId)
      } catch (err) {
        const msg = getErrorMessage(err)
        setError(msg)
        show(msg, 'error')
        throw err
      } finally {
        setLoading(false)
      }
    },
    [fetchPolicy, show]
  )

  const updateAppBlock = useCallback(
    async (deviceId: string, packageName: string, isAllowed: boolean) => {
      setLoading(true)
      setError(null)
      try {
        await policyApi.patchApp(deviceId, packageName, { isAllowed })
        await fetchPolicy(deviceId)
      } catch (err) {
        const msg = getErrorMessage(err)
        setError(msg)
        show(msg, 'error')
        throw err
      } finally {
        setLoading(false)
      }
    },
    [fetchPolicy, show]
  )

  const updateScreenTime = useCallback(
    async (deviceId: string, packageName: string, dailyLimitMinutes: number) => {
      setLoading(true)
      setError(null)
      try {
        await policyApi.patchApp(deviceId, packageName, { dailyLimitMinutes })
        await fetchPolicy(deviceId)
      } catch (err) {
        const msg = getErrorMessage(err)
        setError(msg)
        show(msg, 'error')
        throw err
      } finally {
        setLoading(false)
      }
    },
    [fetchPolicy, show]
  )

  return { policy, loading, error, fetchPolicy, updateSettings, updateAppBlock, updateScreenTime, clearError }
}
