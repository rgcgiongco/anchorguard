# Module 12 — Final Integration & Release

## Scope
End-to-end integration across all modules, final QA, release preparation, and post-release monitoring for AnchorGuard MVP.

## Integration Testing
- Full pairing → policy → enforcement → web console flow (parent registers, pairs device, sets app block, child device enforces, console shows usage)
- Offline → online sync: usage events queued locally, uploaded on reconnect, policies downloaded
- Device Admin + overlay + watchdog coordination (no conflicts, service restarts on crash)
- Boot persistence: service starts on reboot, enforcement resumes
- PIN + biometric + lockout coordination across app restarts
- Web console CRUD: device policies, schedules, app rules reflect on child device
- Logout/unpair: device unbinds, enforcement stops or reverts per policy

## Final QA
- Regression: all Module 01–11 screens and flows still work
- Lint: `./gradlew lintDebug` and `./gradlew lintRelease` clean (no errors)
- Unit tests: all pass, coverage >= 60%
- Instrumented tests: enforcement overlay, Device Admin, boot receiver, PIN flow
- Backend tests: all controller/service/repository tests pass
- Web console: all pages load, forms submit, errors handled
- Device compatibility: test on API 29, 34, 36 emulators (phone + tablet form factors)
- Sideload install: `adb install -r` works, upgrade path preserves data
- Checksum verification: published SHA-256/SHA-512 match downloaded APK

## Release Preparation
- Signed release APK: `AnchorGuard-v<versionCode>-<versionName>-release.apk`
- Checksums: `.sha256` and `.sha512` files alongside APK
- Release notes: original AnchorGuard changelog (no reference product language)
- Install instructions: enable unknown apps, `adb install -r`, grant permissions (Device Admin, overlay, usage access, accessibility, battery optimization)
- Backend: Docker Compose deployed, `.env` with secrets, Flyway migrations run, Nginx + TLS
- Web console: accessible at `app.anchorguard.com` (or local IP for self-hosted)
- API base: `https://api.anchorguard.com/v1` reachable from client

## Post-Release Monitoring
- Crash reporting (optional: capture uncaught exceptions locally)
- Backend health check endpoint: `GET /v1/health`
- Usage sync success/failure rates logged
- Device admin active count tracked
- Parent support contact (email) listed in app and console

## Release Criteria (MVP Launch)
- [ ] All unit + instrumented + backend tests pass
- [ ] Lint clean on debug and release
- [ ] E2E pairing → enforcement → console flow passes on emulator
- [ ] Signed APK + checksums generated and verified
- [ ] Backend deployed via Docker Compose, health check passes
- [ ] Web console accessible, all pages functional
- [ ] Install instructions documented
- [ ] Clean room verified: no reference product names, certs, or API paths

## Post-MVP Immediate Follow-ups
- Web filtering (deferred from MVP)
- MDM/provisioning (enterprise deployment)
- Tablet layout refinements
- Push notifications (FCM)
- Coverage target increase (>= 80%)

## Clean Room Rules
- Release artifact names original AnchorGuard
- No reference product names in release notes, install instructions, or metadata
- New keystore only (no reference cert)

## Deliverables
- Signed release APK + checksums
- Release notes (original AnchorGuard content)
- Install/user guide (original AnchorGuard content)
- Backend deployment guide (Docker Compose, .env example)
- Integration test results summary
- Release readiness sign-off
