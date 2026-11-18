# Module 00 — Project Overview & Architecture

## Project Identity
- **Product name:** AnchorGuard
- **Package name:** `com.anchorguard.client` (Android child device)
- **Web console domain:** `app.anchorguard.com` (parent management only)
- **Backend service name:** anchorguard-api
- **Clean room rule:** No code, assets, strings, or UI layouts are copied from the analyzed reference product. All names, colours, icons, and workflows are original. The word "Kids Place", "Kidsplace", "Kiddoware", "Guardify" must never appear in source, assets, docs, or metadata.

## Scope (MVP)
- Android phone + tablet client (child device)
- Web parent console (management only)
- Self-hosted REST API + PostgreSQL
- Parent email/password account + device pairing code
- Full Android enforcement: Device Admin, Accessibility, foreground service, app blocking
- No web/DNS filtering in MVP (deferred)

## Architecture Diagram (logical)
```
Parent Web Console (React/Vue)  <-->  AnchorGuard API (REST)  <-->  PostgreSQL
                                             ^
                                             | FCM / push (optional, deferred)
                                             v
Android Child Client (Jetpack Compose)  <-->  AnchorGuard API
```

## Client Modules
| Module | Responsibility |
|---|---|
| `client.app` | Application class, DI, initialization, lifecycle |
| `client.ui` | Jetpack Compose screens, design system, navigation |
| `client.enforcement` | Device Admin, Accessibility, foreground service, app blocker, overlay |
| `client.data` | Room local DB, repository, API client, sync |
| `client.auth` | PIN, biometric fallback, parent gate, pairing |
| `client.receiver` | Boot receivers, alarm receivers, package observers |

## Server Modules
| Module | Responsibility |
|---|---|
| `api.auth` | Parent registration, login, JWT/session, password reset |
| `api.devices` | Device registration, pairing, heartbeat, configuration push |
| `api.policy` | App allow/block rules, schedules, time limits, device-level settings |
| `api.reports` | Usage ingestion, app launch events, screen time aggregates |
| `api.account` | Parent profile, subscription, billing (deferred), data export/delete |

## Tech Stack
| Layer | Technology |
|---|---|
| Android client | Kotlin, Jetpack Compose, Room, Hilt/Dagger, WorkManager, Foreground Service, DevicePolicyManager, AccessibilityService |
| API | Java 21 / Kotlin, Spring Boot or Ktor, PostgreSQL, Flyway migrations, JWT |
| Web console | TypeScript, React or Vue, TanStack Query / Pinia, Chart.js (deferred) |
| Infrastructure | Docker, Docker Compose, Nginx reverse proxy, Let's Encrypt TLS |
| CI/CD | GitHub Actions (APK build, sign, test, API deploy) |

## Clean Room Boundaries
- Do not reference package `com.kiddoware.*`, `com.kidsplace.*`, or any obfuscated class names from the reference APK.
- Do not reuse the reference APK's signing certificate (`CN=Paresh Joshi`). Generate a new keystore.
- Do not reuse endpoint paths from the reference (`/kw_backend_new/public/api/kp/api/...`). Design your own REST surface.
- UI: original colour palette, typography, iconography, and component library. Do not replicate `#5C49E0` purple theme exactly; choose a distinct AnchorGuard brand.
