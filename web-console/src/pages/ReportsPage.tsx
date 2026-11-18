import { FormEvent, useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { reportsApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import { useDevices } from '@/context/DeviceContext'

function toIsoStart(date: string) { return date ? new Date(date + 'T00:00:00Z').toISOString() : undefined }
function toIsoEnd(date: string) { return date ? new Date(date + 'T23:59:59Z').toISOString() : undefined }

export default function ReportsPage() {
  const { devices, selectedDeviceId } = useDevices()
  const [deviceId, setDeviceId] = useState(selectedDeviceId || '')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [pkg, setPkg] = useState('')
  const [submitted, setSubmitted] = useState<{ deviceId: string; from?: string; to?: string; packageName?: string } | null>(null)

  const effectiveDevice = deviceId || selectedDeviceId || ''
  const q = useQuery({
    queryKey: ['reports', submitted],
    queryFn: () => reportsApi.usage(submitted!),
    enabled: !!submitted?.deviceId
  })

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!effectiveDevice) return
    setSubmitted({
      deviceId: effectiveDevice,
      from: toIsoStart(from),
      to: toIsoEnd(to),
      packageName: pkg.trim() || undefined
    })
  }

  const rows = q.data ?? []
  const total = useMemo(() => rows.reduce((s, r) => s + (r.eventCount || 0), 0), [rows])

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Reports</h1>
          <p className="muted" style={{ margin: '4px 0 0' }}>Usage tables and top activity for linked devices.</p>
        </div>
        <button
          className="btn btn-secondary"
          disabled
          title="CSV export coming in a future release"
          aria-disabled="true"
        >
          Export CSV
        </button>
      </div>

      <form className="card stack" onSubmit={onSubmit}>
        <div className="grid-3">
          <div className="field">
            <label htmlFor="r-device">Device</label>
            <select id="r-device" value={effectiveDevice} onChange={(e) => setDeviceId(e.target.value)} required>
              <option value="">Select device</option>
              {devices.map((d) => <option key={d.id} value={d.id}>{d.name || d.hexId}</option>)}
            </select>
          </div>
          <div className="field">
            <label htmlFor="r-from">From</label>
            <input id="r-from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="r-to">To</label>
            <input id="r-to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
          </div>
        </div>
        <div className="row" style={{ alignItems: 'end', justifyContent: 'space-between' }}>
          <div className="field" style={{ flex: 1, maxWidth: 420 }}>
            <label htmlFor="r-pkg">Package filter (optional)</label>
            <input id="r-pkg" value={pkg} onChange={(e) => setPkg(e.target.value)} placeholder="com.example.app" />
          </div>
          <button className="btn btn-primary" type="submit" disabled={!effectiveDevice}>Run report</button>
        </div>
        <p className="muted" style={{ margin: 0 }}>CSV export is not available yet (backend MVP returns 501). Use the table view for now.</p>
      </form>

      {q.isLoading && <div className="card">Loading usage…</div>}
      {q.error && <div className="error">{getErrorMessage(q.error)}</div>}

      {submitted && !q.isLoading && !q.error && (
        <div className="card stack">
          <div className="row" style={{ justifyContent: 'space-between' }}>
            <h2 style={{ margin: 0 }}>Usage summary</h2>
            <span className="pill pill-ok">{total} events</span>
          </div>
          {!rows.length ? (
            <p className="muted">No usage events found for the selected filters.</p>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Package</th><th>Event count</th><th>From</th><th>To</th></tr>
                </thead>
                <tbody>
                  {rows.map((r, i) => (
                    <tr key={`${r.packageName || 'all'}-${i}`}>
                      <td className="mono" style={{ fontSize: 12 }}>{r.packageName || '(all)'}</td>
                      <td>{r.eventCount}</td>
                      <td>{new Date(r.from).toLocaleString()}</td>
                      <td>{new Date(r.to).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </>
  )
}
