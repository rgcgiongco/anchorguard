export default function UsersPage() {
  return (
    <>
      <div className="page-header">
        <div>
          <h1>Users</h1>
          <p className="muted" style={{ margin: '4px 0 0' }}>Manage child profiles for linked devices.</p>
        </div>
      </div>
      <div className="card stack">
        <h2>Profiles</h2>
        <p className="muted" style={{ margin: 0 }}>
          Child profile management is not yet available in this console build because device-user backend endpoints are deferred.
          Use device-level controls in Devices, Screen Time, Security, and Reports.
        </p>
      </div>
    </>
  )
}
