import { useMemo, useState } from 'react'

export type DataColumn<T> = {
  key: string
  header: string
  sortable?: boolean
  render: (row: T) => React.ReactNode
  sortValue?: (row: T) => string | number
}

export function DataTable<T>(props: {
  rows: T[]
  columns: DataColumn<T>[]
  rowKey: (row: T) => string
  pageSize?: number
  empty?: React.ReactNode
}) {
  const [sortKey, setSortKey] = useState<string | null>(null)
  const [dir, setDir] = useState<'asc' | 'desc'>('asc')
  const [page, setPage] = useState(0)
  const pageSize = props.pageSize ?? 10

  const sorted = useMemo(() => {
    const col = props.columns.find((c) => c.key === sortKey && c.sortable)
    if (!col) return props.rows
    const get = col.sortValue ?? ((row: T) => String(col.render(row) ?? ''))
    return [...props.rows].sort((a, b) => {
      const av = get(a); const bv = get(b)
      const cmp = av < bv ? -1 : av > bv ? 1 : 0
      return dir === 'asc' ? cmp : -cmp
    })
  }, [props.rows, props.columns, sortKey, dir])

  const pageCount = Math.max(1, Math.ceil(sorted.length / pageSize))
  const safePage = Math.min(page, pageCount - 1)
  const slice = sorted.slice(safePage * pageSize, safePage * pageSize + pageSize)

  if (!props.rows.length) return <>{props.empty ?? <p className="muted">No data.</p>}</>

  function onSort(key: string) {
    if (sortKey !== key) { setSortKey(key); setDir('asc') }
    else setDir((d) => (d === 'asc' ? 'desc' : 'asc'))
  }

  return (
    <div className="stack">
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              {props.columns.map((c) => (
                <th key={c.key}>
                  {c.sortable ? (
                    <button className="btn btn-text" type="button" onClick={() => onSort(c.key)}>
                      {c.header}{sortKey === c.key ? (dir === 'asc' ? ' ↑' : ' ↓') : ''}
                    </button>
                  ) : c.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {slice.map((row) => (
              <tr key={props.rowKey(row)}>
                {props.columns.map((c) => <td key={c.key}>{c.render(row)}</td>)}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {pageCount > 1 && (
        <div className="row" style={{ justifyContent: 'flex-end' }}>
          <span className="muted">Page {safePage + 1} / {pageCount}</span>
          <button className="btn btn-secondary" disabled={safePage === 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>Previous</button>
          <button className="btn btn-secondary" disabled={safePage >= pageCount - 1} onClick={() => setPage((p) => Math.min(pageCount - 1, p + 1))}>Next</button>
        </div>
      )}
    </div>
  )
}
