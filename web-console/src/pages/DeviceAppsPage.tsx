import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { devicesApi, policyApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import type { AppPolicy } from '@/api/types'
import { useToast } from '@/components/ui/Toast'

type Editable = AppPolicy & { _dirty?: boolean }

export default function DeviceAppsPage() {
  const { id = '' } = useParams()
  const qc = useQueryClient()
  const toast = useToast()
  const navigate = useNavigate()
  const deviceQ = useQuery({ queryKey: ['device', id], queryFn: () => devicesApi.get(id), enabled: !!id })
  const policyQ = useQuery({ queryKey: ['policy', id], queryFn: () => policyApi.get(id), enabled: !!id })
  const [apps, setApps] = useState<Editable[]>([])
  const [filter, setFilter] = useState('')

  useEffect(() => {
    if (policyQ.data) setApps(policyQ.data.apps.map((a) => ({ ...a, _dirty: false })))
  }, [policyQ.data])

  const dirty = apps.some((a) => a._dirty)
  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => { if (dirty) { e.preventDefault(); e.returnValue = '' } }
    window.addEventListener('beforeunload', handler)
    return () => window.removeEventListener('beforeunload', handler)
  }, [dirty])

  const save = useMutation({
    mutationFn: async () => {
      const changed = apps.filter((a) => a._dirty)
      for (const a of changed) {
        await policyApi.patchApp(id, a.packageName, {
          appName: a.appName ?? null,
          isAllowed: a.isAllowed,
          dailyLimitMinutes: a.dailyLimitMinutes ?? null,
          internetAccess: a.internetAccess
        })
      }
      return changed.length
    },
    onSuccess: (n) => {
      toast.show(n ? `Saved ${n} app rule${n === 1 ? '' : 's'}` : 'No changes to save')
      setApps((prev) => prev.map((a) => ({ ...a, _dirty: false })))
      void qc.invalidateQueries({ queryKey: ['policy', id] })
    },
    onError: (e) => toast.show(getErrorMessage(e))
  })

  function update(pkg: string, patch: Partial<AppPolicy>) {
    setApps((prev) => prev.map((a) => a.packageName === pkg ? { ...a, ...patch, _dirty: true } : a))
  }

  function guardedNav(to: string) {
    if (dirty && !window.confirm('You have unsaved changes. Leave this page?')) return
    navigate(to)
  }

  const filtered = useMemo(() => {
    const q = filter.trim().toLowerCase()
    if (!q) return apps
    return apps.filter((a) => a.packageName.toLowerCase().includes(q) || (a.appName || '').toLowerCase().includes(q))
  }, [apps, filter])

  if (policyQ.isLoading || deviceQ.isLoading) return <div className="card">Loading app policy…</div>
  if (policyQ.error) return <div className="error">{getErrorMessage(policyQ.error)}</div>

  return (
    <>
      <div className="page-header">
        <div>
          <h1>App Policy</h1>
          <p className="muted" style={{ margin: '4px 0 0' }}>{deviceQ.data?.name || deviceQ.data?.hexId || 'Device'} — allow/block apps, limits, internet.</p>
        </div>
        <div className="row">
          <button className="btn btn-secondary" onClick={() => guardedNav(`/devices/${id}`)}>Device detail</button>
          <button className="btn btn-secondary" onClick={() => guardedNav(`/devices/${id}/schedule`)}>Schedule</button>
          <Link className="btn btn-secondary" to="/devices">Devices</Link>
        </div>
      </div>

      <div className="card stack">
        <div className="row" style={{ justifyContent: 'space-between' }}>
          <div className="field" style={{ minWidth: 260, flex: 1 }}>
            <label htmlFor="app-filter">Filter apps</label>
            <input id="app-filter" value={filter} onChange={(e) => setFilter(e.target.value)} placeholder="Package or app name" />
          </div>
          <div className="row" style={{ alignSelf: 'end' }}>
            <span className={dirty ? 'dirty' : 'muted'}>{dirty ? 'Unsaved changes' : 'All changes saved'}</span>
            <button className="btn btn-primary" onClick={() => save.mutate()} disabled={!dirty || save.isPending}>
              {save.isPending ? 'Saving…' : 'Save changes'}
            </button>
          </div>
        </div>

        {!filtered.length ? (
          <p className="muted">{apps.length ? 'No apps match this filter.' : 'No app policies reported yet. Policies appear after the device syncs installed apps.'}</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>App</th>
                  <th>Package</th>
                  <th>Allowed</th>
                  <th>Daily limit (min)</th>
                  <th>Internet</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((a) => (
                  <tr key={a.packageName}>
                    <td>{a.appName || '—'}</td>
                    <td className="mono" style={{ fontSize: 12 }}>{a.packageName}</td>
                    <td>
                      <input type="checkbox" checked={a.isAllowed} onChange={(e) => update(a.packageName, { isAllowed: e.target.checked })} aria-label={`Allow ${a.packageName}`} />
                    </td>
                    <td>
                      <input
                        type="number" min={0} style={{ width: 100 }}
                        value={a.dailyLimitMinutes ?? ''}
                        onChange={(e) => update(a.packageName, { dailyLimitMinutes: e.target.value === '' ? null : Number(e.target.value) })}
                        placeholder="—"
                      />
                    </td>
                    <td>
                      <select value={a.internetAccess} onChange={(e) => update(a.packageName, { internetAccess: e.target.value })}>
                        <option value="on">On</option>
                        <option value="off">Off</option>
                        <option value="wifi">Wi-Fi only</option>
                        <option value="mobile">Mobile only</option>
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  )
}
