import { Link } from 'react-router-dom'
import type { Device } from '@/api/types'

export function DeviceCard(props: { device: Device; onRemove?: (id: string) => void }) {
  const d = props.device
  return (
    <div className="card device-card">
      <div className="device-card-header">
        <div>
          <h3 style={{ margin: 0 }}>{d.name || d.model || d.hexId}</h3>
          <div className="muted" style={{ fontSize: 13 }}>{d.hexId} · {d.manufacturer || 'Unknown manufacturer'}</div>
        </div>
        <span className={`pill ${d.isOnline ? 'pill-ok' : 'pill-warn'}`}>{d.isOnline ? 'Online' : 'Offline'}</span>
      </div>
      <dl className="kv" style={{ gridTemplateColumns: '120px 1fr' }}>
        <dt>Paired</dt><dd>{d.isPaired ? 'Yes' : 'No'}</dd>
        <dt>Last seen</dt><dd>{d.lastSeen ? new Date(d.lastSeen).toLocaleString() : '—'}</dd>
        <dt>App version</dt><dd>{d.appVersion || '—'}</dd>
      </dl>
      <div className="row">
        <Link className="btn btn-primary" to={`/devices/${d.id}`}>Details</Link>
        <Link className="btn btn-secondary" to={`/devices/${d.id}/apps`}>Apps</Link>
        <Link className="btn btn-secondary" to={`/devices/${d.id}/schedule`}>Schedule</Link>
        {props.onRemove && <button className="btn btn-danger" onClick={() => props.onRemove?.(d.id)}>Remove</button>}
      </div>
    </div>
  )
}
