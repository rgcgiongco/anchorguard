import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { devicesApi, policyApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import { useToast } from '@/components/ui/Toast'

type Day = 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat' | 'sun'
type Slot = { start: string; end: string; enabled: boolean }
type Schedule = Record<Day, Slot>

const DAYS: { key: Day; label: string }[] = [
  { key: 'mon', label: 'Monday' }, { key: 'tue', label: 'Tuesday' }, { key: 'wed', label: 'Wednesday' },
  { key: 'thu', label: 'Thursday' }, { key: 'fri', label: 'Friday' }, { key: 'sat', label: 'Saturday' }, { key: 'sun', label: 'Sunday' }
]

function defaultSchedule(): Schedule {
  return DAYS.reduce((acc, d) => {
    acc[d.key] = { start: '08:00', end: '20:00', enabled: d.key !== 'sat' && d.key !== 'sun' }
    return acc
  }, {} as Schedule)
}

function normalize(raw: unknown): Schedule {
  const base = defaultSchedule()
  if (!raw || typeof raw !== 'object') return base
  const obj = raw as Record<string, any>
  for (const d of DAYS) {
    const v = obj[d.key]
    if (v && typeof v === 'object') {
      base[d.key] = {
        start: typeof v.start === 'string' ? v.start : base[d.key].start,
        end: typeof v.end === 'string' ? v.end : base[d.key].end,
        enabled: typeof v.enabled === 'boolean' ? v.enabled : base[d.key].enabled
      }
    }
  }
  return base
}

export default function DeviceSchedulePage() {
  const { id = '' } = useParams()
  const qc = useQueryClient()
  const toast = useToast()
  const navigate = useNavigate()
  const deviceQ = useQuery({ queryKey: ['device', id], queryFn: () => devicesApi.get(id), enabled: !!id })
  const schedQ = useQuery({ queryKey: ['schedule', id], queryFn: () => policyApi.getSchedule(id), enabled: !!id })
  const [schedule, setSchedule] = useState<Schedule>(defaultSchedule())
  const [dirty, setDirty] = useState(false)

  useEffect(() => {
    if (schedQ.data) { setSchedule(normalize(schedQ.data.weeklySchedule)); setDirty(false) }
  }, [schedQ.data])

  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => { if (dirty) { e.preventDefault(); e.returnValue = '' } }
    window.addEventListener('beforeunload', handler)
    return () => window.removeEventListener('beforeunload', handler)
  }, [dirty])

  const save = useMutation({
    mutationFn: () => policyApi.setSchedule(id, schedule),
    onSuccess: () => { toast.show('Saved'); setDirty(false); void qc.invalidateQueries({ queryKey: ['schedule', id] }); void qc.invalidateQueries({ queryKey: ['policy', id] }) },
    onError: (e) => toast.show(getErrorMessage(e))
  })

  function setDay(day: Day, patch: Partial<Slot>) {
    setSchedule((prev) => ({ ...prev, [day]: { ...prev[day], ...patch } }))
    setDirty(true)
  }

  function applyPreset(preset: 'weekdays' | 'everyday' | 'weekend') {
    setSchedule((prev) => {
      const next = { ...prev }
      for (const d of DAYS) {
        if (preset === 'everyday') next[d.key] = { ...next[d.key], enabled: true }
        if (preset === 'weekdays') next[d.key] = { ...next[d.key], enabled: !['sat', 'sun'].includes(d.key) }
        if (preset === 'weekend') next[d.key] = { ...next[d.key], enabled: ['sat', 'sun'].includes(d.key), start: '09:00', end: '21:00' }
      }
      return next
    })
    setDirty(true)
  }

  function guardedNav(to: string) {
    if (dirty && !window.confirm('You have unsaved changes. Leave this page?')) return
    navigate(to)
  }

  if (schedQ.isLoading || deviceQ.isLoading) return <div className="card">Loading schedule…</div>
  if (schedQ.error) return <div className="error">{getErrorMessage(schedQ.error)}</div>

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Screen Time Schedule</h1>
          <p className="muted" style={{ margin: '4px 0 0' }}>{deviceQ.data?.name || deviceQ.data?.hexId || 'Device'} — weekly allowed windows.</p>
        </div>
        <div className="row">
          <button className="btn btn-secondary" onClick={() => guardedNav(`/devices/${id}`)}>Device detail</button>
          <button className="btn btn-secondary" onClick={() => guardedNav(`/devices/${id}/apps`)}>Apps</button>
          <Link className="btn btn-secondary" to="/devices">Devices</Link>
        </div>
      </div>

      <div className="card stack">
        <div className="row" style={{ justifyContent: 'space-between' }}>
          <div className="row">
            <span className="muted">Presets:</span>
            <button className="btn btn-secondary" type="button" onClick={() => applyPreset('weekdays')}>Weekdays</button>
            <button className="btn btn-secondary" type="button" onClick={() => applyPreset('everyday')}>Every day</button>
            <button className="btn btn-secondary" type="button" onClick={() => applyPreset('weekend')}>Weekend</button>
          </div>
          <div className="row">
            <span className={dirty ? 'dirty' : 'muted'}>{dirty ? 'Unsaved changes' : 'All changes saved'}</span>
            <button className="btn btn-primary" onClick={() => save.mutate()} disabled={!dirty || save.isPending}>
              {save.isPending ? 'Saving…' : 'Save'}
            </button>
          </div>
        </div>

        <div className="schedule-grid">
          {DAYS.map((d) => (
            <div className="schedule-row card" key={d.key} style={{ padding: 12 }}>
              <label className="row" style={{ fontWeight: 600 }}>
                <input type="checkbox" checked={schedule[d.key].enabled} onChange={(e) => setDay(d.key, { enabled: e.target.checked })} />
                {d.label}
              </label>
              <div className="field">
                <label htmlFor={`${d.key}-start`}>Start</label>
                <input id={`${d.key}-start`} type="time" value={schedule[d.key].start} onChange={(e) => setDay(d.key, { start: e.target.value })} disabled={!schedule[d.key].enabled} />
              </div>
              <div className="field">
                <label htmlFor={`${d.key}-end`}>End</label>
                <input id={`${d.key}-end`} type="time" value={schedule[d.key].end} onChange={(e) => setDay(d.key, { end: e.target.value })} disabled={!schedule[d.key].enabled} />
              </div>
              <div className="muted" style={{ fontSize: 13 }}>{schedule[d.key].enabled ? 'Allowed window' : 'Off'}</div>
            </div>
          ))}
        </div>
      </div>
    </>
  )
}
