import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { useEffect } from 'react'
import { AuthProvider, useAuth } from '@/context/AuthContext'
import { ToastProvider } from '@/components/ui/Toast'
import { AppShell } from '@/components/layout/AppShell'
import HomePage from '@/pages/Home'
import FeaturesPage from '@/pages/Features'
import ConsolePage from '@/pages/Console'
import LoginPage from '@/pages/Login'
import SignupPage from '@/pages/Signup'
import DashboardPage from '@/pages/Dashboard'
import PricingPage from '@/pages/Pricing'
import AdminDashboardPage from '@/pages/AdminDashboardPage'
import DevicesPage from '@/pages/DevicesPage'
import DeviceDetailPage from '@/pages/DeviceDetailPage'
import DeviceAppsPage from '@/pages/DeviceAppsPage'
import DeviceSchedulePage from '@/pages/DeviceSchedulePage'
import SecurityPage from '@/pages/SecurityPage'
import ReportsPage from '@/pages/ReportsPage'
import AccountPage from '@/pages/AccountPage'
import UsersPage from '@/pages/UsersPage'
import LocationPage from '@/pages/LocationPage'

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
  if (user) return <Navigate to={isAdmin ? '/dashboard/admin' : '/dashboard'} replace />
  return children
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
        <Route path="/" element={<HomePage />} />
        <Route path="/features" element={<FeaturesPage />} />
        <Route path="/console" element={<ConsolePage />} />
        <Route path="/pricing" element={<PricingPage />} />
        <Route path="/login" element={<RedirectIfAuthed><LoginPage /></RedirectIfAuthed>} />
        <Route path="/signup" element={<RedirectIfAuthed><SignupPage /></RedirectIfAuthed>} />
        <Route path="/dashboard" element={<RequireAuth><AppShell /></RequireAuth>}>
          <Route index element={<DashboardPage />} />
          <Route path="admin" element={<RequireAdmin><AdminDashboardPage /></RequireAdmin>} />
          <Route path="devices" element={<DevicesPage />} />
          <Route path="devices/:id" element={<DeviceDetailPage />} />
          <Route path="devices/:id/apps" element={<DeviceAppsPage />} />
          <Route path="devices/:id/schedule" element={<DeviceSchedulePage />} />
          <Route path="security" element={<SecurityPage />} />
          <Route path="reports" element={<ReportsPage />} />
          <Route path="account" element={<AccountPage />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="locations" element={<LocationPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
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
