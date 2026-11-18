# Reverse Engineering Report — Guardify_custom001.apk

**Analysis date:** 2026-11-17
**Target:** `C:\Users\Admin\Documents\Kids Place Clone\Guardify_custom001.apk`
**Method:** REA 6.9.0 static analysis (JADX headless 0.7.1 provider + APK signature/Flutter readers + DEX string extraction). The APK was **never executed**.

---

## 1. Identity

| Property | Value |
|---|---|
| User-visible app name | **Kids Place** (`@string/app_name`, `@string/home_title`) |
| Package | `com.kiddoware.kidsplace` |
| Version | `3.10.15` (versionCode `965`) |
| SDK | minSdk **29** (Android 10) · targetSdk **36** · compileSdk 36 |
| Size | 27,764,528 bytes |
| SHA-256 | `7f684bc0408a850e1260b6a04d05c594aa619db83f387318f954e161e4d04672` |
| Classes / resources | 11,835 classes · 2,572 resources · 3 DEX (7.6 / 7.5 / 0.5 MB) |
| Native code | **None** — no `lib/` directory; pure Java/Kotlin |
| Build type | Release, **R8/ProGuard obfuscated** (single-letter class, method and field names) |
| Flutter / React Native / Unity | None (`flutter_detected: false`, no `libapp.so`) |

> **Note on the filename:** despite the name `Guardify_custom001.apk`, there is **no** "Guardify" string anywhere in the DEX files or `resources.arsc`. Internally the app is stock **Kids Place** by Kiddoware. The filename is external labelling only.

**Evidence:** `ev_e1480e29082d8d4f9a28e3d634f80c33bc4c70169c7d746bca82bccc136790f3` (package), `ev_523bac918f21da7c9a4ba85743b0a3268d4b2bf9287b5ccb815b0d742b734ed1` (Flutter), `ev_324cac476d4d421ea2756e01673a2a18ec45a786c44d6401f4c86ca698b531d0` (class inventory)

---

## 2. Signing

| Scheme | Present | Notes |
|---|---|---|
| v1 (JAR) | ❌ | — |
| **v2** | ✅ | RSA / SHA-256, digest `matches_apk: true` |
| v3 / v3.1 | ❌ | No key-rotation lineage |
| Source stamp | ❌ | — |

- **Signer:** `CN=Paresh Joshi, O=Unknown, C=Unknown` (self-signed, serial `4D7C1241`)
- **Validity:** 2011-03-13 → 2038-07-29
- **Certificate SHA-256:** `f938caf000ba3d6d939891b5dc0364de15ca960934da9b22430da4801a038dbd`

A v2-only, self-signed certificate with a generic placeholder DN is consistent with a **custom/enterprise build** rather than a Play Store-signed release (Play App Signing would normally show a Google-managed v3 lineage). Signature validity is *not* a trust judgement.

**Evidence:** `ev_4ede3741246515934adada3a7b8dd6a59a63a1b17c2fdc2c41abb034ee1055b5`

---

## 3. Security posture of the manifest

- `android:persistent="true"`, `android:largeHeap="true"`, `android:allowBackup="false"`, `android:extractNativeLibs="false"`
- `android:networkSecurityConfig="@xml/network_security_config"` (contents not recovered — see §9)
- `android:appComponentFactory="androidx.core.app.CoreComponentFactory"`
- Debug leftover: `<meta-data android:name="xanadu_debug" android:value="true"/>`

### 3.1 Components (143 total)

| Kind | Count | Notable |
|---|---|---|
| activity | 83 | `LaunchActivity` (MAIN/LAUNCHER), `LockActivity`, `LockActivityWithGrownUpMode`, `ParseDeepLinkActivity` (VIEW/BROWSABLE), `RemoteLockActivity` |
| service | 31 | `KidsPlaceService`, `KidsPlaceAccessibilityService`, `DnsVpnService`, `RemoteLockIntentService`, `ReportingService`, `SoundService`, `ApplicationWatcherService` |
| receiver | 19 | `StartupReceiver` (BOOT_COMPLETED / QUICKBOOT), `KPDeviceAdminReceiver`, `AutoStartKpReceiver`, `SynchronizationReceiver` |
| provider | 10 | 7 exported Kiddoware providers |

### 3.2 Exported attack surface

- **7 exported ContentProviders** — `AuthenticationProvider`, `AppDataProvider`, `UserDataProvider`, `CategoryDataProvider`, `PreferenceDataProvider`, `AppLaunchesProvider`, `scheduler.db.TimesContentProvider`. All are guarded by the signature-level permission `com.kiddoware.kidsplace.KPService.permission.Access`.
- `KidsPlaceService` (exported, same permission guard), `DnsVpnService` (`BIND_VPN_SERVICE`), `KidsPlaceAccessibilityService` (`BIND_ACCESSIBILITY_SERVICE`), `KPDeviceAdminReceiver` (`BIND_DEVICE_ADMIN`).
- `AutoStartKpReceiver` (action `com.kiddoware.kidsplace.autostart`) and `RemoteLockIntentService` are exported **without** a permission — externally triggerable.

### 3.3 Permissions (34)

Self-protection / control:
`QUERY_ALL_PACKAGES`, `PACKAGE_USAGE_STATS`, `GET_TASKS`, `KILL_BACKGROUND_PROCESSES`, `RESTART_PACKAGES`, `RECEIVE_BOOT_COMPLETED`, `SYSTEM_ALERT_WINDOW`, `SYSTEM_OVERLAY_WINDOW`, `EXPAND_STATUS_BAR`, `WRITE_SETTINGS`, `SET_WALLPAPER`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `WAKE_LOCK`, `FOREGROUND_SERVICE` (+ `SPECIAL_USE`, `MEDIA_PLAYBACK`)

Network: `INTERNET`, `ACCESS_NETWORK_STATE`, `CHANGE_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE`

Identity/billing: `USE_BIOMETRIC`, `USE_FINGERPRINT`, `com.android.vending.BILLING`, `com.google.android.c2dm.permission.RECEIVE`

Custom: `com.kiddoware.kidsplace.KPService.permission.Access`, `...permission.CURRENT_RUNNING_APP`, `com.kiddoware.kidsvideoplayer.*` (plugin IPC)

**Evidence:** `ev_e1480e29082d8d4f9a28e3d634f80c33bc4c70169c7d746bca82bccc136790f3`

---

## 4. Runtime architecture

### 4.1 Third-party SDK inventory (from package histogram)

| Package | Classes | Purpose |
|---|---|---|
| `com.google.*` | 5,614 | Play Services, Firebase (Auth, Firestore, FCM/IID, Remote Config, Crashlytics, Analytics, Storage), reCAPTCHA |
| `com.kiddoware.*` | 490 | **Application code** |
| `kotlinx.coroutines` | 277 | Async |
| `io.grpc` | 204 | gRPC (Firestore transport + app gRPC client modules) |
| `org.xbill` (dnsjava) | 199 | DNS protocol handling for the VPN filter |
| `androidx.work` | 165 | WorkManager background jobs |
| `io.reactivex` | 131 | RxJava |
| `androidx.datastore` / `androidx.room` | 127 / 115 | Preferences + database |
| `com.bumptech` (Glide) | 87 | Image loading |
| `com.kidsplace.stories.*` | 51 | Interactive storybook feature |
| `com.firebase.ui` | 50 | FirebaseUI Auth |
| `com.airbnb` | 42 | (Lottie/Epoxy) |
| `com.evernote.android.job` | 30 | Job scheduling |

### 4.2 Application startup — `com.kiddoware.kidsplace.KidsLauncher`

`KidsLauncher` is the `android.app.Application` subclass. Its `onCreate()` (decompiled) performs, in order:

1. Stores the singleton context (`f19022u` / `f19023v`).
2. Enables AppCompat vector support (`androidx.appcompat.app.g.M(1)`).
3. Creates a **silent** notification channel `"DEFAULT"` (`setSound(null, null)`, importance 3) used by all foreground services.
4. First-run migration flags: `Utility.K3/L3/n7/o7`.
5. Initialises helper SDKs: `g1.a(this)`, `ef.d.g(...)`, `new he.c().a(...)`, `Utility.v6(...)`, `le.i.B(...)`, `ud.j.a().b(...)`.
6. Generates a stable install ID when absent (`Utility.P()` → `Utility.G4(...)`).
7. Constructs **`KidsPlaceRepository`** (Room data layer) and kicks off a background warm-up.
8. Installs a **global uncaught-exception handler** (`v0`).
9. Resolves the central singleton `com.kiddoware.kidsplace.i.b(context)` and registers it as a lifecycle listener.
10. Opens the SQLite DB and **seeds default data** when empty:
    - Categories: *No Category, Maths, Music, Videos, Storybooks, Games, Language, Art* (with colour + icon indices).
    - A default `User` named `@string/default_user_name` if `User.GetUsersCount() < 1`.
11. Runs "internet block reset" maintenance (`Utility.s/t/j3/E5`).
12. Computes grid metrics (`R.dimen.app_grid_item`, `R.dimen.app_parent_size` ÷ density).
13. **Privacy-gated telemetry:** analytics only if `Utility.A1(this, Utility.PrivacySettings.analytics)`; Crashlytics only if `...PrivacySettings.crashReporting`.
14. Fires fire-and-forget `AsyncTask`s (`new k(ctx, packageName)`, `new te.b(ctx)`) for sync/update checks.

**Evidence:** `ev_bc136b17cc79c7a602609e3833903dc09143e41632607345d8a521ecd5195bc6`

### 4.3 Enforcement engine — `KidsPlaceService`

Declared `foregroundServiceType="specialUse"`, subtype `parental_control_app_monitoring`, exported, guarded by `...KPService.permission.Access`.

`onCreate()`:
- Resolves `i.b(context)` singleton, obtains its `Executor` (`i.o().u()`).
- Creates two long-lived workers `ud.v` and `ud.c` (watchdog / pollers).
- Enqueues `new c1(context).execute(-1)` (startup task).
- Builds the **block overlay** by inflating `R.layout.block_popup` into a `ViewGroup` (`this.C`) — this is the "App Blocked" interstitial.
- Registers `KPEventsManager`, `f1.g(context)`, caches `ActivityManager`.

`onStartCommand()` — the state machine:
- Exits immediately if `i.w()` (user requested shutdown).
- Reads sticky-restart (`intent == null`) and `ScreenReceiver` / `DeviceUnlockedReceiver` extras.
- **Active branch** (`screenOn && deviceUnlocked && !fromScreenReceiver`): ensure watchdog running (`m0()`), set running flag (`d0(true)`), then `y(intent)` → start foreground + show lock.
- **Screen-off branch:** `N(intent)` — re-arm listeners.
- **Device-unlock branch:** force `screenOn/deviceUnlocked = true`, then `y(intent)`.
- Kicks `Utility.n3(...)` maintenance and `oe.f.i(this).w()`.
- Returns `START_REDELIVER_INTENT` (`1`) — the service is **restarted with the last intent** if killed.

`onDestroy()` tears down the workers/overlay.

**Evidence:** `ev_3fbd1d946fd75f0e6ba5d91e5b7d7734f06ee6b740f9375c1df4c7ba0ae7de88`, `ev_bc1aa9d9541e5c46f18190f315a9f8c2ca5987a31ebeafa26c22280b3bc9b79d`, `ev_754b21663dbbde422028ba914e5c9df0ff4c663f95be172a1327fceab7e09216`

### 4.4 Accessibility service — `KidsPlaceAccessibilityService`

- Declared with `BIND_ACCESSIBILITY_SERVICE`.
- **`onAccessibilityEvent()` is an empty method** — this build does not react to window-content changes.
- `onCreate()` builds a `Runnable` (`f19043b`) that calls the static `KidsPlaceAccessibilityService.e(...)` — i.e. enforcement is **polling/periodic**, driven by the service's own workers, not by accessibility events.

> This is a meaningful architectural detail: app-blocking in this version relies on the foreground service + polling + overlay, with the accessibility service present but inert for event handling.

**Evidence:** `ev_cabb76fdb2a2e7d2759f98cd65c826c1145e587c398f7f545813dc34aa7bacc8`, `ev_dad6e0447c70b9b228fc6569acad98791220277c47ae2ee33a77646768c26a0f`, `ev_f0437241c68441e4e862fb4384207f0be67f8b65d6b928e238a7c9c22452d80c`

### 4.5 Device admin — `KPDeviceAdminReceiver`

Handles `DEVICE_ADMIN_ENABLED`, `ACTION_PASSWORD_CHANGED`, `ACTION_PASSWORD_SUCCEEDED`. Used to force a lock / validate the parent PIN at the OS level.

### 4.6 DNS content filtering — `com.kiddoware.dnsblockvpn.vpn.DnsVpnService`

- `android.permission.BIND_VPN_SERVICE`, `foregroundServiceType="specialUse"`, subtype `dns-content-filtering-vpn`.
- `onStartCommand()` decodes an int extra `COMMAND` into a `Command` enum and dispatches:

```
switch (command) {
  case START (1):  w(); x();   // build + start tunnel
  case 2:          y();        // stop
  case 3:          r();        // teardown
  default (4):     coroutine   // async reload/config refresh
}
```

- Ships **dnsjava** (`org.xbill`, 199 classes) to act as a DNS server/resolver, plus `okhttp3` public-suffix data (`assets okhttp3/internal/publicsuffix/publicsuffixes.gz`) for domain classification.
- Backends: `https://kpsbproxy.kiddoware.com/` (Kids Place Safe Browsing proxy) and plugin package `com.kiddoware.safebrowsingvpn`.
- `com.kiddoware.kidsplace.vpn.ValidationResult` and `com.kiddoware.dnsblockvpn.api` / `.data` (16 classes) hold the filtering rules and category data.

**Evidence:** `ev_d2e3513a3832932c24325ba605af16aea1db0d5464b346667dca98ad0bf9c0c6`, `ev_a8f65230c9ef763cd2d9e19ab1167120e2dd857cc2928e93ec10a7267a98a5a3`

### 4.7 Remote control (parent → device)

- **Transport trigger:** `SynchronizationReceiver` listens for `kiddoware.kpsbcontrolpanel.PUSHDEVICECONFIG`; `onReceive()` posts a `Runnable` (`f20760d`) onto a handler and then conditionally runs `l0.E(context)` / `l0.Z(context)` (config apply / sync). This is the "push device config from the parent web panel" channel, delivered via FCM.
- **Remote lock:** `RemoteLockIntentService` (a `JobIntentService`) is started with job id **9987** via `RemoteLockIntentService.j(context, intent)`. Its `g(Intent)` handler rebuilds the intent, sets `ComponentName(pkg, LaunchActivity.class)`, adds `FLAG_ACTIVITY_NEW_TASK` (`268435456`) and launches `LaunchActivity` — i.e. a remote command **brings the app to the foreground**, which then forces the lock screen.
- **Remote sound:** `SoundService` (`mediaPlayback` FGS) + `StopSoundActivity` + `DialogMessageActivity` + `KPRemoteControlActivity` / `KPRCSettingsActivity` — parent can make the device ring / show a message.
- **Reporting:** `ReportingService` (`specialUse`, `parental_control_app_monitoring`) — `onStartCommand()` simply calls `a()` and returns `START_STICKY`.
- **gRPC:** `io.grpc` is present with Dagger modules `GrpcChannelModule_ProvidesServiceHostFactory`, `GrpcClientModule_ProvidesApiKeyHeadersFactory`, plus Firebase's `FirestoreGrpc` / `FirebaseClientGrpcMetadataProvider`. So gRPC serves **Firestore** primarily, with a secondary app-level API-key-header gRPC client.

**Evidence:** `ev_962127a99fba492482a2e1f68453a9b52b2e58b311065d868d9b7c0fd5bda62f`, `ev_7faf552d0f77bc277f3475b902505d5faa0ae9f34b80160b8e78bc3b9146fad7`, `ev_a292f4b47e99bc2063459a873e56f51141e43fe5289c3f538d9ffdece426cbe1`, `ev_051a28668f00e6ad48846de830419f0bb5ea36eca280b4234a666d1829091ffb`

### 4.8 FCM / Firebase

- `KPFirebaseMessagingService` (exported=false) handles `com.google.firebase.MESSAGING_EVENT`.
- `FirebaseInstanceIdReceiver` (exported, `com.google.android.c2dm.permission.SEND`) for token rotation.
- Firebase models: `firebase.model.Device`, `KPFirebaseUser`, `MediaInfo`, `VideoPage`; `NotificationParams`.
- Firebase project: **`kids-place`** (`kids-place.appspot.com`).
- Firestore used for **cloud backup/restore** (`FirestoreBackupIntentService`, `FirestoreRestoreIntentService`, `FirestoreBackups`, `CloudBackupActivity`, `BackupSession`) in addition to local backup (`LocalBackupService`, `LocalRestoreService`).
- `com.google.android.backup.api_key` = `AEdPqrEAAAAIFuXPwN9Z7s6d2KLvpbMezw3G8ajlsXIPXfQaPw`.

### 4.9 Device ID derivation — `Device.computeDeviceId()`

```java
str  = "35" + (BOARD.length()%10) + (BRAND.length()%10) + (CPU_ABI.length()%10)
            + (DEVICE.length()%10) + (DISPLAY.length()%10) + (HOST.length()%10)
            + (ID.length()%10) + (MANUFACTURER.length()%10) + (MODEL.length()%10)
            + (PRODUCT.length()%10) + (TAGS.length()%10) + (TYPE.length()%10)
            + (USER.length()%10);
str2 = str + Settings.Secure.ANDROID_ID + wifiMacAddress + BuildConfig.FLAVOR(or BT address);
return Settings.Secure.getString(ANDROID_ID);   // ← note: the MD5 of str2 is computed but NOT returned
```

Observation: the MD5 digest is built into `str3`, upper-cased, then **discarded** — the method actually returns the raw `ANDROID_ID`. On failure it returns `ANDROID_ID + random(0..8999) + 0`. This looks like dead/legacy hashing code left in place.

**Evidence:** `ev_b95573c25e797c99ef000d72e68a65d56cba8a4018f840d21c87995c6a1084c2`

### 4.10 Background work

- WorkManager + Evernote AndroidJob + `Alarm`/`AutoStartKpReceiver`/`StartupReceiver`.
- Workers: `AppLaunchesReportingWorker`, `DnsStatsReportingWorker`, `DeleteOlderRecordsWorker`, `TasksEngagementWorker`, `StoriesCatalogDownloadWorker`, `AgeSignalsWorker`, `AutoApproveWorker`, `AutoStartAppWorker`, `AutoStartKPWorker`, `AutoStartSkipForTodayWorker`, `AllowedUsageScheduleWorker`.

---

## 5. APIs

### 5.1 Kiddoware backend

| Purpose | Endpoint |
|---|---|
| API base | `https://app.kiddoware.com/kw_backend_new/public/api/kp/api/` |
| Client API base | `https://app.kiddoware.com/kw_backend_new/public/api/kpclient/` |
| **Key exchange** | `…/kp/api/getKey` |
| **Install registration** | `…/kpclient/updateInstall` |
| Web login | `https://app.kiddoware.com/login` · `/forgotpass` |
| QA environment | `https://qa.app.kiddoware.com/login` |
| Web portal | `https://kidsplace.kiddoware.com` · `/forgot_password` |
| Licensing | `https://licensing.kiddoware.com/` |
| Safe-browsing DNS proxy | `https://kpsbproxy.kiddoware.com/` |

Additional path fragments observed: `/api/v0`, `/api/v1`.

**Interpretation:** the `getKey` → `updateInstall` pair indicates the client performs a **key-exchange handshake first**, then registers the install — i.e. API payloads are encrypted with a session key rather than plain JSON over TLS alone. (The concrete cipher is in an obfuscated class that was not recovered; see §9.)

### 5.2 Response envelope — `com.kiddoware.kidsplace.reporting.api.network.BaseResponse`

Fields: `responseCode`, `result`, `error`, `serverResponse`, `prcVer` (protocol version), `serialVersionUID`; methods `getError()`, `getServerResponse()`, `isSuccessfull()`, `setServerResponse()`, `trace()`.

**Evidence:** `ev_39c06e54e24226ee8212648145fe1c05e80e12aa47f31577605a627fb95b9942`

### 5.3 Firebase / Google

- Firestore (backup, device registry) via gRPC to `firestore.googleapis.com`
- Firebase Remote Config: `firebaseremoteconfig.googleapis.com/v1/projects/%s/namespaces/%s` (and the `…realtime…` variant)
- Crashlytics settings: `firebase-settings.crashlytics.com/spi/v2/platforms/android/gmp/%s/settings`
- FCM / IID, Firebase Auth (FirebaseUI + Google + Facebook), reCAPTCHA (`www.recaptcha.net`)
- **Firebase Storage bucket `kids-place.appspot.com`** — serves wallpaper images (`kidsplace-wallpapers/{cars,pets,playground,stories}/*.webp`)

### 5.4 Asset / content CDNs

- `https://kidsplace.s3.us-west-2.amazonaws.com/lottieanimation/Permission` — Lottie animations (AWS S3, us-west-2)
- `assets/ads.json` — bundled cross-promotion config (Kids Safe Browser, Kids Video Player, Kids Picture Viewer); `show_ad_banner: true`, `slide_duration_seconds: 10`. Contains **cleartext `http://kiddoware.com/android/app_icons/*.png`** icon URLs.

### 5.5 Legal / policy URLs bundled

`kiddoware.com/kids-place-privacy-policy/`, `/kids-place-remote-control-privacy-policy/`, `/kiddoware-eula-and-terms-and-services-agreement/`, `/kiddoware-subscription-cancellation-policy/`, user manuals, `/what-permissions-kidsplace-app-ask/`.

---

## 6. Data model (Room / SQLite)

**`KidsApplication`** (app catalogue entry) — full field set recovered:
`id`, `rowId`, `packageName`, `className`, `title`, `displayName`, `icon`, `categoryId`, `displayOrder`, `intent`, `filtered`, `selected`, `installed`, `customFilteringEnabled`, `wifiEnabled`, `installedFromKpStore`, `availableForUser`, `alwaysStartAsNewTask`.
Query helpers: `getAllByPackageName`, `getByPackageName`, `getByPackageAndClassName`, `getAllCustomFilteringEnabled`, `getUserAppsQueryString`, `setupContentValues`, `migrateToCurrentLauncherActivity`.

**`User`** — `id` (`DEF_USER_ID`), `name`, `pin`, `image`, `availableApps`.
**`Category`** — `id`, `name`, `colorFilter`, `iconResource`, `editable`, `firstPosition`, `sectionedPosition`, `kidsApplications`.
**Other entities:** `Ad`, `AppLaunches`, `AuthenticationMode`, `NavigatorModel`, `RecordAppSessions`, `firebase.model.Device`, `firebase.model.KPFirebaseUser`, `scheduler.calendar.model.Day`/`DayDB`/`DayDbList`, `tasks.data.*` (Room `TaskDatabase`).

`User.GetDefaultUser()` returns `new User(0L, BuildConfig.FLAVOR, BuildConfig.FLAVOR)` — the default profile is seeded from the build **flavor** string.

**Evidence:** `ev_c49deb5e45cb8c3c18752fdc586f17b7e212b4bbf4411174f4132bcee4f542ad`, `ev_ae0144b14efc34b6a1dca7d2dafa2de4e001cf0b51676ccbd56af4ebfffcc94a`, `ev_dfa25c7286235182571a5f8923ed977f2d177dad1d329a32842bb4bdb589c3e5`, `ev_11aaf8b3c688a6c89a7a4e8c20f868a4cf10e322da395c9b558842a4aa1d8853`

---

## 7. Feature map

| Feature | Implementation |
|---|---|
| Kids launcher / home screen | `LaunchActivity` + `AppPicker`/`AppPickerTabs`, `KidsApplication` catalogue, categories |
| App blocking | `KidsPlaceService` watchdog + `R.layout.block_popup` overlay + polling |
| PIN / parent gate | `LockActivity`, `LockActivityWithGrownUpMode`, `ChangePinActivity`, `PinRecoveryActivity`, `AuthenticationMode` |
| OS-level lock | `KPDeviceAdminReceiver` (DeviceAdmin) + `LockEnableActivity` |
| Boot persistence | `StartupReceiver`, `AutoStartKpReceiver`, `android:persistent="true"`, `START_REDELIVER_INTENT` |
| Time scheduling | `scheduler.calendar.*`, `TimeProfilesActivity`, `AllowedUsageScheduleWorker`, `TimesContentProvider` |
| Web filtering | `DnsVpnService` + dnsjava + `kpsbproxy.kiddoware.com` + `com.kiddoware.kidsafebrowser` / `safebrowsingvpn` plugins |
| Remote control | FCM → `SynchronizationReceiver` → config apply; `RemoteLockIntentService` (job 9987) → `LaunchActivity`; `SoundService` |
| Usage reporting | `AppLaunchesReportingWorker`, `DnsStatsReportingWorker`, `ReportingService`, `RecordAppSessions`, `AppLaunches` |
| Tasks & rewards | `TaskDatabase` (Room), `TasksParentActivity`, `tasks.kids.*`, `RemoteApprovalResult` |
| Stories | `com.kidsplace.stories.*` (catalog, characters, scenes, playback, feedback) |
| Age assurance | `AgeSignalsWorker` + `age-signals.properties` + `developers/mobile/targeting/proto/client_signals.proto` (Google Play age-signals API) |
| Backup/restore | Local (`LocalBackupService`/`LocalRestoreService`) + Firestore cloud |
| Licensing / IAP | Play Billing, `licensing.kiddoware.com`, `com.kiddoware.library.billing`, `com.kiddoware.library.singlesignon` (SSO) |
| Analytics / crash | Firebase Analytics + Crashlytics, both **privacy-gated** |
| Cross-promo | `assets/ads.json` → Kids Safe Browser / Video Player / Picture Viewer |

---

## 8. Notable observations

1. **Custom-signed build.** Self-signed `CN=Paresh Joshi` v2-only signature — not a Play-signed artifact. Anyone can re-sign; integrity is not anchored to a public key lineage.
2. **Exported receivers without permission.** `AutoStartKpReceiver` (`com.kiddoware.kidsplace.autostart`) and `RemoteLockIntentService` are exported with no permission, so any app on the device can broadcast to them / start the job. `RemoteLockIntentService` then launches `LaunchActivity` in the foreground — a local denial-of-service / nuisance vector (repeatedly yank the user into the app).
3. **Accessibility service is inert for events** in this build (`onAccessibilityEvent` empty). Enforcement depends on the sticky foreground service; if the OEM kills it, blocking degrades until restart.
4. **Privacy gating exists** but is opt-out style: analytics and Crashlytics are off only if the user flips `Utility.PrivacySettings.analytics` / `.crashReporting`. `firebase_analytics_collection_enabled` and `google_analytics_adid_collection_enabled` are both `false` in the manifest (good), while `xanadu_debug=true` is left enabled.
5. **Device ID is `ANDROID_ID`.** The elaborate Build-fingerprint/WiFi-MAC/Bluetooth hashing in `computeDeviceId` computes a digest and throws it away, returning raw `ANDROID_ID`. Stable, per-app-signing-key scoped, non-resettable without factory reset — used as the cloud identity key.
6. **Cleartext HTTP** appears in bundled content (`http://kiddoware.com/android/app_icons/*.png`), so the network security config likely whitelists `kiddoware.com`.
7. **7 exported ContentProviders** expose the entire app/user/category/preference/usage database. All rely on the single `...KPService.permission.Access` permission; the permission's `protectionLevel` was not recovered from the decoded manifest text and should be verified (if `normal`, any app can request it and read the full DB).
8. **gRPC + `GrpcClientModule_ProvidesApiKeyHeadersFactory`** suggests a second, API-key-authenticated gRPC channel beyond Firestore — consistent with the `getKey` handshake feeding that API key.

---

## 9. Limitations / not determined

- **Static only.** The APK was never executed; no runtime behaviour, no network traffic, and no dynamic registration of components was observed. All statements are from bytes on disk.
- **Obfuscation.** Class/method/field names are R8-mapped and `mapping.txt` was not supplied, so obfuscated classes (`Utility`, `com.kiddoware.kidsplace.i`, `l0`, `ud.*`, `he.*`, `g1`, `ef.*`, `oe.*`) could only be analysed through their call sites and string constants, not by name.
- **`network_security_config.xml` contents** were not recovered — resource names are obfuscated and `decode-android-resources` requires a caller-supplied apktool, which is not installed.
- **`TaskDatabase` entity list** was not recovered: `inspect-android-method` fell back to raw smali for the Room builder, so the exact table set beyond `KidsApplication` / `User` / `Category` is inferred from the model package, not read from the schema.
- **Exact API request/response bodies** (field names, headers, cipher suite for `getKey`) were not recovered — the networking class is obfuscated and no `mapping.txt` is available.
- **The `...KPService.permission.Access` protectionLevel** is not present in the decoded manifest text; verify before relying on the provider permission as a security boundary.
- `AppLaunchesReportingWorker` exposes only `<init>` and a single obfuscated method `o`; its payload could not be read.

---

## 10. Reproduction

Environment used (Windows x64, REA 6.9.0):

```
Node.js 26.13.1
JDK 21.0.13 (Temurin)  -> JAVA_HOME
jadx-headless-mcp-0.7.1-all.jar (SHA-256 6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75)
```

Key commands:

```powershell
rea inspect-apk-signature   <apk> --json
rea identify-flutter-build  <apk> --json
rea inspect-android-package <apk> --json
rea search-android-classes  <apk> "." --json
rea inspect-android-class   <apk> <fqcn> --json
rea inspect-android-method  <apk> <fqcn> <method> --json
```

Raw outputs and decompiled sources are in `analysis\` (`01-…` … `23-…`, `src_*.java`, `06-AndroidManifest.xml`, `08-class-list.txt`).