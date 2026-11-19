import { MapContainer, TileLayer, Polyline, Circle } from 'react-leaflet'
import LocationMarker from './LocationMarker'
import type { LocationPoint } from '@/api/types'
import type { GeofenceZone } from '@/api/types'

interface LocationMapProps {
  center: [number, number]
  locations: LocationPoint[]
  zones: GeofenceZone[]
  onLocationClick?: (loc: LocationPoint) => void
  selectedLocationId?: string | null
}

export default function LocationMap({
  center,
  locations,
  zones,
  onLocationClick,
  selectedLocationId
}: LocationMapProps) {
  const latest = locations.length ? locations[0] : null

  const ordered = locations
    .slice()
    .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())

  if (!latest) {
    return (
      <div className="map-empty">
        <div className="map-placeholder">No location data available</div>
      </div>
    )
  }

  const path = ordered.map((p) => [p.latitude, p.longitude] as [number, number])

  return (
    <div className="location-map-wrap">
      <MapContainer
        center={[latest.latitude, latest.longitude]}
        zoom={14}
        style={{ height: '100%', width: '100%', borderRadius: 12, overflow: 'hidden', border: '1px solid var(--ag-outline)' }}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://openstreetmap.org">OpenStreetMap</a> contributors'
        />
        {path.length > 1 && (
          <Polyline positions={path} color="#1E3A8A" weight={3} opacity={0.6} />
        )}
        {zones.map((z) => (
          <Circle
            key={z.id}
            center={[z.latitude, z.longitude]}
            radius={z.radiusMeters}
            color="#F97316"
            fillColor="#F97316"
            fillOpacity={0.15}
            weight={2}
          />
        ))}
        {ordered.map((loc) => (
          <LocationMarker
            key={loc.id}
            position={[loc.latitude, loc.longitude]}
            location={loc}
            isSelected={selectedLocationId === loc.id}
            onClick={() => onLocationClick?.(loc)}
          />
        ))}
      </MapContainer>
    </div>
  )
}
