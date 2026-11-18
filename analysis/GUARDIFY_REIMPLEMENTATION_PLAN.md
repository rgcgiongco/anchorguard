# Guardify — Clean Reimplementation Plan (Step-by-Step)

**Source spec:** `analysis/GUARDIFY_ANALYSIS_REPORT.md` (static REA 6.9.0 analysis of `Guardify_custom001.apk`, 2026-11-17; APK never executed).  
**Analyzed artifact (for reference only):** internally **Kids Place** `com.kiddoware.kidsplace` v3.10.15 (965), minSdk 29 / targetSdk 36, 27.76 MB, 3 DEX, 11,835 classes, no native libs, R8/ProGuard obfuscated, Flutter/RN/Unity none, custom v2-only self-signed cert (`CN=Paresh Joshi`).  
**Important:** this plan treats the analyzed app as a **behavioral/feature blueprint**, not a codebase to port. All implementation must be original, owned, and compliant.

---

## 0. Guiding decisions (resolve before coding)

These choices drive architecture, contracts, cost, and schedule. Record outcomes in a decision log.

| # | Decision | Options / notes | Report refs |
|---|---|---|---|
| 0.1 | **Product scope (MVP vs parity)** | MVP (core launcher + blocking + PIN) vs near-parity with all features in §7 | §7 |
| 0.2 | **Package / applicationId** | e.g. `com.kiddoware.guardify` (clean lineage). No in-place upgrade from `com.kiddoware.kidsplace` is possible without original signing key; plan as a new install/product | §1, §2 |
| 0.3 | **Signing & distribution** | Generate and escrow a new release keystore; define Play App Signing vs self-managed; enterprise sideload channel if needed | §2 |
| 0.4 | **Platform baseline** | Keep minSdk 29 / targetSdk 36 parity or adjust; account for FGS, VPN, accessibility, exact alarms, background limits | §1, §3 |
| 0.5 | **Backend ownership** | Build new Guardify API/control plane vs adapt existing Kiddoware services (requires access/contracts). Do not assume reuse of `app.kiddoware.com/kw_backend_new/...` | §5.1–§5.2 |
| 0.6 | **Firebase ownership** | New Firebase/GCP project (`guardify-*`) for FCM, Firestore, Auth, Remote Config, Crashlytics/Analytics, Storage, reCAPTCHA; no access assumed to `kids-place` | §4.8, §5.3 |
| 0.7 | **Safe-browsing/DNS infra** | Operate new proxy/rules service (replacing `kpsbproxy.kiddoware.com`) and DNS filtering stack | §4.6, §5.1 |
| 0.8 | **Licensing/billing/SSO** | Play Billing products/SKUs, entitlement server, SSO strategy (replace `licensing.kiddoware.com`, `com.kiddoware.library.*`) | §5.1, §7 |
| 0.9 | **Companion apps/plugins** | Rebuild, replace, or drop: Safe Browser, Video Player, Picture Viewer, `safebrowsingvpn` plugin; define IPC contracts or remove cross-promo | §3.3, §4.6, §5.4 |
| 0.10 | **Parent control plane** | New web portal/console + push command contract (replaces `kidsplace.kiddoware.com` and `kiddoware.kpsbcontrolpanel.PUSHDEVICECONFIG`) | §4.7, §5.1 |
| 0.11 | **Device identity** | Define privacy-compliant device/install ID. Original returned raw `ANDROID_ID` (hash computed but discarded) — **do not copy**; design migration-safe ID and server binding | §4.9, §8.5 |
| 0.12 | **Data/migration stance** | New product = no automatic migration from Kids Place installs/cloud. Decide if any assisted import is possible (requires access to legacy data — currently unavailable) | §4.8–§4.9, §6 |
| 0.13 | **Compliance & legal** | COPPA/GDPR-K/age-appropriate design as applicable; parental-control disclosures; DPIA; privacy policy/EULA/terms/permissions explainer for Guardify domains | §3.3, §4.2, §5.5, §7, §8.4 |
| 0.14 | **Tech stack** | Kotlin-first, modern AndroidX, single-module or multi-module; DI (Hilt/Koin), coroutines/Flow, Compose or Views (plan must cover all screens) | — |
| 0.15 | **Security posture improvements** | Fix issues observed in original: exported components/receivers without permission, debug flag, ID handling, cleartext assets, provider exposure (§8). Treat as requirements, not optional | §3.1–§3.4, §8 |
| 0.16 | **Hosting/VPS topology & sizing** | Decide VPS vs managed PaaS for API/portal and for DNS/filtering proxy; choose regions, env separation (dev/stage/prod), staging mirror, and initial specs with a scale-up path. DNS filtering is latency-sensitive and should be isolated from DB as load grows | §1.3, §5.1, §6, §7 |

**Exit criteria:** signed decision log, MVP feature list, budget/timeline, owners for app/backend/infra/legal, and initial hosting/VPS plan.

---

## 1. Prerequisites & project setup

### 1.1 Organization, accounts, and infrastructure
- [ ] Create Guardify brand assets source (name, logo, color/type system) and legal entity/process for publishing.
- [ ] Provision **source control** (mono-repo or split app/backend/infra) with branch protection and CI.
- [ ] Provision **Google Play Console** (if distributing on Play), developer account, app record, and policy declarations for parental controls, VPN, accessibility, device admin.
- [ ] Provision **Firebase/GCP project** (dev/stage/prod): enable FCM, Firestore, Authentication, Remote Config, Crashlytics, Analytics (privacy-gated), Cloud Storage, App Check/reCAPTCHA as needed.
- [ ] Provision **backend hosting** for components not covered by managed services (see §1.3): API/control plane, auth, licensing/entitlements, rules/config sync, reporting ingest, parent portal, and **DNS/filtering proxy** infra for safe browsing. Decide per component: VPS vs managed PaaS/containers; self-hosted vs managed DB/cache/object storage.
- [ ] Provision **VPS/compute resources** per §1.3 (dev/stage/prod), including static IPs, regions close to target users, DNS records, and provider account/billing.
- [ ] Provision **domains/TLS/certificates** (app API, portal, proxy, licensing, legal pages, asset CDN) and secrets management.
- [ ] Provision **asset hosting/CDN** for Lottie/animations and wallpapers (replace S3 `kidsplace...` and `kids-place.appspot.com` paths).
- [ ] Establish **observability** (structured logging, metrics, tracing, alerting) and incident process.

### 1.2 Repository bootstrap (Android app)
- [ ] Create Android project: `applicationId` per 0.2, namespace/packages `com.kiddoware.guardify.*` (or chosen), Kotlin, Gradle Kotlin DSL, version catalog.
- [ ] Configure **minSdk 29, target/compile 36** (or decided), Java/Kotlin toolchain, R8/proguard for release, reproducible builds.
- [ ] Add baseline modules (suggested): `:app`, `:core:common`, `:core:data` (Room/datastore), `:core:network`, `:core:ui`, feature modules as scope grows.
- [ ] Integrate **Firebase** via new `google-services.json` (per env/flavor) — never reuse `kids-place` config.
- [ ] Set up **DI**, navigation, theming, logging, and error handling scaffolding.
- [ ] Set up **CI**: lint, unit tests, build, signed artifact pipeline (keystore in secure store), dependency/license scanning.
- [ ] Create **flavors/environments** (dev/stage/prod) for API base URLs, Firebase projects, feature flags.

**Deliverables:** buildable skeleton app, CI green, empty branded shell.

### 1.3 VPS and hosting infrastructure

Plan server infrastructure for components Firebase/managed services do not cover (see 0.5–0.7, §5.1, §6–§8). A VPS is a practical default for MVP/small prod; managed PaaS/containers/DB can replace parts of it. Specs depend on device count, reporting frequency, and DNS query volume — start small, monitor (CPU/RAM/disk, DB size, QPS, DNS latency/error rate), and scale.

#### 1.3.1 Workloads requiring hosted compute
| Component | Hosting need | Notes |
|---|---|---|
| **Core API / control plane** | VPS or managed compute (**required**) | Device registration/handshake, auth, policy/config sync, reporting ingest, remote-command ack, licensing/entitlements integration (replaces `app.kiddoware.com/...` flows) |
| **Parent portal / web console** | VPS or managed/static+API (**required**) | Account/device linking, policy/schedule editing, reports, remote actions |
| **DNS filtering proxy/resolver** | VPS/compute (**required**, latency-sensitive) | Backs `VpnService` DNS filtering (replaces `kpsbproxy.kiddoware.com`); needs stable IP/endpoints and capacity for device DNS traffic |
| **Database/cache/queues** (if self-hosted) | VPS or managed DB | Postgres/MySQL, Redis, job queue for API/reporting workloads |
| **FCM, Remote Config, Auth, Crashlytics/Analytics** | Managed (Firebase/GCP) | Not hosted on VPS |
| **Storage/assets/CDN** | Managed or object storage/CDN | Firestore/Cloud Storage or equivalent; Lottie/wallpapers via CDN |

#### 1.3.2 Recommended topology & sizing
| Tier | Role | vCPU | RAM | Storage | Network/transfer | Notes |
|---|---|---:|---:|---:|---|---|
| **MVP / Dev / Test** (<~100–500 devices) | **All-in-one** (API + portal + self-hosted DB/cache + light jobs) | 2 | **4 GB recommended** (2 GB tight but possible for minimal/light testing with swap 1–2 GB) | 80 GB NVMe | ≥2 TB/mo, ~1 Gbps | Keep stack lean; avoid colocating DNS proxy except light testing; monitor for OOM/memory pressure |
| **Small prod** (~500–5k devices) | **API + portal** | 2–4 | 4–8 GB | 80 GB NVMe | ≥3–4 TB/mo | Stateless app behind TLS/reverse proxy |
| | **Database** (if self-hosted) | 2 | 4–8 GB | 160 GB NVMe | private network preferred | Postgres + backups/PITR; separate from app or use managed DB |
| | **DNS/filtering proxy** | 2 | 2–4 GB | 40 GB NVMe | ≥2–4 TB/mo, low latency | **Latency-sensitive**; place near users, stable/static IP, isolate from DB; plan 2 nodes/failover as you grow |
| **Growth** (>~5k devices or multi-region) | **App nodes** | 4+ each | 8 GB+ | 80 GB+ | as needed | Horizontal scale behind load balancer |
| | **DNS/filtering** | 2–4 per node | 4 GB+ | 40–80 GB | high/uncapped preferred | Multi-region, health-checked failover/anycast-style DNS strategy |
| | **DB/cache** | managed or 4+ | 16 GB+ | 320 GB+ NVMe | private network | Managed Postgres/Redis preferred; replicas/backups |

#### 1.3.3 VPS provisioning & environment checklist
- [ ] **Provider/region:** choose provider(s) (e.g. Hetzner/DigitalOcean/Linode/Vultr/Lightsail or equivalent), region(s) near target users, and account/billing; enable provider DDoS protection where available (esp. for DNS/proxy).
- [ ] **Compute spec:** 64-bit Linux (Ubuntu 22.04/24.04 LTS), NVMe SSD, static **IPv4** (and IPv6 where supported), adequate bandwidth/transfer; right-size per §1.3.2.
- [ ] **Environment separation:** provision **dev/stage/prod** hosts (stage can be downsized, e.g. 1–2 vCPU / 2–4 GB) with isolated networks, DNS names, credentials, and data.
- [ ] **OS/runtime stack:** OS hardening/patching, Docker/Compose (or chosen runtime), reverse proxy/TLS termination (nginx/Caddy/Traefik), process supervision.
- [ ] **Networking & DNS:** DNS records for API/portal/proxy endpoints, TLS certificates (automated renewal), firewall rules (allow only required ports: 80/443 and DNS ports/protocols needed by filtering design; restrict DB/admin ports), private networking between app/DB where possible.
- [ ] **Security hardening:** SSH key-only access, non-root deploy user, fail2ban/rate limiting, secrets management (no secrets in repo/images), least-privilege service accounts, timely patch/upgrade policy.
- [ ] **Data & backups:** automated DB backups with off-host copies and tested restore (PITR for prod DB), disk snapshots before upgrades, backup/retention policy.
- [ ] **Observability & ops:** metrics/alerts (CPU, RAM, disk, load, app health, DNS latency/error rate), centralized logs with rotation, uptime checks, deploy/rollback runbook.
- [ ] **DNS/filtering-specific:** prioritize network quality/latency over raw CPU, capacity for bursty query traffic, rate limiting/abuse controls, isolate from DB, and plan redundancy/failover for production.
- [ ] **Capacity review:** define thresholds and upgrade/scale triggers based on observed usage.

**Deliverables:** provisioned dev/stage/prod VPS/hosting with DNS/TLS, hardened baseline, backups, monitoring, and documented runbooks.

---

## 2. Identity, manifest foundation, and app shell

Implement Guardify branding and a secure component baseline (improving on §8 where noted).

### 2.1 App identity & resources
- [ ] Define `@string/app_name`, home/title strings, notification channel names/descriptions, and full locale strategy.
- [ ] Create launcher icons (adaptive/monochrome), notification icons, VPN/FGS icons, splash/brand theme.
- [ ] Implement home/launcher UI shell (replacing Kids Place home) and navigation skeleton for all MVP surfaces.
- [ ] Implement block/interstitial UI (spec reference: `R.layout.block_popup` overlay concept, §4.3) with Guardify copy/visuals.
- [ ] Remove/avoid debug flags equivalent to `xanadu_debug=true` (§3.4, §8.4).

### 2.2 Application class & startup
- [ ] Implement `GuardifyApplication` startup: strict-mode (debug), DI init, notification channels (user-visible, appropriately described — not silent-by-surprise unless justified), locale/config, privacy-gated telemetry init.
- [ ] Implement **privacy controls**: analytics/crash reporting **off by default or explicit opt-in** per policy (original gated via `Utility.PrivacySettings.*`, §4.2, §8.4); honor consent before initializing SDKs.
- [ ] Implement first-run/bootstrap flow: install/profile initialization, default categories, default user/profile as needed (§4.2, §6) — no flavor-leaked placeholders.
- [ ] Implement global crash/uncaught-exception handling with privacy-safe reporting.

### 2.3 Manifest, permissions, and exported surface (secure-by-default)
- [ ] Declare only required permissions (review each in §3.3): `INTERNET`, `ACCESS_NETWORK_STATE`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE` (+ specific types used), `QUERY_ALL_PACKAGES`/usage access as justified, VPN, overlay, device admin, accessibility, biometrics, billing, wake/lock as needed. Provide in-app rationale and comply with Play policies.
- [ ] Set `android:allowBackup` per policy (original `false`, §3.4); define cloud/local backup strategy explicitly.
- [ ] **Do not export components unless required.** If exported, protect with appropriate `permission` + `protectionLevel` (prefer `signature` for internal IPC) and validate inputs. Specifically avoid unguarded exports seen in original (`AutoStartKpReceiver`, `RemoteLockIntentService`) (§3.2, §8.2).
- [ ] Define Guardify custom permissions only if IPC is required (replacing `...KPService.permission.Access`, `...CURRENT_RUNNING_APP`) with explicit protection levels and minimal grants (§3.2–§3.3, §8.7).
- [ ] Declare deep links (`VIEW/BROWSABLE`) only for owned Guardify hosts/schemes with verification where applicable (§3.1).
- [ ] Plan FGS types and Play declarations: parental-control/monitoring, VPN/special use, media playback — match actual behavior and policy.

**Deliverables:** branded shell, manifest baseline, permission/rationale UX scaffolding.

---

## 3. Core data layer & domain model

Model informed by §6; finalize schema for Guardify (do not assume byte-for-byte parity).

### 3.1 Local persistence
- [ ] Choose storage: **Room** (SQLite) for structured data, **DataStore** for preferences/settings/flags, encrypted storage for secrets/PIN material as appropriate.
- [ ] Implement entities/DAOs/migrations (spec-derived starting set, §6):
  - **KidsApplication/App entry:** `id/rowId`, `packageName`, `className`, `title`, `displayName`, `icon/ref`, `categoryId`, `displayOrder`, `intent/launch info`, `filtered`, `selected`, `installed`, `customFilteringEnabled`, `wifiEnabled`, `installedFromStore`, `availableForUser`, `alwaysStartAsNewTask` (+ queries like by package/class, custom-filtering, user-app query).
  - **User/Profile:** id, name, PIN/credential (store hashed/derived, never plaintext), image/ref, `availableApps`/profile mapping.
  - **Category:** id, name, color/icon, editable, ordering/sectioning, app associations.
  - **Usage/launches:** app launch/session records (`AppLaunches`, `RecordAppSessions`) for reporting/limits.
  - **Auth mode/config:** `AuthenticationMode` and lock settings.
  - **Scheduler/time:** profiles, allowed-usage schedules, calendar/day models (`scheduler.calendar.*` / times provider concept).
  - **Tasks/rewards:** task DB/entities (`TaskDatabase`, task state, remote approval/result).
  - **Stories/content metadata:** catalog/characters/scenes/playback/feedback if in scope.
  - **Ads/content config cache:** cross-promo/content metadata if in scope.
  - **Device/registration:** local device record, sync state.
- [ ] Implement repository layer, transactions, and migration strategy from version 1.

### 3.2 Device/install identity
- [ ] Implement a **new** device/install identifier (random UUID/install token or server-issued), stored securely; bind to app install/signing context as appropriate. **Do not** replicate raw-`ANDROID_ID` return behavior (§4.9, §8.5).
- [ ] Define ID rotation/reset behavior and linkage to backend registration.

**Deliverables:** stable schema v1, repositories, seed/default data, tested migrations.

---

## 4. Enforcement platform (core parental-control engine)

Highest-risk/most platform-sensitive area; build iteratively and test per-OS behavior.

### 4.1 Enforcement service & watchdog
- [ ] Implement foreground **Monitoring/Enforcement Service** (spec: `KidsPlaceService`, FGS `specialUse` parental-control monitoring, §4.3): lifecycle, persistent/robust start, restart semantics, and clear user notification.
- [ ] Implement enforcement state machine: screen on/off, device locked/unlocked, active profile/schedule, “shutdown requested” handling, and start/stop triggers (spec §4.3).
- [ ] Implement foreground-app detection / policy evaluation appropriate to minSdk 29 (usage-stats based polling/observation as permitted) and schedule-aware allow/deny decisions.
- [ ] Implement **block/lock presentation**: overlay or lock activity flow when a blocked app/action is detected (spec: overlay `block_popup`, `LockActivity`/variants, §3.1, §4.3); handle overlay permission and UX fallbacks.
- [ ] Implement watchdog/heartbeat workers to keep enforcement healthy; handle OEM/background restrictions gracefully (document limitations — original relied on sticky/re-delivered service, §4.3, §8.3).

### 4.2 Accessibility service (only if justified)
- [ ] Decide necessity: original declared accessibility but `onAccessibilityEvent` was effectively inert, with enforcement driven by service/polling (§4.4, §8.3). Prefer least-privilege approach.
- [ ] If used, implement minimal, policy-compliant accessibility service, clear disclosure, and robust opt-in; do not collect unnecessary content.

### 4.3 Device admin / PIN / lock
- [ ] Implement **Device Admin** receiver and flows for enable/disable as needed (spec: `KPDeviceAdminReceiver` handling enable/password events, §4.5); provide compliant UX and recovery.
- [ ] Implement **PIN/credential management**: set/change/recover flows, secure storage (hash + salt/KDF), lockout/throttling, biometric unlock if offered (`USE_BIOMETRIC`) (§3.3, §7).
- [ ] Implement parent gate / grown-up mode and lock screens (`LockActivity`, `LockActivityWithGrownUpMode`, change/recovery activities concept, §3.1, §7).

### 4.4 Boot/startup & resilience
- [ ] Implement boot/quickboot handling to restore enforcement state per user settings and policy (`RECEIVE_BOOT_COMPLETED`, §3.1, §3.3).
- [ ] Implement battery-optimization guidance/exemption flow only if essential and policy-compliant.
- [ ] Handle app updates, force-stop, and device restart cases with explicit state recovery.

**Deliverables:** end-to-end block/allow enforcement for a managed app set under a profile/schedule, with PIN gate.

---

## 5. App management, profiles, and launcher UX

- [ ] Implement installed-app discovery (respecting package visibility rules/`QUERY_ALL_PACKAGES` policy) and app catalog sync into local DB (§6 `KidsApplication`).
- [ ] Implement app picker/selection, categories, ordering, per-app flags (filtered/selected/custom filtering/WiFi/always-start-new-task/installed-from-store as applicable) (§6, §7).
- [ ] Implement kid-facing launcher/home: allowed apps grid/list, categories, profile context, safe navigation.
- [ ] Implement profile/user management (multi-profile if in scope; default profile behavior defined explicitly).
- [ ] Implement **time/schedule management**: time profiles, allowed-usage windows, calendar/day rules, enforcement integration (§3.1, §4.1, §6–§7).
- [ ] Implement usage/session recording for limits and reporting (`AppLaunches`, `RecordAppSessions`) with privacy minimization.

**Deliverables:** usable guarded launcher with configurable allowlists/categories/schedules.

---

## 6. Networking, backend API, and sync

Build a new contract; use §5.1–§5.2 as behavioral hints only.

### 6.1 API platform
- [ ] Define Guardify REST/gRPC contracts (auth, device registration, config/policy sync, reporting ingest, content/rules metadata, licensing/entitlements). Document versioning (`prcVer`-like concept optional).
- [ ] Implement secure transport (TLS), authN/authZ (device + parent account), request signing/encryption if required (original hinted at `getKey` handshake, §5.1), rate limiting, and replay protection.
- [ ] Implement **device registration/install update** flow (replacing `getKey`/`updateInstall` semantics) bound to new device ID (§4.9, §5.1).
- [ ] Implement response/error envelope, retries/backoff, idempotency for critical calls, and offline-tolerant sync.
- [ ] Implement network security config: no unintended cleartext; pinning/allowlists per policy (original had custom `@xml/network_security_config` and cleartext icon URLs — fix, §3.4, §5.4, §8.6, §9).

### 6.2 Remote config & push
- [ ] Implement Firebase **Remote Config** (or equivalent) for feature flags/policy knobs.
- [ ] Implement **FCM** messaging: token lifecycle, topic/device targeting, secure payload handling.
- [ ] Define and implement push/command contract for remote control (replacing `PUSHDEVICECONFIG`/`SynchronizationReceiver` behavior): config apply, sync trigger, with auth/validation and auditability (§4.7, §4.8).

### 6.3 Reporting & telemetry
- [ ] Implement usage/DNS-stats/reporting upload with batching, consent gating, and data minimization (spec: `AppLaunchesReportingWorker`, `DnsStatsReportingWorker`, `ReportingService`, §4.7, §7).
- [ ] Integrate **Crashlytics/Analytics** only after consent gates; ensure manifest/SDK flags align with policy (original disabled some ad-id/collection flags, §8.4).

**Deliverables:** registered device can sync policy/config and receive remote commands against Guardify backend.

---

## 7. Web/DNS content filtering (VPN)

Complex, policy- and review-sensitive (VPN + potential accessibility/device-admin app).

- [ ] Design filtering model: DNS-based categories/allow-deny lists, safe-search enforcement as needed, per-profile rules, logging/visibility for parents (privacy-aware).
- [ ] Implement **`VpnService`**-based DNS/content filtering (spec: `DnsVpnService`, `BIND_VPN_SERVICE`, FGS, dnsjava-style DNS handling, §4.6): tunnel setup, DNS interception/forwarding, block/allow decisions, bypass prevention as feasible.
- [ ] Implement rules/config fetch and local caching; integrate with Guardify proxy/resolver/rules API (replacing `kpsbproxy.kiddoware.com`).
- [ ] Handle network changes, IPv4/IPv6, captive portals, private DNS interactions, and always-on/lockdown VPN expectations.
- [ ] Provide transparent UX/notifications and Play/VPN disclosures; avoid deceptive claims.
- [ ] If companion browser/plugin model is chosen, define secure integration; otherwise ship integrated filtering.

**Deliverables:** policy-driven DNS filtering with auditable behavior and compliant disclosures.

---

## 8. Remote control, parent experience, and cloud backup

- [ ] Implement remote commands over FCM/backend: **remote lock** (bring guard UI foreground safely), device message/ring/sound, policy refresh (spec: `RemoteLockIntentService` job concept, `SoundService`, `DialogMessageActivity`, `KPRemoteControlActivity`, §4.7) — with strong authorization, rate limiting, and anti-abuse (original had unguarded exported entry points — do not repeat, §8.2).
- [ ] Build or integrate **parent portal/app**: account auth, device linking, policy/config editing, schedules, reports, remote actions; align with push contract and APIs.
- [ ] Implement **backup/restore**: local export/import and/or cloud backup (spec: local + Firestore backup/restore services, §4.8, §7) with encryption, consent, retention controls, and conflict handling.
- [ ] Define account/device lifecycle: link/unlink, factory reset, lost-device flows.

**Deliverables:** parent can manage device remotely and recover configuration within Guardify ecosystem.

---

## 9. Extended features (scope per MVP)

Implement in priority order after core enforcement is stable.

| Feature | Spec refs | Key tasks |
|---|---|---|
| **Tasks & rewards** | §4.1, §6–§7 | Task models/DB, parent/kid UIs, completion/approval flow, optional remote approval sync |
| **Stories/content** | §4.1, §7 | Catalog delivery, download/cache, playback UI, feedback; host content on Guardify infra/CDN |
| **Age assurance/signals** | §7 | Integrate compliant age-signals/verification approach if required; isolate PII and document basis |
| **Cross-promo / content modules** | §5.4, §7 | Only for Guardify-owned products; config-driven, HTTPS, no cleartext |
| **Ads/monetization** (if any) | §5.4 | Policy-compliant, child-directed rules respected |
| **Multi-language/localization** | §1 | Full string/resource localization and RTL/layout testing |

**Deliverables:** prioritized feature increments, each with acceptance tests and privacy review.

---

## 10. Security, privacy, and compliance hardening

Address proactively (informed by §8 observations).

- [ ] **Permission minimization & review:** justify `QUERY_ALL_PACKAGES`, usage stats, accessibility, device admin, overlay, VPN, exact alarms/background — provide in-product education and policy disclosures (§3.3).
- [ ] **Component hardening:** no exported providers/services/receivers without strict necessity and signature-level protection; validate all inbound intents/extras/content URIs (§3.2, §8.2, §8.7).
- [ ] **Secrets/keys:** no hardcoded API keys in client beyond what is unavoidable/public; secure storage for tokens/credentials; rotate/expire.
- [ ] **Data protection:** encrypt sensitive local data; TLS everywhere; minimize collection/retention; secure logging (no PINs/tokens/PII).
- [ ] **Identity & auth:** robust device binding, anti-tamper checks appropriate to threat model, secure PIN hashing/KDF, rate limiting.
- [ ] **Supply chain:** dependency pinning/SBOM, vulnerability scanning, SDK review (Firebase/Play services/third-party).
- [ ] **Abuse resistance:** authorize and throttle remote commands, config pushes, and admin actions; audit logs.
- [ ] **VPS/hosting hardening:** firewall least-privilege rules, SSH key-only access, timely OS/runtime patching, secrets isolation, DDoS/rate limiting (especially DNS proxy), TLS everywhere, restricted DB/admin surfaces, and verified backup/restore procedures (§1.3).
- [ ] **Compliance artifacts:** privacy policy, DPA/DPIA as needed, consent flows, data subject/export/delete processes, store data-safety forms.

**Deliverables:** threat model, security checklist sign-off, pen-test/remediation plan (as applicable).

---

## 11. Testing & quality assurance

### 11.1 Automated testing
- [ ] Unit tests: domain logic, policy/schedule evaluation, ID/registration, parsing/envelope handling, migrations.
- [ ] Integration tests: Room migrations, repositories, API clients (mock server), FCM/command handling, VPN/rule evaluation (where feasible).
- [ ] UI tests: onboarding/permissions, launcher, PIN/lock, block flows, parent settings.

### 11.2 Manual/system testing matrix
- [ ] Devices/OS: Android 10 (API 29) through current target (36) across major OEMs (Samsung/Xiaomi/etc.) for background/FGS/VPN/accessibility/device-admin quirks.
- [ ] Core flows: install/first-run, grant permissions, profile/category/app selection, allow/block enforcement, overlay/lock correctness, schedule transitions, timezone/DST changes.
- [ ] Security/lock: PIN set/change/recovery, biometric, device-admin enable/disable, attempt-to-bypass scenarios (within ethical/test scope).
- [ ] Resilience: reboot, app update, force-stop, crash/restart, low memory, no network/offline, captive portal.
- [ ] VPN/filtering: enable/disable, DNS blocking/allowing, network switch (Wi‑Fi/cellular), IPv6, private DNS, always-on VPN interactions.
- [ ] Remote/control: FCM delivery, remote lock/sound/config apply, authz failures, rapid/replayed commands.
- [ ] Cloud: registration, sync, backup/restore, account/device unlink, consent toggles for analytics/crash reporting.
- [ ] Billing/licensing: purchase/restore/entitlement expiry, grace/offline behavior.
- [ ] Accessibility & localization: screen readers, font scaling, RTL/locales.

### 11.3 Pre-release gates
- [ ] Performance/battery profiling (polling/watchdog/VPN impact).
- [ ] Play policy / store review readiness (sensitive permissions declarations, VPN/accessibility forms if used).
- [ ] Legal/privacy review sign-off and production secrets/config verified.

**Deliverables:** test plan, traceability to features, release-candidate sign-off report.

---

## 12. Release, rollout, and operations

- [ ] Finalize **versioning, release notes, store listing, screenshots, privacy/data-safety disclosures** under Guardify brand.
- [ ] Produce **signed release artifacts** (AAB/APK) with production Firebase/backend endpoints and hardened config (debug off).
- [ ] Staged rollout/canary, monitoring dashboards, crash/ANR/error budgets, and rollback criteria.
- [ ] **Migration/support posture:** communicate that this is a new Guardify install/lineage (no keystore/source continuity with analyzed build); provide parent guidance for setup and, if ever feasible, assisted data import.
- [ ] Post-launch: on-call/ops runbooks, remote-config/kill-switch procedures, update cadence, and feedback loop into backlog.
- [ ] **Infrastructure operations:** maintain VPS/hosting capacity and scaling plan (per §1.3), monitor app/DNS/DB health and alerts, rotate secrets/certificates, apply OS/runtime security patches, verify backups/restores, and execute failover/rollback procedures for API, portal, and DNS proxy.

**Deliverables:** launched Guardify v1, ops playbook (including VPS runbooks/backup-restore/failover), measured KPIs.

---

## 13. Suggested phased timeline (indicative)

| Phase | Focus | Depends on | Exit signal |
|---|---|---|---|
| **P0 — Foundations** | Decisions (Sec 0, incl. 0.16 hosting), accounts/infra, VPS provisioning per §1.3 (dev/stage/prod, DNS/TLS, backups/monitoring), repo/CI, app skeleton, Firebase/backend stubs | Org access, budget | Build+CI green, envs + VPS/hosting provisioned |
| **P1 — Core enforcement MVP** | Data model, launcher/catalog, PIN/lock, monitoring + block flow, schedules (Secs 2–5) | P0 | Device can enforce allow/block locally |
| **P2 — Cloud & control** | API/registration/sync, FCM remote commands, parent portal basics, reporting (Secs 6, 8) | P1, backend | Remote policy/lock works end-to-end |
| **P3 — Filtering & resilience** | DNS VPN/filtering, boot/resilience hardening, OEM tuning (Secs 4.4, 7) | P1–P2 | Compliant filtering on test matrix |
| **P4 — Extended features** | Tasks/stories/age/backup depth, plugins or replacements (Secs 8–9) | P2–P3 | MVP+ features accepted |
| **P5 — Hardening & launch** | Security/compliance, QA matrix, store/policy readiness, rollout (Secs 10–12) | P4 | Production release |

---

## 14. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| No original key/source/config | Cannot update or fully replicate legacy behavior/cloud state | Commit to new lineage (this plan); focus on owned implementation |
| Platform/background restrictions (OEM) | Enforcement gaps | Robust FGS/watchdog, OEM-specific guidance/testing, transparent UX, graceful degradation |
| VPN/accessibility/device-admin policy scrutiny | Store rejection/compliance issues | Minimal permissions, clear disclosures, policy-aligned features, pre-review checks |
| DNS filtering complexity & liability | Breakage or unsafe browsing claims | Conservative defaults, rigorous testing, clear scope/limits, legal review |
| Child privacy/regulatory exposure | Legal/reputational | Privacy-by-design, consent gates, minimization, audits, counsel |
| Backend/proxy/portal scope underestimated | Schedule slip | Parallelize app/backend tracks, stub contracts early, prioritize MVP |
| Companion plugin dependency | Feature gaps | Decide replace/drop early; avoid blocking core on third-party packages |

---

## 15. Immediate next actions (concrete)

1. **Approve decisions in Section 0** (esp. 0.1–0.3, 0.5–0.6, 0.10–0.11) and nominate owners.
2. **Create repositories and provision** Firebase/GCP + dev/stage projects + CI; provision VPS/hosting per §1.3 (dev/stage/prod, static IPs, DNS/TLS, firewall, backups, monitoring) (Sec 1).
3. **Stand up Guardify API skeleton** on staging VPS (device register, config fetch, command ack) with DB/reverse proxy/TLS to unblock app integration (Sec 6, §1.3).
4. **Bootstrap Android app** with chosen package/signing, manifest baseline, and branded shell (Secs 1.2, 2).
5. **Implement core enforcement MVP** (Secs 3–5) before VPN/extended features.
6. **Maintain a traceability matrix**: report feature (§7) → requirement → implementation → test case.

---

*This document is an implementation roadmap derived from static analysis evidence; items marked as “spec reference” indicate observed behavior in the analyzed artifact and must be re-specified and validated for Guardify.*