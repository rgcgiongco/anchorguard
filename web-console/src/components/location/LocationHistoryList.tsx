import type { LocationPoint } from '@/api/types'

interface LocationHistoryListProps {
  history: LocationPoint[]
}

function relativeTime(timestamp: string): string {
  const ms = Date.now() - new Date(timestamp).getTime()
  const secs = Math.floor(ms / 1000)
  if (secs < 60) return 'Just now'
  const mins = Math.floor(secs / 60)
  if (mins < 60) return `${mins} min${mins === 1 ? '' : 's'} ago`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours} hour${hours === 1 ? '' : 's'} ago`
  const days = Math.floor(hours / 24)
  return `${days} day${days === 1 ? '' : 's'} ago`
}

export default function LocationHistoryList({ history }: LocationHistoryListProps) {
  if (history.length === 0) {
    return (
      <div className="history-empty">
        <p className="muted">No location history yet.</p>
      </div>
    )
  }
  return (
    <div className="history-list">
      {history.map((p) => (
        <div key={p.id} className="history-item">
          <div className="history-coords">
            {p.latitude.toFixed(5)}, {p.longitude.toFixed(5)}
          </div>
          <div className="history-meta">
            <span className="history-time">{relativeTime(p.timestamp)}</span>
            {p.accuracy != null && (
              <span className="history-accuracy">±{Math.round(p.accuracy)} m</span>
            )}
          </div>
        </div>
      ))}
    </div>
  )
}
