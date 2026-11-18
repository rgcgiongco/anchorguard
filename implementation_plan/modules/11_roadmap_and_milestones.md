# Module 11 — Roadmap & Milestones

## Overview
6-week solo development roadmap for AnchorGuard MVP, with phases, milestones, risks, and post-MVP priorities.

## Assumptions
- Solo developer, ~15–20 hrs/week
- Android client + self-hosted backend + web parent console
- MVP: sideload APK, parent console web, core enforcement (Device Admin, overlay, watchdog)
- Web filtering deferred
- No Google Play Store distribution

## Phase 1 — Foundation (Weeks 1–2)
Modules: 00 (overview), 01 (mobile client scaffold), 02 (backend API), 05 (database/data model)

### Week 1
- Scaffold Android project (Gradle, Compose, Room, Hilt, Retrofit, WorkManager)
- AndroidManifest with com.anchorguard.client package, minSdk 29/targetSdk 36
- Navigation host with route placeholders (Splash, Welcome, Auth, Pin, Pairing, ChildHome, ParentGate, ParentDashboard, DeviceSettings, Security, Users, Account)
- Backend: Spring Boot/Ktor project, PostgreSQL, Flyway V1, auth controllers (register/login/refresh/logout)
- Database: PostgreSQL parents/devices tables + Room entities/DAOs

### Week 2
- Android: placeholder screens (Compose, original design tokens from Module 09)
- Backend: device register/pair/heartbeat endpoints, pairing code generation
- Networking: unified Retrofit/OkHttp client, auth interceptor
- Navigation wiring between placeholder screens
- Milestone M1: App builds, navigates between screens, backend auth + device register respond

## Phase 2 — Core Auth & Pairing (Weeks 3–4)
Modules: 06 (auth & pairing), 07 (networking & communication)

### Week 3
- Android: PIN create/verify/change (encrypted persistence), brute-force lockout, biometric fallback
- Pairing screen: 6-char code, API call, error handling
- Secure token storage (EncryptedSharedPreferences)
- Auth refresh/retry via OkHttp Authenticator

### Week 4
- WorkManager sync: usage upload + policy download workers
- Offline queue: unsynced usage events in Room, retry with backoff
- Connectivity gating
- Backend: /v1/usage/batch, /v1/policies endpoints
- Milestone M2: Parent can register, log in, pair device, PIN protected, sync works online

## Phase 3 — Enforcement Engine (Week 5)
Module: 04 (enforcement engine)

- AnchorGuardService foreground service with notification
- App blocker overlay (SYSTEM_ALERT_WINDOW)
- DevicePolicyManager DeviceAdminReceiver
- Watchdog loop (polling, configurable interval)
- Boot persistence (RECEIVE_BOOT_COMPLETED)
- Package observation (QUERY_ALL_PACKAGES, new install detection)
- Enforcement state machine (IDLE/RUNNING/BLOCKED/PARENT_MODE)
- Milestone M3: Blocked app triggers overlay, service survives reboot, Device Admin active

## Phase 4 — Web Console & Deployment (Week 6)
Modules: 03 (web parent console), 08 (deployment & distribution)

- Web console: shell (sidebar/header/content), login, dashboard, devices, device detail, apps, schedule, security, reports, account
- API integration with backend endpoints
- New signing keystore (anchorguard-release.jks, not committed)
- Sideload APK naming + SHA-256/SHA-512 checksums
- CI/CD: GitHub Actions (Android build/test/lint, backend Docker build)
- Docker Compose backend deployment
- Milestone M4: MVP complete — signed APK + checksums published, backend deployed, web console functional

## Milestones Summary
| Milestone | Week | Criteria |
|-----------|------|----------|
| M1 | 2 | App builds, navigates, backend auth + device register respond |
| M2 | 4 | Pairing works, PIN protected, sync online/offline |
| M3 | 5 | Enforcement active (overlay, Device Admin, boot persistence) |
| M4 | 6 | MVP release: signed APK, checksums, CI/CD, web console, backend deployed |

## Risks & Mitigations
| Risk | Impact | Mitigation |
|------|--------|------------|
| Device Admin/overlay restrictions on newer Android | High | Test on API 29, 34, 36; document limitations |
| Battery optimization killing foreground service | High | Request ignore battery optimizations, foreground service notification |
| No Gradle wrapper/Android SDK locally | Medium | Set up CI with android-actions; develop on machine with SDK |
| Web filtering deferred (user expectation) | Low | Clearly document as post-MVP; MVP is app-level blocking |
| Single developer bandwidth | High | Scope strictly to MVP; cut non-essentials |

## Post-MVP (Months 2–3)
- Web filtering (DNS/local VPN or accessibility-based URL blocking)
- MDM/provisioning (Device Owner, enrollment QR)
- Tablet-optimized layouts (split navigation, larger touch targets)
- Additional enforcement (time-of-day schedules per app, location-based rules)
- Push notifications (FCM) for parent alerts
- iOS client (future, separate codebase)
- App Store/Play Store distribution evaluation

## Clean Room Rules
- All milestone names, phase names original AnchorGuard
- No reference product roadmap or milestone names

## Deliverables
- This roadmap document
- CI/CD workflow files (created in Module 08)
- Release checklist for M4
