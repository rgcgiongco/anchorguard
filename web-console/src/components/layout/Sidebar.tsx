import { NavLink } from 'react-router-dom'
import { useDevices } from '@/context/DeviceContext'
import { useAuth } from '@/context/AuthContext'

const base = 'nav-item'

const SIDEBAR_ITEMS = [
  { to: '.', label: 'Dashboard', icon: '▣' },
  { to: 'devices', label: 'Devices', icon: '▤' },
  { to: 'devices', label: 'App Blocker', icon: '🚫' },
  { to: 'devices', label: 'Screen Time', icon: '◷' },
  { to: 'locations', label: 'Location', icon: '📍' },
  { to: 'reports', label: 'Reports', icon: '▥' },
  { to: 'account', label: 'Settings', icon: '◉' },
]

function NavItem({ to, label, icon }: { to: string; label: string; icon: string }) {
  return (
    <NavLink to={to} className={({ isActive }) => `${base}${isActive ? ' active' : ''}`} title={label}>
      <span className="nav-icon" aria-hidden>{icon}</span>
      <span className="nav-label">{label}</span>
    </NavLink>
  )
}

export function Sidebar() {
  const { devices, selectedDeviceId, setSelectedDeviceId } = useDevices()
  const { user, logout } = useAuth()

  return (
    <aside className="sidebar">
      <div className="p-3 mb-4 border-b border-[#B9C4CE]">
        <label className="block text-[10px] font-semibold text-[#5B6B7C] uppercase tracking-wider mb-2">Linked device</label>
        <select
          aria-label="Device switcher"
          value={selectedDeviceId ?? ''}
          onChange={(e) => setSelectedDeviceId(e.target.value || null)}
          disabled={!devices.length}
          className="w-full px-3 py-2 bg-[#F7F9FB] border border-[#B9C4CE] rounded-md text-sm text-[#1C2530] focus:border-[#2E6F8E] focus:outline-none"
        >
          {!devices.length && <option value="">No linked devices</option>}
          {devices.map((d) => (
            <option key={d.id} value={d.id}>
              {d.name || d.model || d.hexId} {d.isOnline ? '· Online' : '· Offline'}
            </option>
          ))}
        </select>
      </div>

      <nav className="sidebar-nav" aria-label="Primary navigation">
        {SIDEBAR_ITEMS.map((item) => (
          <NavItem key={item.label} to={item.to} label={item.label} icon={item.icon} />
        ))}
      </nav>

      <div className="mt-6 p-3 border-t border-[#B9C4CE]">
        <div className="flex items-center gap-2 mb-3">
          <div className="w-8 h-8 rounded-full bg-[#2E6F8E] flex items-center justify-center text-white text-[10px] font-bold">
            {user?.email?.charAt(0)?.toUpperCase() ?? 'P'}
          </div>
          <div className="overflow-hidden">
            <p className="text-sm font-medium text-[#1C2530] truncate">{user?.email}</p>
            <p className="text-[10px] text-[#5B6B7C]">Parent · {devices.length} device{devices.length === 1 ? '' : 's'}</p>
          </div>
        </div>
        <button
          className="w-full px-3 py-2 text-left text-sm font-medium text-[#C0504D] hover:bg-red-50 rounded-md transition-colors"
          onClick={() => { void logout() }}
        >
          Sign out
        </button>
      </div>
    </aside>
  )
}
