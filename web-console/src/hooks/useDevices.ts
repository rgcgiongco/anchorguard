import { useCallback, useState } from 'react'
import { useDevices as useDeviceContext } from '@/context/DeviceContext'
import { devicesApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import { useToast } from '@/components/ui/Toast'
import type { Device } from '@/api/types'

export type UseDevicesReturn = {
  devices: Device[]
  loading: boolean
  error: string | null
  refetch: () => void
  registerDevice: () => Promise<void>
  deleteDevice: (id: string) => Promise<void>
  clearError: () => void
}

export function useDevices(): UseDevicesReturn {
  const { devices, refetch } = useDeviceContext()
  const { show } = useToast()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const clearError = useCallback(() => setError(null), [])

  const registerDevice = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      // No dedicated register endpoint exposed by the backend yet;
      // pairing is initiated from the AnchorGuard mobile client.
      throw new Error('Device registration is handled via the mobile app pairing flow.')
    } catch (err) {
      const msg = getErrorMessage(err)
      setError(msg)
      show(msg, 'error')
    } finally {
      setLoading(false)
    }
  }, [show])

  const deleteDevice = useCallback(
    async (id: string) => {
      setLoading(true)
      setError(null)
      try {
        await devicesApi.remove(id)
        void refetch()
      } catch (err) {
        const msg = getErrorMessage(err)
        setError(msg)
        show(msg, 'error')
        throw err
      } finally {
        setLoading(false)
      }
    },
    [refetch, show]
  )

  return { devices, loading, error, refetch, registerDevice, deleteDevice, clearError }
}
