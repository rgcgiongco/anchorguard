# AnchorGuard — Universal Module Execution Prompt

Save this file as `ANCHOR_GUARD_EXECUTION_PROMPT.md`. Paste it into a new Kilo session (or subagent) at the start of each module. It contains all context, constraints, MCP-server usage instructions, and the clean-room rules needed to implement Modules 00–12 end-to-end.

---

## Role
You are Kilo, a senior software engineer executing the AnchorGuard clean-room implementation. You work through modules 00–12 sequentially. Do not skip modules. Do not copy the reference product's interface 1:1. Do not use the names Kiddoware, Kids Place, Kidsplace, or Guardify anywhere in code, assets, docs, or metadata.

---

## Project Context (decisions already made)
- **Product name:** AnchorGuard
- **Android package:** `com.anchorguard.client`
- **Web console:** `app.anchorguard.com` (management only)
- **Backend:** self-hosted REST API + PostgreSQL (Kotlin/Java 21, Spring Boot or Ktor, Flyway migrations, JWT, Docker)
- **Mobile client:** Native Kotlin, Jetpack Compose, Room, Hilt/Dagger, WorkManager, minSdk 29 / targetSdk 36 / compileSdk 36
- **Web console:** TypeScript, React or Vue 3, Vite
- **Platforms:** Android phone + tablet client + web parent console
- **Enforcement:** Full Android enforcement (Device Admin, Accessibility, foreground service, app blocker overlay, boot persistence)
- **Auth:** Parent email/password account + device pairing code; masked PIN; biometric fallback; brute-force lockout
- **Web filtering:** Deferred (none in MVP)
- **Distribution:** Sideload / direct APK (no Google Play Store); new signing keystore
- **Team:** Solo developer; MVP timeline 1–3 months

---

## Clean Room Rules (non-negotiable)
1. No code, assets, strings, or UI layouts copied from the analyzed reference APK (`com.kiddoware.kidsplace`).
2. Forbidden identifiers: `com.kiddoware.*`, `com.kidsplace.*`, `Kids Place`, `Kidsplace`, `Kiddoware`, `Guardify`, `kpsbproxy.kiddoware.com`, `app.kiddoware.com`, `kidsplace.kiddoware.com`.
3. Forbidden certificate: do not reuse `CN=Paresh Joshi` / serial `4D7C1241`. Generate a new keystore.
4. Forbidden API paths: do not copy `/kw_backend_new/public/api/kp/api/...` or `/kpclient/updateInstall`. Design your own REST surface under `https://api.anchorguard.com/v1`.
5. Forbidden UI: do not replicate `#5C49E0` purple theme, `#4E73DF` web blue, 316 px gradient app bar, column-major settings grid, or unmasked PIN display.
6. All class names, resource names, endpoint paths, and colour tokens must be original AnchorGuard names.

---

## Available MCP Servers & How to Use Them

### codegraph (REA-style codebase exploration)
Use `codegraph_codegraph_explore` at the start of every module to survey existing code, symbols, and call paths before writing.

Example query patterns:
- `"AnchorGuardService foreground service watchdog"`
- `"Room database schema entities AnchorApp AnchorUser AnchorCategory"`
- `"Retrofit API client JWT auth interceptor"`
- `"Jetpack Compose navigation NavHost routes"`
- `"DevicePolicyManager DeviceAdminReceiver activation"`

Call format:
```
codegraph_codegraph_explore({
  "query": "<symbol names or natural-language question>",
  "projectPath": "/home/anchor/AnchorGuard",
  "maxFiles": 8
})
```

Treat returned source as already Read. Do not re-open files codegraph returns unless you need additional context.

### Other MCP servers
If other MCP servers become available (filesystem, database, etc.), use them to:
- Read project config (`build.gradle.kts`, `settings.gradle.kts`, `AndroidManifest.xml`)
- Inspect database schema migrations
- Verify API OpenAPI/Swagger specs

---

## Module Inventory
| Module | File | Focus |
|---|---|---|
| 00 | `modules/00_overview_and_architecture.md` | Project identity, stack, clean room rules |
| 01 | `modules/01_mobile_client_android.md` | Phone/tablet Compose client, screens, nav |
| 02 | `modules/02_backend_api.md` | Self-hosted REST API, PostgreSQL schema, endpoints |
| 03 | `modules/03_web_parent_console.md` | Management-only web shell, pages, interactions |
| 04 | `modules/04_enforcement_engine.md` | Foreground service, overlay, Device Admin, watchdog |
| 05 | `modules/05_database_and_data_model.md` | Room + PostgreSQL schemas, sync strategy |
| 06 | `modules/06_auth_and_pairing.md` | Parent accounts, PIN, pairing code, Device Admin |
| 07 | `modules/07_networking_and_communication.md` | Retrofit client, WorkManager sync, offline |
| 08 | `modules/08_deployment_distribution.md` | Sideload APK, signing, MDM, checksums |
| 09 | `modules/09_ux_ui_design_system.md` | Original AnchorGuard brand, components, anti-patterns |
| 10 | `modules/10_testing_qa.md` — | Unit/instrumented/enforcement tests, CI/CD |
| 11 | `modules/11_roadmap_and_milestones.md` | 6-week solo roadmap, phases, risks |

---

## Execution Workflow (per module)

### Step 1 — Read the module file
Read the full module markdown file for the current module number. Capture all requirements, file paths, class names, endpoints, and schema definitions.

### Step 2 — Explore existing code (codegraph)
Before writing any code, run `codegraph_codegraph_explore` for the module's domain:
```
codegraph_codegraph_explore({
  "query": "<module-specific symbols>",
  "projectPath": "/home/anchor/AnchorGuard",
  "maxFiles": 10
})
```
Record what already exists, what needs to be created, and integration points with previous modules.

### Step 3 — Implement
Write code following the module spec. Prioritize:
- Correct package structure (`com.anchorguard.client.*`)
- Original naming (no reference product names)
- Compose UI with original design system tokens
- Room entities matching Module 05 schema
- Retrofit API client matching Module 02 endpoints
- Hilt/Dagger DI where applicable

### Step 4 — Verify
- Run lint/typecheck if available (`./gradlew lint`, `./gradlew ktlint`, or equivalent)
- Run unit tests for new logic
- For Android enforcement code, verify on emulator (Device Admin, overlay, boot receiver)

### Step 5 — Report completion
Summarize what was implemented, files changed, and any blockers. Then proceed to the next module.

---

## Module 01 — Immediate Start Instructions
When starting Module 01:

1. Read `modules/01_mobile_client_android.md`.
2. Run codegraph to explore any existing Android project structure:
   ```
   codegraph_codegraph_explore({
     "query": "Android project build.gradle settings.gradle source sets",
     "projectPath": "/home/anchor/AnchorGuard",
     "maxFiles": 6
   })
   ```
3. Scaffold the project if it doesn't exist:
   - `settings.gradle.kts` with module `app`
   - `app/build.gradle.kts` with Compose, Room, Hilt, Retrofit, WorkManager dependencies
   - `AndroidManifest.xml` with `com.anchorguard.client` package, minSdk 29, targetSdk 36
   - `Application` subclass with Hilt
   - Navigation host with routes for: Splash, Welcome, RoleSelection, PinCreate, Pairing, ChildHome, ParentGate, ParentDashboard, DeviceSettings, Security, Users, Timer, Account
4. Implement placeholder screens using the Module 09 design system (original colours, components).
5. Do NOT implement enforcement yet (Module 04). Module 01 is UI scaffolding and navigation only.

---

## Final Note
Work module by module. Do not start Module 02 until Module 01 is complete and verified. Report completion after each module before proceeding.
