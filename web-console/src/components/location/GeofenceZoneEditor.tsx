import { useCallback, useState } from 'react'
import { useCreateGeofenceZone, useDeleteGeofenceZone } from '@/hooks/useLocations'
import type { GeofenceZone } from '@/api/types'

interface GeofenceZoneEditorProps {
  deviceId: string
  zones: GeofenceZone[]
}

export default function GeofenceZoneEditor({ deviceId, zones }: GeofenceZoneEditorProps) {
  const [name, setName] = useState('')
  const [radius, setRadius] = useState(100)
  const [lat, setLat] = useState('')
  const [lng, setLng] = useState('')
  const { createGeofenceZone, creating, error: createError } = useCreateGeofenceZone(deviceId)
  const { deleteGeofenceZone, deleting, error: deleteError } = useDeleteGeofenceZone(deviceId)

  const handleCreate = useCallback(
    async (e: React.FormEvent) => {
      e.preventDefault()
      if (!name.trim()) return
      const latitude = Number(lat)
      const longitude = Number(lng)
      if (!latitude || !longitude) return
      try {
        await createGeofenceZone({
          name: name.trim(),
          latitude,
          longitude,
          radiusMeters: radius
        })
        setName('')
        setLat('')
        setLng('')
      } catch {
        // error shown by hook toast
      }
    },
    [name, lat, lng, radius, createGeofenceZone]
  )

  const handleDelete = useCallback(
    async (zoneId: string) => {
      if (!window.confirm('Delete this geofence zone?')) return
      await deleteGeofenceZone(zoneId)
    },
    [deleteGeofenceZone]
  )

  return (
    <div className="geofence-editor card stack">
      <h2>Geofence zones</h2>
      <div className="zone-list">
        {zones.length === 0 ? (
          <p className="muted">No geofence zones defined yet.</p>
        ) : (
          zones.map((z) => (
            <div key={z.id} className="zone-item row">
              <div>
                <strong>{z.name}</strong>
                <div className="muted" style={{ fontSize: '0.85rem' }}>
                  {z.latitude.toFixed(5)}, {z.longitude.toFixed(5)} · {z.radiusMeters} m
                </div>
              </div>
              <button
                className="btn btn-danger btn-ghost"
                onClick={() => handleDelete(z.id)}
                disabled={deleting}
              >
                Delete
              </button>
            </div>
          ))
        )}
      </div>

      <form onSubmit={handleCreate} className="zone-form row" style={{ gap: 8, marginTop: 12 }}>
        <div className="field" style={{ minWidth: 140 }}>
          <label>Zone name</label>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Home"
          />
        </div>
        <div className="field" style={{ minWidth: 120 }}>
          <label>Latitude</label>
          <input value={lat} onChange={(e) => setLat(e.target.value)} placeholder="lat" />
        </div>
        <div className="field" style={{ minWidth: 120 }}>
          <label>Longitude</label>
          <input value={lng} onChange={(e) => setLng(e.target.value)} placeholder="lng" />
        </div>
        <div className="field" style={{ minWidth: 120 }}>
          <label>Radius (m)</label>
          <input
            type="number"
            value={radius}
            onChange={(e) => setRadius(Math.max(10, Number(e.target.value) || 10))}
            min="10"
          />
        </div>
        <button
          className="btn btn-primary"
          type="submit"
          disabled={creating || !name.trim() || !lat || !lng}
        >
          {creating ? 'Adding…' : 'Add zone'}
        </button>
      </form>

      {createError && <div className="error">{createError}</div>}
      {deleteError && <div className="error">{deleteError}</div>}
    </div>
  )
}
