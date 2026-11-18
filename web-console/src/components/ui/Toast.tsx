import { createContext, useCallback, useContext, useMemo, useState } from 'react'

export type ToastTone = 'info' | 'success' | 'error' | 'warning'
type ToastState = { id: number; message: string; tone: ToastTone } | null
type ToastCtx = {
  show: (message: string, tone?: ToastTone) => void
}

const Ctx = createContext<ToastCtx | undefined>(undefined)

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toast, setToast] = useState<ToastState>(null)
  const show = useCallback((message: string, tone: ToastTone = 'info') => {
    const id = Date.now() + Math.floor(Math.random() * 1000)
    setToast({ id, message, tone })
    window.setTimeout(() => setToast((current) => (current?.id === id ? null : current)), 4000)
  }, [])
  const value = useMemo(() => ({ show }), [show])
  return (
    <Ctx.Provider value={value}>
      {children}
      <div className="toast-stack" aria-live="polite" aria-atomic="true">
        {toast && <div className={`toast toast-${toast.tone}`} role="status">{toast.message}</div>}
      </div>
    </Ctx.Provider>
  )
}

export function useToast() {
  const ctx = useContext(Ctx)
  if (!ctx) throw new Error('useToast must be used within ToastProvider')
  return ctx
}
