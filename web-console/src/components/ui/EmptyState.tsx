export function EmptyState(props: { title: string; description: string; action?: React.ReactNode; icon?: string }) {
  return (
    <div className="empty-state">
      <div style={{ fontSize: 40, marginBottom: 8 }} aria-hidden>{props.icon ?? '◎'}</div>
      <h3>{props.title}</h3>
      <p>{props.description}</p>
      {props.action && <div style={{ marginTop: 16 }}>{props.action}</div>}
    </div>
  )
}
