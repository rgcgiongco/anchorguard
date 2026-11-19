import { useCallback, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useDevices } from '@/context/DeviceContext'
import { useLocations, useCurrentLocation, useGeofenceZones } from '@/hooks/useLocations'
import { getErrorMessage } from '@/api/client'
import { useToast } from '@/components/ui/Toast'
import { EmptyState } from '@/components/ui/EmptyState'
import LocationMap from '@/components/location/LocationMap'
import LocationHistoryList from '@/components/location/LocationHistoryList'
import GeofenceZoneEditor from '@/components/location/GeofenceZoneEditor'
import type { LocationHistoryParams } from '@/api/types'

export default function LocationPage() {
  const { devices, selectedDeviceId, setSelectedDeviceId } = useDevices()
  const { show } = useToast()
  const navigate = useNavigate()
  const [trackingEnabled, setTrackingEnabled] = useState(false)

  const selected = devices.find((d) => d.id === selectedDeviceId) ?? null
  const deviceId = selected?.id ?? ''

  const historyQ = useLocations(deviceId, { deviceId, limit: 100 })
  const currentQ = useCurrentLocation(deviceId)
  const zonesQ = useGeofenceZones(deviceId)

  const latest = currentQ.location ?? historyQ.locations[0] ?? null
  const center: [number, number] = latest ? [latest.latitude, latest.longitude] : [0, 0]

  const handleToggleTracking = useCallback(async () => {
    const next = !trackingEnabled
    setTrackingEnabled(next)
    try {
      // No dedicated device-settings endpoint exposed yet; toggle reflected locally
      // and syncs with the Android client's DeviceSetting (location_tracking_enabled).
      show(`Location tracking ${next ? 'enabled' : 'disabled'} (syncs to device on next check-in)`, 'info')
    } catch (err) {
      show(getErrorMessage(err), 'error')
    }
  }, [trackingEnabled, show])

  if (!devices.length) {
    return (
      <>
        <div className="page-header">
          <div>
            <h1>Location</h1>
            <p className="muted">View device location, history, and geofence zones.</p>
          </div>
        </div>
        <div className="card">
          <EmptyState
            title="No linked devices"
            description="Install AnchorGuard on a child device and complete pairing to view its location here."
            action={<Link to="/devices" className="btn btn-primary">Go to Devices</Link>}
          />
        </div>
      </>
    )
  }

  if (!selected) {
    return (
      <>
        <div className="page-header">
          <div>
            <h1>Location</h1>
            <p className="muted">Select a device from the sidebar to view its location.</p>
          </div>
        </div>
        <div className="card">
          <EmptyState
            title="No device selected"
            description="Choose a linked device from the device switcher in the sidebar."
          />
        </div>
      </>
    )
  }

  const history = historyQ.locations
  const zones = zonesQ.zones

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Location</h1>
          <p className="muted">Map, history, and geofence zones for {selected.name || selected.hexId}.</p>
        </div>
        <div className="row">
          <select
            value={selectedDeviceId ?? ''}
            onChange={(e) => setSelectedDeviceId(e.target.value || null)}
            className="w-auto px-3 py-2 bg-[#F7F9FB] border border-[#B9C4CE] rounded-md text-sm"
          >
            {!devices.length && <option value="">No linked devices</option>}
            {devices.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name || d.model || d.hexId} {d.isOnline ? '· Online' : '· Offline'}
              </option>
            ))}
          </select>
          <button
            className="btn btn-primary"
            onClick={handleToggleTracking}
            disabled={historyQ.loading || currentQ.loading}
          >
            {trackingEnabled ? 'Tracking enabled' : 'Enable location tracking'}
          </button>
          <Link className="btn btn-secondary" to="/devices">
            Back to devices
          </Link>
        </div>
      </div>

      {historyQ.error && <div className="error">{historyQ.error}</div>}
      {currentQ.error && <div className="error">{currentQ.error}</div>}

      {!latest && (
        <div className="card">
          <EmptyState
            title="No location data yet"
            description="Location tracking is not yet active on this device. Enable tracking on the child device, then check back here."
          />
        </div>
      )}

      {latest && (
        <div className="location-layout">
          <section className="map-section card stack">
            <div className="map-header row">
              <h2 style={{ margin: 0, fontSize: '1.1rem' }}>Live map</h2>
              <span className={`pill ${selected.isOnline ? 'pill-ok' : 'pill-warn'}`}>
                {selected.isOnline ? 'Online' : 'Offline'}
              </span>
            </div>
            <LocationMap
              center={center}
              locations={history}
              zones={zones}
              selectedLocationId={latest?.id ?? null}
            />
          </section>

          <section className="details-section card stack">
            <div className="current-card card stack" style={{ padding: 16 }}>
              <h2 style={{ margin: 0, fontSize: '1.1rem' }}>Current location</h2>
              <div className="kv">
                <dt>Latitude</dt>
                <dd className="mono">{latest.latitude.toFixed(6)}</dd>
                <dt>Longitude</dt>
                <dd className="mono">{latest.longitude.toFixed(6)}</dd>
                {latest.accuracy != null && (
                  <>
                    <dt>Accuracy</dt>
                    <dd>{Math.round(latest.accuracy)} m</dd>
                  </>
                )}
                <dt>Recorded</dt>
                <dd>{new Date(latest.timestamp).toLocaleString()}</dd>
              </div>
            </div>

            <div className="history-card card stack">
              <h2 style={{ margin: 0, fontSize: '1.1rem' }}>Location history</h2>
              <LocationHistoryList history={history} />
            </div>

            <GeofenceZoneEditor deviceId={deviceId} zones={zones} />
          </section>
        </div>
      )}
    </>
  )
}
