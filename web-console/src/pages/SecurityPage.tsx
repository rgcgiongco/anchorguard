import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { devicesApi, policyApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import type { DeviceSettings } from '@/api/types'
import { useDevices } from '@/context/DeviceContext'
import { useToast } from '@/components/ui/Toast'

const defaults: DeviceSettings = {
  advancedTimer: false, backgroundMode: false, tamperProof: false, bootStart: false,
  keepAlive: false, bruteForcePin: false, blockIntervalMs: 500, appTitle: 'AnchorGuard'
}

export default function SecurityPage() {
  const { selectedDeviceId, selectedDevice } = useDevices()
  const qc = useQueryClient()
  const toast = useToast()
  const { data: devices = [] } = useQuery({ queryKey: ['devices'], queryFn: devicesApi.list })
  const policyQ = useQuery({
    queryKey: ['policy', selectedDeviceId],
    queryFn: () => policyApi.get(selectedDeviceId!),
    enabled: !!selectedDeviceId
  })
  const [settings, setSettings] = useState<DeviceSettings>(defaults)
  const [dirty, setDirty] = useState(false)

  useEffect(() => {
    if (policyQ.data) { setSettings(policyQ.data.settings); setDirty(false) }
    else if (!selectedDeviceId) { setSettings(defaults); setDirty(false) }
  }, [policyQ.data, selectedDeviceId])

  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => { if (dirty) { e.preventDefault(); e.returnValue = '' } }
    window.addEventListener('beforeunload', handler)
    return () => window.removeEventListener('beforeunload', handler)
  }, [dirty])

  const save = useMutation({
    mutationFn: () => policyApi.patch(selectedDeviceId!, { settings }),
    onSuccess: () => { toast.show('Saved'); setDirty(false); void qc.invalidateQueries({ queryKey: ['policy', selectedDeviceId] }) },
    onError: (e) => toast.show(getErrorMessage(e))
  })

  function toggle<K extends keyof DeviceSettings>(key: K, value: DeviceSettings[K]) {
    setSettings((prev) => ({ ...prev, [key]: value }))
    setDirty(true)
  }

  if (!devices.length) return <div className="card">Link a device to configure security settings. <Link to="/devices">Go to devices</Link>.</div>

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Security</h1>
          <p className="muted" style={{ margin: '4px 0 0' }}>Device protection controls for {selectedDevice?.name || selectedDevice?.hexId || 'selected device'}.</p>
        </div>
        {selectedDeviceId && <Link className="btn btn-secondary" to={`/devices/${selectedDeviceId}`}>Open device</Link>}
      </div>

      <div className="card stack">
        {policyQ.isLoading && <p className="muted">Loading settings…</p>}
        {policyQ.error && <div className="error">{getErrorMessage(policyQ.error)}</div>}
        <div className="grid-2">
          <label className="row"><input type="checkbox" checked={settings.tamperProof} onChange={(e) => toggle('tamperProof', e.target.checked)} /> <span><strong>Tamper proofing</strong><div className="muted">Harden against uninstall or disabling protection.</div></span></label>
          <label className="row"><input type="checkbox" checked={settings.bootStart} onChange={(e) => toggle('bootStart', e.target.checked)} /> <span><strong>Boot start</strong><div className="muted">Start protection when the device boots.</div></span></label>
          <label className="row"><input type="checkbox" checked={settings.keepAlive} onChange={(e) => toggle('keepAlive', e.target.checked)} /> <span><strong>Keep alive</strong><div className="muted">Maintain background protection services.</div></span></label>
          <label className="row"><input type="checkbox" checked={settings.backgroundMode} onChange={(e) => toggle('backgroundMode', e.target.checked)} /> <span><strong>Background mode</strong><div className="muted">Allow monitoring to run in background.</div></span></label>
          <label className="row"><input type="checkbox" checked={settings.bruteForcePin} onChange={(e) => toggle('bruteForcePin', e.target.checked)} /> <span><strong>Brute-force PIN protection</strong><div className="muted">Throttle repeated failed PIN attempts.</div></span></label>
          <label className="row"><input type="checkbox" checked={settings.advancedTimer} onChange={(e) => toggle('advancedTimer', e.target.checked)} /> <span><strong>Advanced timer</strong><div className="muted">Enable advanced screen-time timer controls.</div></span></label>
        </div>
        <div className="grid-2">
          <div className="field">
            <label htmlFor="blockIntervalMs">Block interval (ms)</label>
            <input id="blockIntervalMs" type="number" min={100} step={50} value={settings.blockIntervalMs} onChange={(e) => toggle('blockIntervalMs', Number(e.target.value || 500))} />
          </div>
          <div className="field">
            <label htmlFor="appTitle">App title</label>
            <input id="appTitle" value={settings.appTitle} onChange={(e) => toggle('appTitle', e.target.value)} />
          </div>
        </div>
        <div className="notice">Device Admin status is reported by the Android client. These controls update the server policy for the selected device.</div>
        <div className="save-bar">
          <span className={dirty ? 'dirty' : 'muted'}>{dirty ? 'Unsaved changes' : 'All changes saved'}</span>
          <button className="btn btn-primary" onClick={() => save.mutate()} disabled={!dirty || save.isPending || !selectedDeviceId}>
            {save.isPending ? 'Saving…' : 'Save'}
          </button>
        </div>
      </div>
    </>
  )
}
