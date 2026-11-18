import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { devicesApi } from '@/api/endpoints'
import { useDevices } from '@/context/DeviceContext'
import { EmptyState } from '@/components/ui/EmptyState'

export default function DashboardPage() {
  const { devices, selectedDevice } = useDevices()
  const q = useQuery({ queryKey: ['devices'], queryFn: devicesApi.list })
  const list = q.data ?? devices
  const online = list.filter((d) => d.isOnline).length
  const paired = list.filter((d) => d.isPaired).length

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p className="muted" style={{ margin: '4px 0 0' }}>Overview of linked devices and quick actions.</p>
        </div>
        <Link className="btn btn-secondary" to="/devices">Manage devices</Link>
      </div>

      <div className="stat-grid">
        <div className="card stat"><div className="value">{list.length}</div><div className="label">Linked devices</div></div>
        <div className="card stat"><div className="value">{online}</div><div className="label">Online now</div></div>
        <div className="card stat"><div className="value">{paired}</div><div className="label">Paired</div></div>
        <div className="card stat"><div className="value">—</div><div className="label">Subscription</div></div>
      </div>

      <div className="card">
        <h2>Selected device</h2>
        {!selectedDevice ? (
          <EmptyState
            title="No device selected"
            description="Link a child device from the AnchorGuard app to see status and manage controls."
            action={<Link className="btn btn-primary" to="/devices">View devices</Link>}
          />
        ) : (
          <div className="stack">
            <dl className="kv">
              <dt>Name</dt><dd>{selectedDevice.name || selectedDevice.hexId}</dd>
              <dt>Model</dt><dd>{selectedDevice.model || '—'}</dd>
              <dt>Status</dt><dd><span className={`pill ${selectedDevice.isOnline ? 'pill-ok' : 'pill-warn'}`}>{selectedDevice.isOnline ? 'Online' : 'Offline'}</span></dd>
              <dt>Last seen</dt><dd>{selectedDevice.lastSeen ? new Date(selectedDevice.lastSeen).toLocaleString() : '—'}</dd>
            </dl>
            <div className="row">
              <Link className="btn btn-primary" to={`/devices/${selectedDevice.id}`}>Open device details</Link>
              <Link className="btn btn-secondary" to={`/devices/${selectedDevice.id}/apps`}>App policy</Link>
              <Link className="btn btn-secondary" to={`/devices/${selectedDevice.id}/schedule`}>Screen time</Link>
            </div>
          </div>
        )}
      </div>

      <div className="card">
        <h2>Quick actions</h2>
        <div className="row">
          <Link className="btn btn-secondary" to="/devices">Device list</Link>
          <Link className="btn btn-secondary" to="/reports">Usage reports</Link>
          <Link className="btn btn-secondary" to="/security">Security</Link>
          <Link className="btn btn-secondary" to="/account">Account</Link>
        </div>
        <p className="muted" style={{ marginBottom: 0, marginTop: 12 }}>User profile management is available from navigation when backend endpoints are enabled.</p>
      </div>
    </>
  )
}
