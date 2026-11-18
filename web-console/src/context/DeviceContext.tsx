import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { devicesApi } from '@/api/endpoints'
import type { Device } from '@/api/types'
import { useAuth } from './AuthContext'

type DeviceCtx = {
  devices: Device[]
  selectedDeviceId: string | null
  setSelectedDeviceId: (id: string | null) => void
  selectedDevice: Device | null
  loading: boolean
  refetch: () => void
}

const Ctx = createContext<DeviceCtx | undefined>(undefined)

export function DeviceProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  const [selectedDeviceId, setSelectedDeviceId] = useState<string | null>(null)
  const q = useQuery({
    queryKey: ['devices'],
    queryFn: devicesApi.list,
    enabled: !!user
  })
  const devices = q.data ?? []

  useEffect(() => {
    if (!devices.length) {
      if (selectedDeviceId !== null) setSelectedDeviceId(null)
      return
    }
    if (!selectedDeviceId || !devices.some((d) => d.id === selectedDeviceId)) {
      setSelectedDeviceId(devices[0].id)
    }
  }, [devices, selectedDeviceId])

  const value = useMemo<DeviceCtx>(() => ({
    devices,
    selectedDeviceId,
    setSelectedDeviceId,
    selectedDevice: devices.find((d) => d.id === selectedDeviceId) ?? null,
    loading: q.isLoading,
    refetch: () => { void q.refetch() }
  }), [devices, selectedDeviceId, q])

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>
}

export function useDevices() {
  const ctx = useContext(Ctx)
  if (!ctx) throw new Error('useDevices must be used within DeviceProvider')
  return ctx
}
