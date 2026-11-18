import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { devicesApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import { EmptyState } from '@/components/ui/EmptyState'
import { Modal } from '@/components/ui/Modal'
import { useToast } from '@/components/ui/Toast'
import { DeviceCard } from '@/components/devices/DeviceCard'
import { useState } from 'react'

export default function DevicesPage() {
  const qc = useQueryClient()
  const toast = useToast()
  const { data: devices = [], isLoading, error } = useQuery({ queryKey: ['devices'], queryFn: devicesApi.list })
  const [confirmId, setConfirmId] = useState<string | null>(null)
  const [confirmText, setConfirmText] = useState('')
  const target = devices.find((d) => d.id === confirmId) || null

  const remove = useMutation({
    mutationFn: (id: string) => devicesApi.remove(id),
    onSuccess: () => {
      toast.show('Device removed', 'success')
      setConfirmId(null)
      setConfirmText('')
      void qc.invalidateQueries({ queryKey: ['devices'] })
    },
    onError: (e) => toast.show(getErrorMessage(e), 'error')
  })

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Devices</h1>
          <p className="muted" style={{ margin: '4px 0 0' }}>Linked child devices on this account.</p>
        </div>
      </div>

      {isLoading && <div className="card"><p className="muted">Loading devices…</p></div>}
      {error && <div className="error">{getErrorMessage(error)}</div>}

      {!isLoading && !devices.length && !error && (
        <div className="card">
          <EmptyState
            title="No devices linked yet"
            description="Install AnchorGuard on a child device and complete pairing to manage it here."
          />
        </div>
      )}

      {!!devices.length && (
        <div className="grid-2">
          {devices.map((d) => (
            <DeviceCard key={d.id} device={d} onRemove={(id) => { setConfirmId(id); setConfirmText('') }} />
          ))}
        </div>
      )}

      <Modal
        open={!!confirmId}
        title="Remove device"
        destructive
        onClose={() => { if (!remove.isPending) { setConfirmId(null); setConfirmText('') } }}
        footer={<>
          <button className="btn btn-secondary" onClick={() => { setConfirmId(null); setConfirmText('') }} disabled={remove.isPending}>Cancel</button>
          <button
            className="btn btn-danger"
            disabled={confirmText !== 'DELETE' || remove.isPending || !target}
            onClick={() => target && remove.mutate(target.id)}
          >
            {remove.isPending ? 'Removing…' : 'Confirm remove'}
          </button>
        </>}
      >
        <p>This will unlink <strong>{target?.name || target?.hexId}</strong> from your account. Type <strong>DELETE</strong> to confirm.</p>
        <div className="field">
          <label htmlFor="confirm-delete">Confirmation</label>
          <input id="confirm-delete" value={confirmText} onChange={(e) => setConfirmText(e.target.value)} placeholder="DELETE" />
        </div>
      </Modal>
    </>
  )
}
