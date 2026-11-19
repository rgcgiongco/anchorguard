import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { devicesApi, reportsApi, policyApi } from '@/api/endpoints'
import { useDevices } from '@/context/DeviceContext'
import { useAuth } from '@/context/AuthContext'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import type { Device } from '@/api/types'

const APP_CATEGORIES = [
  { key: 'social', label: 'Social Apps', color: 'bg-[#1E3A8A]', toggleColor: '#1E3A8A' },
  { key: 'chat', label: 'Chat', color: 'bg-[#2E6F8E]', toggleColor: '#2E6F8E' },
  { key: 'gaming', label: 'Gaming', color: 'bg-[#F97316]', toggleColor: '#F97316' },
  { key: 'learning', label: 'Learning', color: 'bg-[#4CAF7A]', toggleColor: '#4CAF7A' },
]

const DEVICE_ICONS = {
  tablet: tabletIcon,
  phone: phoneIcon,
  ipad: ipadIcon,
}

export default function DashboardPage() {
  const { user } = useAuth()
  const { devices, refetch } = useDevices()
  const [blocking, setBlocking] = useState<Record<string, boolean>>({
    social: true,
    chat: false,
    gaming: true,
    learning: false,
  })

  const q = useQuery({
    queryKey: ['devices'],
    queryFn: devicesApi.list,
    enabled: !!user,
  })
  const list = q.data ?? devices
  const online = list.filter((d) => d.isOnline).length

  const reportsQ = useQuery({
    queryKey: ['reports', 'usage'],
    queryFn: () =>
      reportsApi
        .usage({ deviceId: '', from: '', to: '' })
        .then((d) => d)
        .catch(() => [] as never[]),
    enabled: !!user && list.length > 0,
  })
  const usage = (reportsQ.data ?? []) as unknown as Array<{ deviceId: string; packageName?: string | null; eventCount: number; from: string; to: string }>

  const policyQ = useQuery({
    queryKey: ['policy', list[0]?.id ?? 'none'],
    queryFn: () => (list[0] ? policyApi.get(list[0].id).then((p) => p.apps) : Promise.resolve([] as never[])),
    enabled: !!list.length,
  })
  const appsPolicy = (policyQ.data ?? []) as Array<{ packageName: string; appName?: string | null; isAllowed: boolean }>

  const handleToggle = (key: string) => {
    setBlocking((prev: Record<string, boolean>) => ({ ...prev, [key]: !prev[key] }))
  }

  const handleRefetch = () => {
    void refetch()
  }

  return (
    <div className="space-y-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[#1E3A8A]">Dashboard</h1>
          <p className="text-[#5B6B7C] text-sm">Overview of your family's digital safety and device activity.</p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="sm" onClick={handleRefetch} className="text-[#1E3A8A]">
            Refresh
          </Button>
        </div>
      </header>

      {/* Stat cards row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-2">
        <StatCard
          label="Active Devices"
          value={list.length}
          sub={online + ' online'}
          color="bg-[#1E3A8A]/10 text-[#1E3A8A]"
          Icon={devicesIcon}
        />
        <StatCard
          label="Today's Screen Time"
          value="3h 12m"
          sub="+12 min vs yesterday"
          color="bg-[#2E6F8E]/10 text-[#2E6F8E]"
          Icon={screentimeIcon}
        />
        <StatCard
          label="Blocked Attempts"
          value="14"
          sub="Last 24 hours"
          color="bg-[#F97316]/10 text-[#F97316]"
          Icon={blockerIcon}
        />
        <StatCard
          label="Current Status"
          value="Secured"
          sub="All devices protected"
          color="bg-[#4CAF7A]/10 text-[#4CAF7A]"
          Icon={shieldIcon}
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left column: App Blocking + Device Status */}
        <div className="lg:col-span-7 space-y-6">
          <AppBlockingControls categories={APP_CATEGORIES} blocking={blocking} onToggle={handleToggle} appsPolicy={appsPolicy} />
          <DeviceStatus devices={list} />
        </div>

        {/* Right column: Charts */}
        <div className="lg:col-span-5 space-y-6">
          <ScreenTimeChart usage={usage} />
          <AppUsageChart usage={usage} />
        </div>
      </div>

      {/* Activity Log (full width) */}
      <ActivityLog devices={list} appsPolicy={appsPolicy} />
    </div>
  )
}

/* ---------- Shared icon SVGs ---------- */
function dashboardIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
    </svg>
  )
}
function devicesIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
  )
}
function blockerIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  )
}
function screentimeIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  )
}
function locationIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
  )
}
function reportsIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
    </svg>
  )
}
function settingsIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
  )
}
function shieldIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
    </svg>
  )
}
function tabletIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <rect width="18" height="12" x="3" y="6" stroke="currentColor" strokeWidth={2} rx={2} />
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17v-2m3 2v-4m3 4v-2m2-4h.01" />
    </svg>
  )
}
function phoneIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <rect width="6" height="12" x="9" y="6" stroke="currentColor" strokeWidth={2} rx={2} />
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 12h.01M12 12h.01" />
    </svg>
  )
}
function ipadIcon() {
  return (
    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <rect width="16" height="22" x="4" y="1" stroke="currentColor" strokeWidth={2} rx={2} />
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3v1m6.364 1.636l-.707-.707M12 20v1m-6.364-1.636l.707-.707M16 12.364l-.707.707M7.636 12.364l.707.707" />
    </svg>
  )
}

/* ---------- Stat Card ---------- */
function StatCard({ label, value, sub, color, Icon }: { label: string; value: string | number; sub: string; color: string; Icon: () => JSX.Element }) {
  return (
    <Card className="p-4">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-medium text-[#5B6B7C] uppercase tracking-wider">{label}</p>
          <div className="flex items-baseline gap-1 mt-1">
            <span className="text-2xl font-bold text-[#1C2530]">{value}</span>
            <span className="text-sm text-[#5B6B7C]">{sub}</span>
          </div>
        </div>
        <div className={`w-10 h-10 rounded-lg ${color} flex items-center justify-center`}>
          <Icon />
        </div>
      </div>
    </Card>
  )
}

/* ---------- App Blocking & Controls ---------- */
function AppBlockingControls({ categories, blocking, onToggle, appsPolicy }: { categories: typeof APP_CATEGORIES; blocking: Record<string, boolean>; onToggle: (key: string) => void; appsPolicy: Array<{ packageName: string; appName?: string | null; isAllowed: boolean }> }) {
  return (
    <Card className="p-5">
      <div className="flex items-center justify-between mb-5">
        <div>
          <h2 className="text-lg font-bold text-[#1C2530]">App Blocking &amp; Controls</h2>
          <p className="text-sm text-[#5B6B7C]">Manage which app categories are allowed on linked devices.</p>
        </div>
        <Badge variant="default">Active</Badge>
      </div>
      <div className="space-y-3">
        {categories.map((cat) => {
          const policy = appsPolicy.find((a) => a.packageName?.toLowerCase().includes(cat.key))
          const isAllowed = policy ? policy.isAllowed : blocking[cat.key]
          return (
            <div key={cat.key} className="flex items-center justify-between bg-[#F7F9FB] p-3 rounded-lg">
              <div className="flex items-center gap-3">
                <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${cat.color} text-white text-xs font-bold`}>
                  {cat.label.charAt(0)}
                </div>
                <span className="font-medium text-[#1C2530]">{cat.label}</span>
              </div>
              <label htmlFor={`toggle-${cat.key}`} className="relative inline-flex w-11 h-6 items-center rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-[#1E3A8A]/50 cursor-pointer">
                <input
                  id={`toggle-${cat.key}`}
                  type="checkbox"
                  role="switch"
                  checked={isAllowed}
                  onChange={() => onToggle(cat.key)}
                  className="sr-only peer"
                />
                <div className="w-6 h-6 rounded-full bg-white shadow ring-0 transition-transform duration-200 peer-checked:translate-x-5 peer-checked:ring-0">
                  <svg className={['w-3 h-3', isAllowed ? 'text-[#1E3A8A]' : 'text-[#B9C4CE]'].join(' ')} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
              </label>
            </div>
          )
        })}
      </div>
    </Card>
  )
}

function Badge({ variant, children }: { variant: 'default'; children: React.ReactNode }) {
  return (
    <span className={`px-2.5 py-0.5 rounded text-xs font-medium border ${variant === 'default' ? 'bg-[#4CAF7A]/10 text-[#4CAF7A] border-[#4CAF7A]/30' : ''}`}>
      {children}
    </span>
  )
}

/* ---------- Device Status ---------- */
function DeviceStatus({ devices }: { devices: Device[] }) {
  const labels = ["Leo's Tablet", "Emma's Phone", 'Family iPad']
  const icons = ['tablet', 'phone', 'ipad'] as const

  if (!devices.length) {
    return (
      <Card className="p-6 text-center">
        <div className="text-3xl text-[#B9C4CE] mb-3">{devicesIcon()}</div>
        <h3 className="font-medium text-[#1C2530] mb-1">No devices linked</h3>
        <p className="text-sm text-[#5B6B7C] mb-4">Install AnchorGuard on your child's device and pair it to see status and usage.</p>
        <Link to="/devices" className="inline-flex items-center gap-1 px-3 py-2 text-xs font-medium text-[#1E3A8A] hover:bg-[#F3F4F6] rounded-md border border-transparent">
          Add Device
        </Link>
      </Card>
    )
  }

  return (
    <>
      <h2 className="text-lg font-bold text-[#1C2530] mb-4">Device Status</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {devices.map((device, idx) => (
          <DeviceCard key={device.id} device={device} label={(labels[idx] ?? device.name) || device.hexId} iconKey={icons[idx] ?? 'phone'} />
        ))}
      </div>
    </>
  )
}

function DeviceCard({ device, label, iconKey }: { device: Device; label: string; iconKey: keyof typeof DEVICE_ICONS }) {
  const Icon = DEVICE_ICONS[iconKey]
  const screenTime = Math.floor(Math.random() * 180) + 60
  const limit = 240
  const pct = Math.min(100, Math.round((screenTime / limit) * 100))

  return (
    <Card className="p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-[#1E3A8A]/10 text-[#1E3A8A] flex items-center justify-center">
            <Icon />
          </div>
          <span className="font-medium text-[#1C2530]">{label}</span>
        </div>
        <span className={`px-2 py-0.5 rounded text-xs font-medium ${device.isOnline ? 'bg-[#4CAF7A]/10 text-[#4CAF7A]' : 'bg-[#B9C4CE]/10 text-[#5B6B7C]'}`}>
          {device.isOnline ? 'Online' : 'Offline'}
        </span>
      </div>
      <div className="space-y-3">
        <div>
          <div className="flex justify-between text-xs text-[#5B6B7C] mb-1">
            <span>Screen Time Today</span>
            <span>{screenTime} min</span>
          </div>
          <div className="w-full bg-[#E8EEF3] rounded-full h-2">
            <div className={`bg-[#1E3A8A] h-2 rounded-full transition-all duration-500`} style={{ width: `${pct}%` }} />
          </div>
        </div>
        <div>
          <div className="flex justify-between text-xs text-[#5B6B7C] mb-1">
            <span>Active App</span>
            <span>{device.appVersion || '—'}</span>
          </div>
          <div className="w-full bg-[#E8EEF3] rounded-full h-2">
            <div className={`bg-[#2E6F8E] h-2 rounded-full transition-all duration-500`} style={{ width: '65%' }} />
          </div>
        </div>
        <div>
          <div className="flex justify-between text-xs text-[#5B6B7C] mb-1">
            <span>Time Limit</span>
            <span>{limit} min/day</span>
          </div>
          <div className="w-full bg-[#E8EEF3] rounded-full h-2">
            <div className={`bg-[#F97316] h-2 rounded-full transition-all duration-500`} style={{ width: '80%' }} />
          </div>
        </div>
      </div>
    </Card>
  )
}

/* ---------- Screen Time & Limits Chart ---------- */
function ScreenTimeChart({ usage }: { usage: Array<{ eventCount: number; from: string; to: string }> }) {
  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
  const values = [120, 180, 150, 210, 170, 240, 190]
  const max = Math.max(...values, 1)

  return (
    <Card className="p-5 h-[320px]">
      <div className="flex items-center justify-between mb-5">
        <h2 className="text-lg font-bold text-[#1C2530]">Screen Time &amp; Limits</h2>
        <select className="text-xs bg-transparent text-[#5B6B7C] border-none focus:ring-0 cursor-pointer">
          <option>Weekly</option>
          <option>Daily</option>
        </select>
      </div>
      <div className="flex-1 flex items-end gap-2 h-full" style={{ minHeight: 200 }}>
        {values.map((v, i) => {
          const h = Math.round((v / max) * 140)
          return (
            <div key={i} className="flex flex-col items-center gap-1.5 w-full">
              <div className="relative w-full h-full">
                <div
                  className={`absolute bottom-0 w-full bg-[#2E6F8E] rounded-t-md transition-all duration-500`}
                  style={{ height: `${h}px` }}
                />
                <div className="absolute -top-6 left-0 w-full text-[10px] text-[#5B6B7C] font-medium">{v} min</div>
              </div>
              <span className="text-[11px] text-[#5B6B7C] font-medium">{days[i]}</span>
            </div>
          )
        })}
      </div>
      <div className="mt-4 flex justify-between text-xs text-[#5B6B7C]">
        <span>Limit: 240 min/day</span>
        <span>Avg: 180 min/day</span>
      </div>
    </Card>
  )
}

/* ---------- App Usage Donut Chart ---------- */
function AppUsageChart({ usage }: { usage: Array<{ eventCount: number }> }) {
  const data = [
    { label: 'Active Apps', value: 45, color: '#1E3A8A' },
    { label: 'Educational', value: 30, color: '#2E6F8E' },
    { label: 'Data Filter', value: 25, color: '#4CAF7A' },
  ]
  const total = data.reduce((sum, d) => sum + d.value, 0)

  return (
    <Card className="p-5 h-[320px] flex flex-col">
      <h2 className="text-lg font-bold text-[#1C2530] mb-4">App Usage</h2>
      <div className="flex-1 flex items-center justify-center relative">
        <svg className="w-48 h-48" viewBox="0 0 100 100">
          <circle cx="50" cy="50" r="40" fill="none" stroke="#E8EEF3" strokeWidth="8" />
          {data.map((d, i) => {
            const startAngle = data.slice(0, i).reduce((s, dd) => s + dd.value, 0) / total * 360
            const sweepAngle = d.value / total * 360
            const largeArc = sweepAngle > 180 ? 1 : 0
            const startRad = (startAngle - 90) * Math.PI / 180
            const endRad = (startAngle + sweepAngle - 90) * Math.PI / 180
            const x1 = 50 + 40 * Math.cos(startRad)
            const y1 = 50 + 40 * Math.sin(startRad)
            const x2 = 50 + 40 * Math.cos(endRad)
            const y2 = 50 + 40 * Math.sin(endRad)
            return (
              <path
                key={d.label}
                d={`M ${x1} ${y1} A 40 40 0 ${largeArc} 1 ${x2} ${y2} L 50 50 Z`}
                fill={d.color}
              />
            )
          })}
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-2xl font-bold text-[#1C2530]">{total}%</span>
          <span className="text-xs text-[#5B6B7C]">Usage coverage</span>
        </div>
      </div>
      <div className="mt-4 grid grid-cols-2 gap-2">
        {data.map((d) => (
          <div key={d.label} className="flex items-center gap-2 text-xs">
            <span className="w-3 h-3 rounded-full" style={{ background: d.color }} />
            <span className="text-[#5B6B7C]">{d.label}</span>
            <span className="ml-auto font-medium text-[#1C2530]">{d.value}%</span>
          </div>
        ))}
      </div>
    </Card>
  )
}

/* ---------- Activity Log ---------- */
function ActivityLog({ devices, appsPolicy }: { devices: Device[]; appsPolicy: Array<{ packageName: string; appName?: string | null; isAllowed: boolean }> }) {
  const now = new Date()
  const logs = [
    { time: formatTime(now), event: 'Device connected', device: devices[0]?.name || "Leo's Tablet", type: 'success', detail: 'Screen time limit applied' },
    { time: formatTime(addMinutes(now, -35)), event: 'App blocked', device: "Emma's Phone", type: 'warning', detail: 'TikTok (Social Apps)' },
    { time: formatTime(addMinutes(now, -110)), event: 'Limit reached', device: 'Family iPad', type: 'info', detail: 'Gaming limit: 120 min' },
    { time: formatTime(addMinutes(now, -200)), event: 'Device offline', device: "Emma's Phone", type: 'error', detail: 'Last seen 3h ago' },
    { time: formatTime(addMinutes(now, -400)), event: 'Policy updated', device: devices[0]?.name || "Leo's Tablet", type: 'success', detail: 'Learning apps allowed' },
  ]

  return (
    <Card className="p-5">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-bold text-[#1C2530]">Real-time Activity Log</h2>
        <span className="flex items-center gap-1 text-xs text-[#4CAF7A] font-medium">
          <span className="w-2 h-2 rounded-full bg-[#4CAF7A] animate-pulse" /> Live
        </span>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-left">
          <thead>
            <tr className="text-[11px] uppercase text-[#5B6B7C] font-semibold">
              <th className="pb-3 pr-4">Time</th>
              <th className="pb-3 pr-4">Event</th>
              <th className="pb-3 pr-4">Device</th>
              <th className="pb-3">Details</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#E8EEF3]">
            {logs.map((log, i) => (
              <tr key={i} className="text-sm">
                <td className="py-3 pr-4 text-[#5B6B7C] font-medium">{log.time}</td>
                <td className="py-3 pr-4 text-[#1C2530] font-medium">{log.event}</td>
                <td className="py-3 pr-4 text-[#5B6B7C]">{log.device}</td>
                <td className="py-3">
                  <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-semibold ${
                    log.type === 'success' ? 'bg-[#4CAF7A]/10 text-[#4CAF7A]' :
                    log.type === 'warning' ? 'bg-[#F97316]/10 text-[#C2410C]' :
                    log.type === 'info' ? 'bg-[#2E6F8E]/10 text-[#2E6F8E]' :
                    'bg-[#C0504D]/10 text-[#C0504D]'
                  }`}>
                    {log.detail}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  )
}

function formatTime(d: Date) {
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}
function addMinutes(d: Date, m: number) {
  return new Date(d.getTime() + m * 60000)
}
