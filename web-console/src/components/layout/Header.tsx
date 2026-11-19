import { Link } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'

export function Header() {
  const { user, logout } = useAuth()

  return (
    <header className="app-header">
      <div className="row" style={{ gap: 16 }}>
        <Link to="/dashboard" className="btn btn-ghost brand-button">
          AnchorGuard
        </Link>
        <span className="hidden md:inline px-3 py-1.5 rounded-md bg-[#1E3A8A]/10 text-[#1E3A8A] text-xs font-medium border border-[#1E3A8A]/20">
          Parent Console
        </span>
      </div>
      <div className="row">
        <button className="btn btn-ghost hidden sm:inline" title="Notifications" aria-label="Notifications">
          Alerts
        </button>
        <div className="row" style={{ gap: 8 }}>
          <span className="hidden sm:inline muted" style={{ fontSize: 14 }}>{user?.email}</span>
          <button className="btn btn-secondary hidden sm:inline" onClick={() => { void logout() }} title="Sign out">
            Sign out
          </button>
        </div>
      </div>
    </header>
  )
}
