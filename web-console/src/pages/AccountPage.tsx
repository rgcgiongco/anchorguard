import { FormEvent, useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { accountApi } from '@/api/endpoints'
import { getErrorMessage } from '@/api/client'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/components/ui/Toast'
import { Modal } from '@/components/ui/Modal'

export default function AccountPage() {
  const { user, refreshMe, logout } = useAuth()
  const qc = useQueryClient()
  const toast = useToast()
  const meQ = useQuery({ queryKey: ['me'], queryFn: accountApi.me, initialData: user ?? undefined })
  const [name, setName] = useState(user?.name ?? '')
  const [dirty, setDirty] = useState(false)
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [deleteText, setDeleteText] = useState('')

  useEffect(() => {
    if (meQ.data) { setName(meQ.data.name ?? ''); setDirty(false) }
  }, [meQ.data])

  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => { if (dirty) { e.preventDefault(); e.returnValue = '' } }
    window.addEventListener('beforeunload', handler)
    return () => window.removeEventListener('beforeunload', handler)
  }, [dirty])

  const saveProfile = useMutation({
    mutationFn: () => accountApi.update({ name: name || null }),
    onSuccess: async () => { toast.show('Saved'); setDirty(false); await qc.invalidateQueries({ queryKey: ['me'] }); await refreshMe() },
    onError: (e) => toast.show(getErrorMessage(e))
  })

  const changePw = useMutation({
    mutationFn: () => accountApi.changePassword(currentPassword, newPassword),
    onSuccess: async () => {
      toast.show('Password changed. Please sign in again.')
      setCurrentPassword(''); setNewPassword('')
      await logout()
    },
    onError: (e) => toast.show(getErrorMessage(e))
  })

  const deleteAcc = useMutation({
    mutationFn: () => accountApi.delete(),
    onSuccess: async () => {
      toast.show('Account deleted')
      await logout()
    },
    onError: (e) => toast.show(getErrorMessage(e))
  })

  function onSave(e: FormEvent) { e.preventDefault(); saveProfile.mutate() }
  function onChangePw(e: FormEvent) {
    e.preventDefault()
    if (newPassword.length < 8) { toast.show('New password must be at least 8 characters.'); return }
    changePw.mutate()
  }

  const me = meQ.data ?? user

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Account</h1>
          <p className="muted" style={{ margin: '4px 0 0' }}>Manage your profile, password, and session.</p>
        </div>
      </div>

      <div className="grid-2">
        <form className="card stack" onSubmit={onSave}>
          <h2>Profile</h2>
          <dl className="kv" style={{ gridTemplateColumns: '120px 1fr' }}>
            <dt>Email</dt><dd>{me?.email || '—'}</dd>
            <dt>Verified</dt><dd>{me?.emailVerified ? 'Yes' : 'No'}</dd>
            <dt>Created</dt><dd>{me?.createdAt ? new Date(me.createdAt).toLocaleString() : '—'}</dd>
          </dl>
          <div className="field">
            <label htmlFor="name">Display name</label>
            <input id="name" value={name} onChange={(e) => { setName(e.target.value); setDirty(true) }} placeholder="Your name" />
          </div>
          <div className="save-bar">
            <span className={dirty ? 'dirty' : 'muted'}>{dirty ? 'Unsaved changes' : 'All changes saved'}</span>
            <button className="btn btn-primary" type="submit" disabled={!dirty || saveProfile.isPending}>
              {saveProfile.isPending ? 'Saving…' : 'Save'}
            </button>
          </div>
        </form>

        <form className="card stack" onSubmit={onChangePw}>
          <h2>Change password</h2>
          <div className="field">
            <label htmlFor="curpw">Current password</label>
            <input id="curpw" type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} autoComplete="current-password" required />
          </div>
          <div className="field">
            <label htmlFor="newpw">New password</label>
            <input id="newpw" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} autoComplete="new-password" required minLength={8} />
          </div>
          <p className="muted" style={{ margin: 0 }}>Changing your password signs you out of all sessions.</p>
          <div className="row" style={{ justifyContent: 'flex-end' }}>
            <button className="btn btn-primary" type="submit" disabled={changePw.isPending || !currentPassword || !newPassword}>
              {changePw.isPending ? 'Updating…' : 'Update password'}
            </button>
          </div>
        </form>
      </div>

      <div className="card stack">
        <h2>Session & account</h2>
        <div className="row">
          <button className="btn btn-secondary" onClick={() => { void logout() }}>Log out</button>
          <button className="btn btn-danger" onClick={() => { setDeleteOpen(true); setDeleteText('') }}>Delete account</button>
        </div>
        <p className="muted" style={{ margin: 0 }}>Deleting your account removes linked devices and data. This action cannot be undone.</p>
      </div>

      <Modal
        open={deleteOpen}
        title="Delete account"
        onClose={() => { if (!deleteAcc.isPending) { setDeleteOpen(false); setDeleteText('') } }}
        footer={<>
          <button className="btn btn-secondary" onClick={() => { setDeleteOpen(false); setDeleteText('') }} disabled={deleteAcc.isPending}>Cancel</button>
          <button className="btn btn-danger" disabled={deleteText !== 'DELETE' || deleteAcc.isPending} onClick={() => deleteAcc.mutate()}>
            {deleteAcc.isPending ? 'Deleting…' : 'Confirm delete'}
          </button>
        </>}
      >
        <p>This permanently deletes your AnchorGuard account and linked devices. Type <strong>DELETE</strong> to confirm.</p>
        <div className="field">
          <label htmlFor="del-confirm">Confirmation</label>
          <input id="del-confirm" value={deleteText} onChange={(e) => setDeleteText(e.target.value)} placeholder="DELETE" />
        </div>
      </Modal>
    </>
  )
}
