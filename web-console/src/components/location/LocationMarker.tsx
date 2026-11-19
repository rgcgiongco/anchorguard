import { Marker, Popup, Tooltip } from 'react-leaflet'
import type { LocationPoint } from '@/api/types'

interface LocationMarkerProps {
  position: [number, number]
  location: LocationPoint
  onClick?: () => void
  isSelected?: boolean
}

export default function LocationMarker({
  position,
  location,
  onClick,
  isSelected = false
}: LocationMarkerProps) {
  return (
    <Marker position={position} eventHandlers={{ click: onClick }}>
      <Tooltip
        direction="top"
        offset={[0, -10]}
        permanent={false}
        className="location-tooltip"
      >
        {location.accuracy != null
          ? `${location.latitude.toFixed(4)}, ${location.longitude.toFixed(4)} (±${Math.round(location.accuracy)} m)`
          : `${location.latitude.toFixed(4)}, ${location.longitude.toFixed(4)}`}
      </Tooltip>
      <Popup>
        <div className="location-popup">
          <div className="popup-title">Device location</div>
          <div className="popup-row">
            <span className="popup-label">Latitude</span>
            <span className="popup-value">{location.latitude.toFixed(6)}</span>
          </div>
          <div className="popup-row">
            <span className="popup-label">Longitude</span>
            <span className="popup-value">{location.longitude.toFixed(6)}</span>
          </div>
          {location.accuracy != null && (
            <div className="popup-row">
              <span className="popup-label">Accuracy</span>
              <span className="popup-value">{Math.round(location.accuracy)} m</span>
            </div>
          )}
          <div className="popup-row">
            <span className="popup-label">Recorded</span>
            <span className="popup-value">{new Date(location.timestamp).toLocaleString()}</span>
          </div>
        </div>
      </Popup>
    </Marker>
  )
}
