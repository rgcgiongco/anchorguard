import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { adminApi } from '@/api/endpoints'
import type { AdminDashboard as AdminDashboardType, AdminUser, MetricSeries } from '@/api/types'
import { EmptyState } from '@/components/ui/EmptyState'

function fmtBytes(n: number): string {
  if (!n || n < 0) return '—'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let v = n
  let i = 0
  while (v >= 1024 && i < units.length - 1) {
    v /= 1024
    i++
  }
  return `${v.toFixed(v >= 100 ? 0 : 1)} ${units[i]}`
}

function Sparkline({ series, color }: { series: MetricSeries; color: string }) {
  const pts = series.points
  const w = 320
  const h = 64
  const pad = 4
  const max = Math.max(series.max, ...pts.map((p) => p.v), 1)
  const n = pts.length
  if (n === 0) {
    return <svg viewBox={`0 0 ${w} ${h}`} width="100%" height={h} role="img" aria-label={series.label} />
  }
  const x = (i: number) => (n <= 1 ? w / 2 : pad + (i * (w - pad * 2)) / (n - 1))
  const y = (v: number) => h - pad - (v / max) * (h - pad * 2)
  const line = pts.map((p, i) => `${i === 0 ? 'M' : 'L'}${x(i).toFixed(1)},${y(p.v).toFixed(1)}`).join(' ')
  const area = `${line} L${x(n - 1).toFixed(1)},${(h - pad).toFixed(1)} L${x(0).toFixed(1)},${(h - pad).toFixed(1)} Z`
  const id = `grad-${series.key}`
  return (
    <svg viewBox={`0 0 ${w} ${h}`} width="100%" height={h} role="img" aria-label={series.label} preserveAspectRatio="none">
      <defs>
        <linearGradient id={id} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={color} stopOpacity="0.28" />
          <stop offset="100%" stopColor={color} stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={area} fill={`url(#${id})`} />
      <path d={line} fill="none" stroke={color} strokeWidth={2} strokeLinejoin="round" strokeLinecap="round" />
    </svg>
  )
}

function Gauge({ value, max, color, label }: { value: number; max: number; color: string; label: string }) {
  const pct = Math.max(0, Math.min(100, max > 0 ? (value / max) * 100 : 0))
  return (
    <div className="gauge">
      <svg viewBox="0 0 120 120" width="120" height="120" role="img" aria-label={`${label} ${pct.toFixed(0)}%`}>
        <circle cx="60" cy="60" r="48" fill="none" stroke="var(--ag-outline)" strokeWidth="12" />
        <circle
          cx="60" cy="60" r="48" fill="none" stroke={color} strokeWidth="12"
          strokeLinecap="round" strokeDasharray={`${(pct / 100) * 2 * Math.PI * 48} ${2 * Math.PI * 48}`}
          transform="rotate(-90 60 60)"
        />
      </svg>
      <div className="gauge-value">{pct.toFixed(0)}%</div>
    </div>
  )
}

function HealthCard({ series, color, sub }: { series: MetricSeries; color: string; sub: string }) {
  const current = series.points.length ? series.points[series.points.length - 1].v : series.current
  return (
    <div className="card health-card">
      <div className="row" style={{ justifyContent: 'space-between', alignItems: 'baseline' }}>
        <h3 style={{ margin: 0 }}>{series.label}</h3>
        <span className="health-current" style={{ color }}>{current.toFixed(1)}%</span>
      </div>
      <Sparkline series={series} color={color} />
      <p className="muted" style={{ margin: '6px 0 0', fontSize: 12.5 }}>{sub}</p>
    </div>
  )
}

function StatCard({ value, label }: { value: string | number; label: string }) {
  return (
    <div className="card stat">
      <div className="value">{value}</div>
      <div className="label">{label}</div>
    </div>
  )
}

function formatDate(s?: string | null): string {
  if (!s) return '—'
  const d = new Date(s)
  return Number.isNaN(d.getTime()) ? '—' : d.toLocaleString()
}

function UsersTable({ users }: { users: AdminUser[] }) {
  const [sortKey, setSortKey] = useState<'email' | 'role' | 'devices' | 'lastSeen'>('email')
  const [ascending, setAscending] = useState(true)

  const sorted = useMemo(() => {
    const copy = [...users]
    copy.sort((a, b) => {
      let cmp = 0
      if (sortKey === 'email') cmp = a.email.localeCompare(b.email)
      else if (sortKey === 'role') cmp = a.role.localeCompare(b.role)
      else if (sortKey === 'devices') cmp = a.deviceCount - b.deviceCount
      else if (sortKey === 'lastSeen') cmp = (a.lastSeen ?? '').localeCompare(b.lastSeen ?? '')
      return ascending ? cmp : -cmp
    })
    return copy
  }, [users, sortKey, ascending])

  function toggle(key: typeof sortKey) {
    if (key === sortKey) setAscending(!ascending)
    else {
      setSortKey(key)
      setAscending(true)
    }
  }

  if (!users.length) {
    return (
      <EmptyState title="No registered users" description="Users who register through the console will appear here." />
    )
  }

  const sortIndicator = (key: typeof sortKey) => (sortKey === key ? (ascending ? '▲' : '▼') : '')

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            <th><button className="th-sort" onClick={() => toggle('email')}>User {sortIndicator('email')}</button></th>
            <th>Email</th>
            <th><button className="th-sort" onClick={() => toggle('role')}>Role {sortIndicator('role')}</button></th>
            <th><button className="th-sort" onClick={() => toggle('devices')}>Devices {sortIndicator('devices')}</button></th>
            <th>Joined</th>
            <th><button className="th-sort" onClick={() => toggle('lastSeen')}>Last seen {sortIndicator('lastSeen')}</button></th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((u) => (
            <tr key={u.id}>
              <td>{u.name || '—'}</td>
              <td className="mono">{u.email}</td>
              <td>
                <span className={`role-badge role-${u.role}`}>{u.role === 'admin' ? 'Administrator' : 'Parent'}</span>
              </td>
              <td>{u.deviceCount}</td>
              <td>{formatDate(u.createdAt)}</td>
              <td>{formatDate(u.lastSeen)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default function AdminDashboardPage() {
  const dashQ = useQuery({ queryKey: ['admin', 'dashboard'], queryFn: adminApi.dashboard })
  const healthQ = useQuery({ queryKey: ['admin', 'health'], queryFn: adminApi.health, refetchInterval: 5000 })
  const dash = dashQ.data as AdminDashboardType | undefined
  const health = healthQ.data

  const onlineCount = dash?.onlineDevices ?? 0
  const totalDevices = dash?.totalDevices ?? 0

  return (
    <>
      <div className="page-header">
        <div>
          <h1>System Administration</h1>
          <p className="muted" style={{ margin: '4px 0 0' }}>Platform overview, system health, and registered users.</p>
        </div>
      </div>

      {dashQ.isLoading ? (
        <div className="card">Loading dashboard…</div>
      ) : (
        <div className="stat-grid">
          <StatCard value={dash?.totalUsers ?? 0} label="Registered users" />
          <StatCard value={dash?.adminCount ?? 0} label="Administrators" />
          <StatCard value={totalDevices} label="Linked devices" />
          <StatCard value={onlineCount} label="Online devices" />
        </div>
      )}

      <div className="card">
        <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <h2 style={{ margin: 0 }}>System Health</h2>
          {health && (
            <span className={`role-badge role-${health.status === 'ok' ? 'parent' : 'admin'}`}>
              {health.status === 'ok' ? 'Operational' : 'Degraded'}
            </span>
          )}
        </div>

        {healthQ.isLoading ? (
          <p className="muted">Loading metrics…</p>
        ) : health ? (
          <>
            <div className="stat-grid" style={{ marginBottom: 16, gridTemplateColumns: 'repeat(3, minmax(0,1fr))' }}>
              <StatCard value={health.cpu.current.toFixed(1) + '%'} label="CPU" />
              <StatCard value={health.ram.current.toFixed(1) + '%'} label="Memory" />
              <StatCard value={health.storage.current.toFixed(1) + '%'} label="Storage" />
            </div>
            <div className="grid-3" style={{ marginBottom: 16 }}>
              <HealthCard series={health.cpu} color="#2E6F8E" sub="Aggregate CPU utilisation (system load)." />
              <HealthCard series={health.ram} color="#6C5CE7" sub="JVM heap + non-heap memory in use." />
              <HealthCard series={health.storage} color="#E08A3C" sub="Root filesystem usage." />
            </div>
            <div className="grid-3">
              <div className="card health-gauge">
                <Gauge value={health.cpu.current} max={health.cpu.max} color="#2E6F8E" label="CPU" />
                <span className="muted" style={{ fontSize: 13 }}>CPU utilisation</span>
              </div>
              <div className="card health-gauge">
                <Gauge value={health.ram.current} max={health.ram.max} color="#6C5CE7" label="Memory" />
                <span className="muted" style={{ fontSize: 13 }}>Memory in use</span>
              </div>
              <div className="card health-gauge">
                <Gauge value={health.storage.current} max={health.storage.max} color="#E08A3C" label="Storage" />
                <span className="muted" style={{ fontSize: 13 }}>Disk {fmtBytes(health.api.diskTotalBytes - health.api.diskFreeBytes)} used of {fmtBytes(health.api.diskTotalBytes)}</span>
              </div>
            </div>
            <div className="card" style={{ marginTop: 16 }}>
              <h3 style={{ margin: '0 0 8px' }}>API status</h3>
              <dl className="kv">
                <dt>API</dt><dd><span className="status-dot" /> {health.api.status}</dd>
                <dt>Database</dt><dd><span className="status-dot" /> {health.api.db}</dd>
                <dt>Uptime</dt><dd>{Math.floor(health.uptimeSeconds / 3600)}h {Math.floor((health.uptimeSeconds % 3600) / 60)}m</dd>
                <dt>Disk free</dt><dd>{fmtBytes(health.api.diskFreeBytes)} of {fmtBytes(health.api.diskTotalBytes)}</dd>
              </dl>
            </div>
          </>
        ) : (
          <p className="muted">No health data available.</p>
        )}
      </div>

      <div className="card">
        <h2 style={{ margin: '0 0 12px' }}>Registered Users</h2>
        <UsersTable users={dash?.users ?? []} />
      </div>
    </>
  )
}