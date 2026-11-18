# Module 10 — Testing & QA

## Scope
Test strategy, test types, tools, coverage targets, and release criteria for AnchorGuard MVP. Original test classes and test data (no reference test names/data).

## Test Types

### Unit Tests (JVM, JUnit 5 + Kotlin)
- ViewModels: state emission, user actions, error handling
- Use cases / repository logic: PIN validation, pairing code format, sync merge
- Policy engine: block/allow decisions, schedule matching, daily limit calculation
- Token utils: pairing code generation, JWT validation (backend)
- Location: `app/src/test/java/com/anchorguard/client/...` and `backend/src/test/...`
- Framework: JUnit 5, MockK (Android), Mockito (backend)

### Instrumented Tests (Android device/emulator)
- Device Admin activation flow
- Overlay display when blocked app launches
- Foreground service persistence (boot receiver)
- PIN create/verify/change persistence
- Pairing flow end-to-end (with mock backend or test container)
- Location: `app/src/androidTest/java/com/anchorguard/client/...`
- Framework: Espresso + Compose Test + UI Automator

### Backend Tests
- Controller tests: auth endpoints, device endpoints, sync endpoints
- Service tests: token generation, pairing validation, policy merge
- Repository tests: Flyway migration integrity, repository queries
- Framework: JUnit 5, MockMvc, Testcontainers (PostgreSQL)

### End-to-End (E2E)
- Parent registers → logs in → pairs device → sets policy → child device enforces → web console shows usage
- Location: `e2e/` or `web-console/e2e/` (Playwright or Cypress)
- Framework: Playwright (web), Espresso (Android), or combined

## Tools & Config
- Android: JUnit 5, MockK, Espresso, Compose Test, UI Automator, Robolectric (optional)
- Backend: JUnit 5, Mockito, MockMvc, Testcontainers, Jacoco
- Web: Vitest, React Testing Library, Playwright
- CI: GitHub Actions (lint, unit tests, instrumented tests on emulator, backend tests)

## Coverage Targets (MVP)
- Unit test coverage: >= 60% (client + backend)
- Instrumented tests: critical enforcement paths (overlay, Device Admin, boot)
- E2E: core pairing + enforcement flow
- No coverage gate blocking release in MVP; target increases post-MVP

## Test Data
- Original AnchorGuard test accounts, devices, packages
- No reference product test data or account names
- Test PINs: `1234`, `0000` (clearly marked as test-only)

## Release Criteria (MVP)
- All unit tests pass (`./gradlew testDebugUnitTest`, `./gradlew test` backend)
- Lint passes (`./gradlew lintDebug`, `./gradlew ktlint`)
- Backend tests pass (`./gradlew test` or `mvn test`)
- E2E pairing + enforcement flow passes on emulator
- No critical/severe lint issues
- Crash-free on target emulators (API 29, 34, 36)

## CI/CD Integration
- `.github/workflows/ci.yml`:
  - Android: unit tests + lint + assemble debug/release
  - Backend: unit tests + Docker build
  - Web: unit tests + lint + build
  - Instrumented tests: run on Firebase Test Lab or local emulator (if available)
- Test reports published as CI artifacts

## Clean Room Rules
- Test class names original AnchorGuard names
- No reference test class/method names
- Test data original to AnchorGuard

## Deliverables
- Unit tests for ViewModels, use cases, policy engine, token utils
- Instrumented tests for enforcement, PIN, pairing
- Backend controller/service/repository tests
- Web console component tests
- E2E core flow test
- CI workflow file(s)
