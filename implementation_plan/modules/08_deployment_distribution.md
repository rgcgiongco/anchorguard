# Module 08 — Deployment & Distribution

## Scope
- Sideload APK distribution (direct install, no Play Store)
- New AnchorGuard signing keystore (clean-room, not reference cert)
- Versioned artifact naming and checksum generation/verification
- MDM/provisioning guidance (optional, documented)
- CI/CD pipeline for Android build/sign/test and backend build/push

## Signing (clean room)
- Generate new keystore: `anchorguard-release.jks` (alias `anchorguard`, RSA 2048+, validity 25+ years)
- Keystore NOT committed to repo; path/password via env vars or local.properties (gitignored)
- `app/build.gradle.kts`: `signingConfigs { release { ... } }` wired to `buildTypes.release.signingConfig`
- `isMinifyEnabled = true` with ProGuard/R8 rules (keep AnchorGuard classes, reflection-safe)
- Two keystores recommended: debug (dev) and release (distribution)

## APK Distribution (sideload)
- Artifact naming: `AnchorGuard-v<versionCode>-<versionName>-release.apk` (e.g. `AnchorGuard-v4-1.0.4-release.apk`)
- Output directory: `app/release/` (gitignored)
- Checksums: SHA-256 (`sha256sum`) and SHA-512 for each APK; publish `*.apk.sha256` and `*.apk.sha512`
- Verification script: `scripts/verify-apk-checksum.sh` (compare computed vs published)
- Install instructions: enable "Install unknown apps", `adb install -r AnchorGuard-...-release.apk`
- Version codes increment per release; versionName semver (e.g. 1.0.0)

## MDM / Provisioning (documented, optional for MVP)
- Device Owner provisioning via NFC/ADB (`dpc`/`dpm`) for enterprise deployments
- `android.app.admin.DevicePolicyManager` integration (Module 04 Device Admin)
- Provisioning QR/enrollment token flow documented but not implemented in MVP
- Sideload remains primary distribution for MVP (parent-installed on child device)

## CI/CD (GitHub Actions)
- `.github/workflows/android-ci.yml`:
  - Setup JDK + Android SDK (`android-actions/setup-android`)
  - Unit tests (`./gradlew testDebugUnitTest`)
  - Lint (`./gradlew lintDebug`)
  - Assemble release APK (`./gradlew assembleRelease`)
  - Sign with release keystore (secrets: `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`)
  - Generate SHA-256/SHA-512 checksums
  - Upload APK + checksums as release assets (GitHub Releases)
- `.github/workflows/backend-ci.yml`:
  - Build backend Docker image (`docker build -t anchorguard-api`)
  - Run backend tests
  - Push to container registry (optional)
- Secrets: never commit keystore, JWT signing key, DB credentials, API secrets

## Backend Deployment (existing primitives)
- `backend/Dockerfile`, `backend/docker-compose.yml` (db/api/nginx, cert mounts)
- Docker Compose for self-hosted backend; `.env` for DB credentials, JWT secret
- Nginx reverse proxy with TLS termination (certs via Let's Encrypt or self-signed for dev)
- Database migrations: Flyway on startup

## Environment Variables / Secrets
- `ANCHORGUARD_KEYSTORE_FILE`, `ANCHORGUARD_KEYSTORE_PASSWORD`, `ANCHORGUARD_KEY_ALIAS`, `ANCHORGUARD_KEY_PASSWORD`
- `ANCHORGUARD_JWT_SECRET`, `ANCHORGUARD_DB_URL`, `ANCHORGUARD_DB_USER`, `ANCHORGUARD_DB_PASSWORD`
- `ANCHORGUARD_API_BASE_URL` (client: `https://api.anchorguard.com/v1`)

## Clean Room Rules
- New keystore/alias names (AnchorGuard, not reference names)
- No reference cert/PEM material from `analysis/apk_extract/`
- Original artifact naming and versioning scheme
- Original script names (`verify-apk-checksum.sh`, etc.)

## Verification (environment-limited)
- No Gradle wrapper or Android SDK locally; CI cannot run until `ANDROID_HOME`/wrapper present
- Backend Docker build can be validated when Docker is available
- Checksum scripts are shell-based and testable locally
