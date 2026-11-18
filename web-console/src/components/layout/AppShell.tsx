import { Outlet } from 'react-router-dom'
import { Header } from './Header'
import { Sidebar } from './Sidebar'
import { Footer } from './Footer'
import { DeviceProvider } from '@/context/DeviceContext'

export function AppShell() {
  return (
    <DeviceProvider>
      <div className="app-shell">
        <Header />
        <div className="shell-body">
          <Sidebar />
          <main className="content-wrap">
            <div className="content-canvas">
              <Outlet />
            </div>
          </main>
        </div>
        <Footer />
        <nav className="mobile-nav" aria-label="Primary">
          <a href="/dashboard">Dashboard</a>
          <a href="/devices">Devices</a>
          <a href="/reports">Reports</a>
          <a href="/account">Account</a>
        </nav>
      </div>
    </DeviceProvider>
  )
}
