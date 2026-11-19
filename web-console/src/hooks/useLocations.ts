import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { locationsApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import { useToast } from '@/components/ui/Toast'
import type { GeofenceZone, LocationHistoryParams, LocationPoint } from '@/api/types'

export type UseLocationsReturn = {
  locations: LocationPoint[]
  loading: boolean
  error: string | null
  refetch: () => void
}

export function useLocations(
  deviceId: string,
  params?: LocationHistoryParams
): UseLocationsReturn {
  const query = useQuery({
    queryKey: ['locations', deviceId, params],
    queryFn: () => locationsApi.getLocationHistory(deviceId, params),
    enabled: !!deviceId
  })
  return {
    locations: query.data ?? [],
    loading: query.isLoading,
    error: query.error ? getErrorMessage(query.error) : null,
    refetch: query.refetch
  }
}

export type UseCurrentLocationReturn = {
  location: LocationPoint | null
  loading: boolean
  error: string | null
}

export function useCurrentLocation(deviceId: string): UseCurrentLocationReturn {
  const query = useQuery({
    queryKey: ['locations', deviceId, 'current'],
    queryFn: () => locationsApi.getCurrentLocation(deviceId),
    enabled: !!deviceId
  })
  return {
    location: query.data ?? null,
    loading: query.isLoading,
    error: query.error ? getErrorMessage(query.error) : null
  }
}

export type UseGeofenceZonesReturn = {
  zones: GeofenceZone[]
  loading: boolean
  error: string | null
}

export function useGeofenceZones(deviceId: string): UseGeofenceZonesReturn {
  const query = useQuery({
    queryKey: ['geofences', deviceId],
    queryFn: () => locationsApi.getGeofenceZones(deviceId),
    enabled: !!deviceId
  })
  return {
    zones: query.data ?? [],
    loading: query.isLoading,
    error: query.error ? getErrorMessage(query.error) : null
  }
}

export type UseCreateGeofenceZoneReturn = {
  createGeofenceZone: (
    payload: { name: string; latitude: number; longitude: number; radiusMeters: number }
  ) => Promise<GeofenceZone>
  creating: boolean
  error: string | null
}

export function useCreateGeofenceZone(deviceId: string): UseCreateGeofenceZoneReturn {
  const { show } = useToast()
  const qc = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const mutation = useMutation({
    mutationFn: (payload: { name: string; latitude: number; longitude: number; radiusMeters: number }) =>
      locationsApi.createGeofenceZone(deviceId, payload),
    onSuccess: () => {
      setError(null)
      void qc.invalidateQueries({ queryKey: ['geofences', deviceId] })
    },
    onError: (err) => {
      const msg = getErrorMessage(err)
      setError(msg)
      show(msg, 'error')
    }
  })
  return {
    createGeofenceZone: (payload) => mutation.mutateAsync(payload),
    creating: mutation.isPending,
    error
  }
}

export type UseDeleteGeofenceZoneReturn = {
  deleteGeofenceZone: (zoneId: string) => Promise<void>
  deleting: boolean
  error: string | null
}

export function useDeleteGeofenceZone(deviceId: string): UseDeleteGeofenceZoneReturn {
  const { show } = useToast()
  const qc = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const mutation = useMutation({
    mutationFn: (zoneId: string) => locationsApi.deleteGeofenceZone(zoneId),
    onSuccess: () => {
      setError(null)
      void qc.invalidateQueries({ queryKey: ['geofences', deviceId] })
    },
    onError: (err) => {
      const msg = getErrorMessage(err)
      setError(msg)
      show(msg, 'error')
    }
  })
  return {
    deleteGeofenceZone: async (zoneId) => {
      await mutation.mutateAsync(zoneId)
    },
    deleting: mutation.isPending,
    error
  }
}
