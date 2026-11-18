# Sideload APK Distribution (Module 08)

AnchorGuard distributes Android builds as sideloaded APKs (no Play Store). This document covers artifact naming, signing prerequisites, checksums, and installation.

## Versioning
- `app/build.gradle.kts`:
  - `versionCode` = integer incremented for every release (current: `1`)
  - `versionName` = semver string (current: `0.1.0`)
- Release artifact name: `AnchorGuard-v<versionCode>-<versionName>-release.apk`  
  Example: `AnchorGuard-v1-0.1.0-release.apk`

## Signing (clean room)
- Generate a **new** AnchorGuard release keystore (never reuse reference product material):
  ```bash
  keytool -genkeypair -v \
    -keystore anchorguard-release.jks \
    -alias anchorguard \
    -keyalg RSA -keysize 2048 -validity 10000
  ```
- **Do not commit** `anchorguard-release.jks`, passwords, or `local.properties`.
- `app/build.gradle.kts` reads release signing values from `local.properties` or environment variables:
  - `ANCHORGUARD_KEYSTORE_FILE` (absolute path recommended)
  - `ANCHORGUARD_KEYSTORE_PASSWORD`
  - `ANCHORGUARD_KEY_ALIAS` (recommended: `anchorguard`)
  - `ANCHORGUARD_KEY_PASSWORD`
- Template: `local.properties.example` (copy to `local.properties`, gitignored).
- If signing values are missing, local `assembleRelease` may produce an unsigned release artifact; CI should provide secrets for signed distribution.
- Debug and release must use separate keystores/credentials for real distribution.

## Build and package
```bash
./gradlew :app:assembleRelease
./scripts/package-release-apk.sh
```
`package-release-apk.sh` copies the release APK from `app/build/outputs/apk/release/` to `app/release/` using the AnchorGuard naming scheme and generates `*.apk.sha256` and `*.apk.sha512` (requires `sha256sum`/`sha512sum` or `shasum`).

`app/release/` is gitignored except `.gitkeep`; publish artifacts via CI/GitHub Releases, not source control.

## Checksums
- Generate:
  ```bash
  sha256sum AnchorGuard-v1-0.1.0-release.apk > AnchorGuard-v1-0.1.0-release.apk.sha256
  sha512sum AnchorGuard-v1-0.1.0-release.apk > AnchorGuard-v1-0.1.0-release.apk.sha512
  ```
- Verify:
  ```bash
  ./scripts/verify-apk-checksum.sh app/release/AnchorGuard-v1-0.1.0-release.apk
  ./scripts/verify-apk-checksum.sh --dir app/release
  ```
  Script accepts `.sha256`/`.sha512` files containing `<hash>  <filename>` or `<hash>`.

## Install (sideload)
1. Enable **Install unknown apps** for the installer/source app on the child device.
2. Transfer APK and checksum files to the device (or verify on host before transfer).
3. Verify checksum before install:
   ```bash
   ./scripts/verify-apk-checksum.sh AnchorGuard-v1-0.1.0-release.apk
   ```
4. Install via ADB (recommended for controlled rollout):
   ```bash
   adb install -r AnchorGuard-v1-0.1.0-release.apk
   ```
5. Launch AnchorGuard and complete onboarding/pairing per app flow.

## Security notes
- Verify checksums from a trusted channel before install.
- Keep release keystore and passwords in a secret manager/CI secrets; rotate/backup securely outside repo.
- `isMinifyEnabled = true` for release; ProGuard/R8 rules live in `app/proguard-rules.pro`.
- This MVP distribution path is parent-managed sideloading; enterprise/MDM provisioning is documented separately in `docs/mdm-provisioning.md`.
