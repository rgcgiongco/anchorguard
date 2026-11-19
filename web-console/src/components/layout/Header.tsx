import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { useDevices } from '@/context/DeviceContext'

export function Header() {
  const { user, logout } = useAuth()
  const { devices, selectedDeviceId, setSelectedDeviceId } = useDevices()
  const navigate = useNavigate()

  return (
    <header className="app-header">
      <div className="row" style={{ gap: 16 }}>
        <button className="btn btn-ghost brand-button" onClick={() => navigate('/dashboard')}>
          AnchorGuard
        </button>
        {user?.role === 'admin' ? (
          <span className="pill" style={{ color: 'var(--ag-primary-dark)', fontWeight: 600 }}>System Administrator</span>
        ) : (
          <div className="field" style={{ minWidth: 220 }}>
            <select
              aria-label="Device switcher"
              value={selectedDeviceId ?? ''}
              onChange={(e) => setSelectedDeviceId(e.target.value || null)}
              disabled={!devices.length}
            >
              {!devices.length && <option value="">No linked devices</option>}
              {devices.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name || d.model || d.hexId} {d.isOnline ? '• Online' : '• Offline'}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>
      <div className="row">
        {user?.role !== 'admin' && (
          <button className="btn btn-ghost" title="Notifications" aria-label="Notifications">Alerts</button>
        )}
        <div className="row" style={{ gap: 8 }}>
          <span className="muted" style={{ fontSize: 14 }}>{user?.email}</span>
          <button className="btn btn-secondary" onClick={() => { void logout() }}>Sign out</button>
        </div>
      </div>
    </header>
  )
}
