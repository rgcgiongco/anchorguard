# MDM / Device Provisioning (Module 08, documented MVP option)

This document describes Device Owner/MDM provisioning options for AnchorGuard. **Sideload remains the primary MVP distribution path** (parent-installed on child device); provisioning automation/enrollment token UX is not fully implemented in-app.

## Prerequisites
- Factory-reset device or device in a state eligible for Device Owner provisioning (before accounts are added, per Android provisioning rules for managed setup).
- AnchorGuard release APK signed and checksum-verified.
- ADB available on host for development/controlled provisioning, or NFC/QR provisioning infrastructure for managed deployment.
- AnchorGuard package: `com.anchorguard.client`.
- Device Admin receiver/component as implemented by Module 04 enforcement (`AnchorGuardDeviceAdminReceiver` / manifest-declared admin component; confirm exact component name in built APK/manifest before production use).

## Provisioning methods (reference)

### ADB (development/controlled devices)
1. Install APK: `adb install -r AnchorGuard-v<versionCode>-<versionName>-release.apk`
2. Set Device Owner (single device admin/owner context required):
   ```bash
   adb shell dpm set-device-owner com.anchorguard.client/<admin-receiver-component>
   ```
3. Verify policy/admin state in system settings and AnchorGuard security status UI.
4. Remove Device Owner for testing/reset only when appropriate:
   ```bash
   adb shell dpm remove-active-admin com.anchorguard.client/<admin-receiver-component>
   ```

> Use the actual component name from the installed app manifest; placeholder `<admin-receiver-component>` must be replaced with the concrete receiver class.

### NFC
- Provision during device setup using an NFC provisioning app/record that installs the AnchorGuard APK from a trusted source and sets it as Device Owner/profile owner as applicable.
- Requires managed provisioning infrastructure and signed, hosted package metadata outside this repo.

### QR / enrollment token (documented, not implemented in MVP)
- Android enterprise QR provisioning can supply Wi-Fi, download location, checksum/signature, and admin extras.
- A production flow would include: enrollment server issues short-lived token, QR encodes provisioning payload, device downloads verified APK, sets Device Owner, and AnchorGuard completes pairing/activation.
- **MVP status:** flow documented only; no enrollment token service or in-app QR enrollment implemented.

## Security and operational notes
- Only provision devices you own/manage and with informed consent/policy compliance.
- Use checksum-verified APKs from trusted distribution channels.
- Device Owner grants powerful controls; protect admin credentials/PIN and document recovery/reset procedures.
- Test provisioning on non-production devices first; Android behavior varies by OEM/version.
- Sideload + manual activation remains supported for MVP households without MDM infrastructure.
