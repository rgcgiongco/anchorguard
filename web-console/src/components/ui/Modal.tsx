import type React from 'react'

export function Modal(props: {
  open: boolean
  title: string
  children: React.ReactNode
  onClose: () => void
  footer?: React.ReactNode
  destructive?: boolean
}) {
  if (!props.open) return null
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-label={props.title} onClick={props.onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3 style={{ marginTop: 0, color: props.destructive ? 'var(--ag-danger)' : undefined }}>{props.title}</h3>
        <div className="stack">{props.children}</div>
        {props.footer && <div className="row" style={{ justifyContent: 'flex-end', marginTop: 16 }}>{props.footer}</div>}
      </div>
    </div>
  )
}
