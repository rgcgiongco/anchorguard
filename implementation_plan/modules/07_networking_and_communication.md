# Module 07 — Networking and Communication

## Scope
- Unified Retrofit/OkHttp HTTP client for Android client
- Auth interceptor + OkHttp `Authenticator` for bearer injection and 401 refresh/retry
- Centralized network error mapping and standardized offline UX
- Connectivity-aware request gating (no requests when offline)
- WorkManager sync workers: usage upload (`POST /v1/usage/batch`), policy download (`GET /v1/policies`)
- Offline queue: unsynced usage events stored in Room, retried with exponential backoff
- Device API key (`X-Device-Key`) and bearer auth header consistency

## HTTP Client (`NetworkModule`)
- Single cached `OkHttpClient` + `Retrofit` instance (not separate Auth/Sync providers)
- Base URL from secure storage (`SecureSessionStore` / `SyncPreferences`)
- Gson converter
- Logging interceptor (debug only)
- Timeouts: connect 20s, read/write 30s
- `AuthInterceptor`: injects `Authorization: Bearer <access_token>` from secure store
- `Authenticator`: on 401, calls `POST /v1/auth/refresh`, retries failed request once with new token; on refresh failure → logout
- `NetworkErrorInterceptor`: maps exceptions to `NetworkError` (offline, timeout, server, auth, unknown)

## API Services
- `AuthApi` — `/auth/register|login|refresh|logout`, `/devices/register|pair`
- `SyncApi` — `POST /v1/usage/batch` (usage events), `GET /v1/policies` (policy delta)
- Device API key header `X-Device-Key` injected via interceptor when paired
- Clean room: original interface names, no reference-service naming

## Connectivity
- `ConnectivityManager` observer (or WorkManager `NetworkType.CONNECTED` constraint)
- `isOnline()` gating before enqueuing sync work
- Offline state surfaced in UI (toast/snackbar: "Offline — changes will sync when reconnected")

## Sync Workers (WorkManager)
- `UsageBatchUploadWorker` — uploads unsynced `UsageEvent` rows (Room `UsageEventDao.getUnsynced()`), marks accepted IDs synced, quarantines rejected IDs with max retry count
- `PolicyDownloadWorker` — downloads policy delta via `GET /v1/policies`, merges into Room (`AppPolicyDao`, `DeviceSettingDao`), cloud-wins
- Constraints: `NetworkType.CONNECTED`, exponential backoff (initial 30s), `KEEP` policy
- Periodic: usage upload every 15 min, policy download every 30 min
- `syncNow()` — one-time chain: policy download → usage upload when device provisioned

## Offline Queue
- Usage events written to Room first with `synced=false`
- `SyncStatus` table tracks `lastSyncedAt` per table for incremental sync (since-timestamp)
- Rejected upload IDs quarantined (max 5 retries), then moved to dead-letter (not retried indefinitely)
- Full snapshot vs incremental: use `since=lastSyncedAt` for policy downloads where supported

## Error Handling
- Standardized `Result< T, NetworkError>` pattern across repositories
- Auth errors (401/403) → token refresh attempt → logout on failure
- Transient errors → retry via WorkManager backoff
- Offline → queue locally, no crash, UI shows offline indicator

## Clean Room Rules
- All client/network class names original AnchorGuard names
- No copied interceptor/authenticator class names from reference
- No reference package/class references (`ud.*`, `l0`, `i`, `Utility`, etc.)
