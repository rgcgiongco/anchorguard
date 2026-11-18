import { NavLink, useNavigate } from 'react-router-dom'
import { useDevices } from '@/context/DeviceContext'

const base = 'nav-item'

function NavItem({ to, label, icon }: { to: string; label: string; icon: string }) {
  return (
    <NavLink to={to} className={({ isActive }) => `${base}${isActive ? ' active' : ''}`} title={label}>
      <span className="nav-icon" aria-hidden>{icon}</span>
      <span className="nav-label">{label}</span>
    </NavLink>
  )
}

export function Sidebar() {
  const { selectedDeviceId } = useDevices()
  const navigate = useNavigate()
  const screenTimeTo = selectedDeviceId ? `/devices/${selectedDeviceId}/schedule` : '/devices'

  return (
    <aside className="sidebar">
      <nav className="sidebar-nav" aria-label="Primary">
        <NavItem to="/dashboard" label="Dashboard" icon="▣" />
        <NavItem to="/devices" label="Devices" icon="▤" />
        <NavItem to="/users" label="Users" icon="◍" />
        <button type="button" className="nav-item nav-button" onClick={() => navigate(screenTimeTo)} title="Screen Time">
          <span className="nav-icon" aria-hidden>◷</span>
          <span className="nav-label">Screen Time</span>
        </button>
        <NavItem to="/security" label="Security" icon="◈" />
        <NavItem to="/reports" label="Reports" icon="▥" />
        <NavItem to="/account" label="Account" icon="◉" />
      </nav>
    </aside>
  )
}
