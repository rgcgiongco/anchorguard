# Module 06 — Auth and Pairing

## Scope
- Parent account lifecycle: email/password registration, login, refresh, logout, password reset
- Device pairing: 6-character alphanumeric pairing code, device registration, device API key (X-Device-Key)
- Child PIN lifecycle: create, verify, change, persistence (encrypted), setup completion state
- Biometric fallback for parent gate (Android BiometricPrompt)
- Brute-force lockout: PIN attempt counter, cooldown after N failures, reset on success
- Device Admin / parent gate integration: enforcement of locked device state

## Backend (existing primitives, extended)

### Auth endpoints (partially exist)
- `POST /v1/auth/register` — email + password → parent account + JWT pair
- `POST /v1/auth/login` — email + password → JWT pair
- `POST /v1/auth/refresh` — refresh token → new access token
- `POST /v1/auth/logout` — revoke refresh token
- `POST /v1/auth/password-reset/request` — email → reset token (email-send deferred)
- `POST /v1/auth/password-reset/confirm` — token + new password → rotate password

### Pairing endpoints
- `POST /v1/devices/register` — X-Device-Key + device metadata → device record + pairing_token
- `POST /v1/devices/pair` — parent JWT + pairing_token → binds device to parent, returns device API key
- `GET /v1/devices/heartbeat` — X-Device-Key → last_seen update, policy delta
- `DELETE /v1/devices/:id` — parent JWT → unbind device

### Security
- JWT access token (short TTL) + refresh token (longer TTL, hashed storage)
- Rate limiting on `/v1/auth/login` and `/v1/devices/pair` (brute-force protection)
- Pairing code: 6-char alphanumeric, single-use, TTL (default 10 min)
- Clean room: original endpoint names, no reference-service naming

## Android Client

### PIN lifecycle
- `PinCreateScreen` — >=4 digits, equality confirm, persist encrypted PIN hash to EncryptedSharedPreferences / Room, set `pin_setup_complete=true`
- `PinVerifyScreen` — verify against stored hash; on success → enter parent mode / unlock
- `PinChangeScreen` — verify old PIN, set new PIN (>=4 digits), persist
- PIN stored as Argon2/BCrypt hash (client-side) or encrypted at rest; never plaintext
- Attempt counter: increment on failure, lockout after 5 failures for 300s (configurable), reset on success
- Lockout state persisted across app restarts

### Biometric fallback
- `BiometricPrompt` for parent gate unlock when biometric enrolled
- Biometric success → treat as PIN-verified (short session, e.g. 5 min)
- Fallback to PIN entry if biometric unavailable or user cancels

### Pairing flow (client)
- `PairingScreen` — 6-char code input, call `POST /v1/devices/pair` with parent JWT
- On success: store device API key (X-Device-Key) securely, mark device paired, navigate to dashboard
- Error handling: invalid/expired code, network failure, rate-limit cooldown
- Pairing code expiry shown via countdown UI

### Device Admin / parent gate integration
- `AnchorGuardDeviceAdminReceiver` — device-admin enable/disable, lock-task, disable-request
- Parent gate (`ParentGateScreen`) — PIN or biometric verification before:
  - Exiting parent mode
  - Changing device admin state
  - Uninstalling app
- Tamper-proof: when Device Admin active, block uninstall via `onDisableRequested` returning CHAR_CUSTOM_DISABLE_MESSAGE; enforce parent-gate re-verification on re-enable

### Auth session contract
- Access token stored in EncryptedSharedPreferences (not plain SharedPreferences)
- Refresh token used to silently rotate access token on 401
- Logout clears tokens, PIN state (optional), and unregisters device admin if configured

### Screens
- `AuthWelcomeScreen` — login / register entry
- `AuthRegisterScreen` — email + password + confirm
- `AuthLoginScreen` — email + password
- `PinCreateScreen` — PIN create + confirm
- `PinVerifyScreen` — PIN entry (parent gate)
- `PinChangeScreen` — change PIN
- `PairingScreen` — pairing code input
- `SecurityScreen` — Device Admin/overlay/usage-access/accessibility/battery status + intents

### Clean Room Rules
- All screen/ViewModel/repository names are original AnchorGuard names
- No reference-service class/method names (no `ud.*`, `l0`, `i`, `Utility`, etc.)
- PIN hash algorithm and storage original to AnchorGuard
- Pairing code generation original implementation
