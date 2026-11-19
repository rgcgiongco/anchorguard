import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { useEffect } from 'react'
import { AuthProvider, useAuth } from '@/context/AuthContext'
import { ToastProvider } from '@/components/ui/Toast'
import { AppShell } from '@/components/layout/AppShell'
import LoginPage from '@/pages/LoginPage'
import DashboardPage from '@/pages/DashboardPage'
import AdminDashboardPage from '@/pages/AdminDashboardPage'
import DevicesPage from '@/pages/DevicesPage'
import DeviceDetailPage from '@/pages/DeviceDetailPage'
import DeviceAppsPage from '@/pages/DeviceAppsPage'
import DeviceSchedulePage from '@/pages/DeviceSchedulePage'
import SecurityPage from '@/pages/SecurityPage'
import ReportsPage from '@/pages/ReportsPage'
import AccountPage from '@/pages/AccountPage'
import UsersPage from '@/pages/UsersPage'

function Loading() {
  return <div className="content-wrap"><div className="card">Loading session…</div></div>
}

function RequireAuth({ children }: { children: JSX.Element }) {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <Loading />
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />
  return children
}

function RedirectIfAuthed({ children }: { children: JSX.Element }) {
  const { user, loading, isAdmin } = useAuth()
  if (loading) return <div className="auth-page"><div className="card auth-card">Loading…</div></div>
  if (user) return <Navigate to={isAdmin ? '/admin' : '/dashboard'} replace />
  return children
}

function HomeRedirect() {
  const { user, loading, isAdmin } = useAuth()
  if (loading) return <Loading />
  if (!user) return <Navigate to="/login" replace />
  return <Navigate to={isAdmin ? '/admin' : '/dashboard'} replace />
}

function RequireAdmin({ children }: { children: JSX.Element }) {
  const { isAdmin } = useAuth()
  if (!isAdmin) return <Navigate to="/dashboard" replace />
  return children
}

function SessionExpiryListener() {
  const navigate = useNavigate()
  const { user } = useAuth()
  useEffect(() => {
    const onExpired = () => {
      if (window.location.pathname !== '/login') navigate('/login', { replace: true })
    }
    window.addEventListener('auth:expired', onExpired)
    return () => window.removeEventListener('auth:expired', onExpired)
  }, [navigate, user])
  return null
}

function AppRoutes() {
  return (
    <>
      <SessionExpiryListener />
      <Routes>
        <Route path="/login" element={<RedirectIfAuthed><LoginPage /></RedirectIfAuthed>} />
        <Route path="/" element={<RequireAuth><AppShell /></RequireAuth>}>
          <Route index element={<HomeRedirect />} />
          <Route path="admin" element={<RequireAdmin><AdminDashboardPage /></RequireAdmin>} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="devices" element={<DevicesPage />} />
          <Route path="devices/:id" element={<DeviceDetailPage />} />
          <Route path="devices/:id/apps" element={<DeviceAppsPage />} />
          <Route path="devices/:id/schedule" element={<DeviceSchedulePage />} />
          <Route path="security" element={<SecurityPage />} />
          <Route path="reports" element={<ReportsPage />} />
          <Route path="account" element={<AccountPage />} />
          <Route path="users" element={<UsersPage />} />
        </Route>
        <Route path="*" element={<HomeRedirect />} />
      </Routes>
    </>
  )
}

export default function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </ToastProvider>
  )
}