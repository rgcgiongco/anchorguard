import { FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { devicesApi, policyApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import { useToast } from '@/components/ui/Toast'

export default function DeviceDetailPage() {
  const { id = '' } = useParams()
  const qc = useQueryClient()
  const toast = useToast()
  const navigate = useNavigate()
  const deviceQ = useQuery({ queryKey: ['device', id], queryFn: () => devicesApi.get(id), enabled: !!id })
  const policyQ = useQuery({ queryKey: ['policy', id], queryFn: () => policyApi.get(id), enabled: !!id })
  const [name, setName] = useState('')
  const [dirty, setDirty] = useState(false)

  useEffect(() => {
    if (deviceQ.data) {
      setName(deviceQ.data.name ?? '')
      setDirty(false)
    }
  }, [deviceQ.data])

  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => {
      if (dirty) { e.preventDefault(); e.returnValue = '' }
    }
    window.addEventListener('beforeunload', handler)
    return () => window.removeEventListener('beforeunload', handler)
  }, [dirty])

  const saveName = useMutation({
    mutationFn: () => devicesApi.patch(id, { name: name || null }),
    onSuccess: () => {
      toast.show('Saved')
      setDirty(false)
      void qc.invalidateQueries({ queryKey: ['device', id] })
      void qc.invalidateQueries({ queryKey: ['devices'] })
    },
    onError: (e) => toast.show(getErrorMessage(e))
  })

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    saveName.mutate()
  }

  function guardedNav(to: string) {
    if (dirty && !window.confirm('You have unsaved changes. Leave this page?')) return
    navigate(to)
  }

  if (deviceQ.isLoading) return <div className="card">Loading device…</div>
  if (deviceQ.error || !deviceQ.data) return <div className="error">{getErrorMessage(deviceQ.error) || 'Device not found.'}</div>

  const d = deviceQ.data
  const s = policyQ.data?.settings

  return (
    <>
      <div className="page-header">
        <div>
          <h1>{d.name || d.hexId}</h1>
          <p className="muted" style={{ margin: '4px 0 0' }}>Device detail and settings overview.</p>
        </div>
        <div className="row">
          <button className="btn btn-secondary" onClick={() => guardedNav(`/devices/${id}/apps`)}>App policy</button>
          <button className="btn btn-secondary" onClick={() => guardedNav(`/devices/${id}/schedule`)}>Schedule</button>
          <Link className="btn btn-secondary" to="/devices">Back to devices</Link>
        </div>
      </div>

      <div className="grid-2">
        <div className="card stack">
          <h2>Device info</h2>
          <dl className="kv">
            <dt>Hex ID</dt><dd className="mono">{d.hexId}</dd>
            <dt>Model</dt><dd>{d.model || '—'}</dd>
            <dt>Manufacturer</dt><dd>{d.manufacturer || '—'}</dd>
            <dt>Android ID</dt><dd className="mono">{d.androidId || '—'}</dd>
            <dt>App version</dt><dd>{d.appVersion || '—'}</dd>
            <dt>Status</dt><dd><span className={`pill ${d.isOnline ? 'pill-ok' : 'pill-warn'}`}>{d.isOnline ? 'Online' : 'Offline'}</span></dd>
            <dt>Paired</dt><dd>{d.isPaired ? 'Yes' : 'No'}</dd>
            <dt>Last seen</dt><dd>{d.lastSeen ? new Date(d.lastSeen).toLocaleString() : '—'}</dd>
            <dt>Created</dt><dd>{new Date(d.createdAt).toLocaleString()}</dd>
          </dl>
        </div>

        <form className="card stack" onSubmit={onSubmit}>
          <h2>Device name</h2>
          <div className="field">
            <label htmlFor="device-name">Display name</label>
            <input id="device-name" value={name} onChange={(e) => { setName(e.target.value); setDirty(true) }} placeholder="e.g. Alex's tablet" />
          </div>
          <div className="save-bar">
            <span className={dirty ? 'dirty' : 'muted'}>{dirty ? 'Unsaved changes' : 'All changes saved'}</span>
            <button className="btn btn-primary" type="submit" disabled={!dirty || saveName.isPending}>
              {saveName.isPending ? 'Saving…' : 'Save'}
            </button>
          </div>
        </form>
      </div>

      <div className="card stack">
        <h2>Protection overview</h2>
        {!s ? <p className="muted">Loading policy…</p> : (
          <div className="grid-3">
            <div><div className="muted">Tamper proofing</div><strong>{s.tamperProof ? 'Enabled' : 'Disabled'}</strong></div>
            <div><div className="muted">Boot start</div><strong>{s.bootStart ? 'Enabled' : 'Disabled'}</strong></div>
            <div><div className="muted">Keep alive</div><strong>{s.keepAlive ? 'Enabled' : 'Disabled'}</strong></div>
            <div><div className="muted">Background mode</div><strong>{s.backgroundMode ? 'Enabled' : 'Disabled'}</strong></div>
            <div><div className="muted">Advanced timer</div><strong>{s.advancedTimer ? 'Enabled' : 'Disabled'}</strong></div>
            <div><div className="muted">Apps in policy</div><strong>{policyQ.data?.apps.length ?? 0}</strong></div>
          </div>
        )}
        <p className="muted" style={{ marginBottom: 0 }}>Edit protection flags from <Link to="/security">Security</Link>. Manage per-app rules and schedule from the buttons above.</p>
      </div>
    </>
  )
}
