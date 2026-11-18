# Kids Place — Tablet UI Analysis
## Landscape form factor (2560 × 1600)

**Analysis date:** 2026-11-18
**Evidence base:** 64 screenshots in `/screenshot/Tablet/`
**Package:** `com.kiddoware.kidsplace` (plus three system surfaces: `com.android.settings`, `com.miui.securitycenter`, `com.google.android.photopicker`)
**Device profile:** 2560 × 1600 px Android tablet (landscape, 16:10), MIUI/HyperOS‑style system UI
**Method:** OCR transcription (RapidOCR / PP‑OCR) plus pixel‑level colour, geometry and column‑cluster analysis of every frame.

> **Confidence note.** Text was recovered by OCR; a few glyphs are uncertain (`Brc`, `lcon`, `Regquires`, `Strech`). Geometry, colour and column positions are measured from pixels and are reliable. No PIN or password value is reproduced.

---

## 1. Executive summary

This capture set is the **tablet (landscape) rendition of the same Kids Place app** analysed on the phone. It is not a scaled phone UI: the app reflows into **multi‑column grids**, centres onboarding content in **cards over hero imagery**, and exposes **tablet‑only affordances** (an `EXIT` button in child mode, a 4‑column app manager, a 3‑column settings hub, timer presets and an analogue clock picker).

The flow captured is primarily **configuration and hardening**, not child‑mode setup:

1. Per‑app time limit and block schedule
2. Manage Apps (4‑column grid)
3. User selection / creation
4. Settings hub (3 columns) + plugin prompts
5. Parental Controls (including tablet‑only *Lock home button*, *Auto restart apps*, *Auto Start Kids Place*)
6. Whitelist package management
7. Permission onboarding (3/4)
8. Kids Place Remote Control onboarding → data‑collection consent → device enrolment → Device‑Admin activation
9. Wallpaper, Timer, UI Settings
10. Backup & Restore, Account, Privacy, Security, Plugins
11. Usage details and the in‑app Help overlay in child mode

**Headline finding:** the tablet build is the most complete expression of the product — it adds the remote‑control enrolment funnel, privacy controls and the plugin system that the phone capture only hinted at, but it also shows the same structural problems: duplicated settings paths, documentation‑grade copy, unmasked PINs, and an inconsistent theme (light here, dark on the phone's Manage Apps).

---

## 2. Device and capture context

- **Resolution:** 2560 × 1600, landscape. No navigation bar captured (gesture or immersive).
- **System UI:** MIUI‑style. Status bar shows `12:31 Wed, Nov 18` plus a numeric badge; system dialogs use a **dark** theme (`#000000`, `#242424`) that contrasts sharply with the app's light surfaces.
- **Two app‑level type sizes:** the same screens appear at different scales in the set, indicating the system display‑size/zoom setting changed mid‑capture (see Appendix A).
- **Account:** `rongiancarlogiongco@gmail.com` (Ron Gian Carlo Giongco).
- **Device:** `Xiaomi 25099RP13G` / `e39becce86d893c7` (also `Xiaomi M2102J20SG` in the restore list).

---

## 3. Design system

### 3.1 Colour

The tablet uses the same tokens as the phone build:

| Token | Value | Usage |
|---|---|---|
| Brand purple | `#5C49DF` (92,73,223) | App bar, primary buttons, selected chips |
| App‑bar gradient | `#5D50DC` → `#5442B0` | Vertical gradient on the 316 px header |
| Deep purple | `#241C57` (36,28,87) | Scrims, dark surfaces |
| Surface | `#FFFFFF` / `#FAFAFA` | All content pages |
| Divider | `#CECECF` (206,206,207) | Chip outlines, inactive controls |
| Success / allowed | `#00AD5B` | "Unrestricted" |
| Blocked | `#FF3D01` (255,61,0) | Blocked schedule cells |
| Warning | `#FCC466` | Reward chips |
| Scrim | `rgba(0,0,0,0.6)` → `#666666` | Behind every dialog |

Notable: **Manage Apps is light on tablet** (`#FFFFFF` body) whereas the phone build renders the same page on `#323232`. No dark variant appears anywhere in this capture set.

### 3.2 Typography

Measured at 2560 px width (≈ 1.6× the phone's 1080 px in absolute pixels, but with a much lower dp scaling because the tablet is physically larger):

| Role | Size (px) | Weight | Colour |
|---|---|---|---|
| App‑bar title ("AMdroid", "Manage Apps") | ~56 | 600 | white |
| Screen title ("Settings") | ~56 | 600 | `#212121` |
| Section header ("Customize Content and Controls") | ~50 | 600 | `#212121` |
| Setting title | ~46 | 500 | `#212121` |
| Setting description | ~40 | 400 | `#757575` |
| Button label | ~46 | 600 | white |
| Status bar clock | ~30 | 400 | white |

- Descriptions are set at ~87 % of the title size and sit **directly under** the title (stacked), not beside it.
- Buttons remain UPPERCASE (`UPDATE DAILY TIME LIMITS`, `GRANT PERMISSION`, `CREATE NEW BACKUP`).

### 3.3 Layout grid

```
0                                                              2560
┌─────────────────────────────────────────────────────────────────────┐ 316 px
│  status bar ··········  APP BAR (purple, gradient)  ·············   │ gradient
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   CONTENT CANVAS  (white, 40–60 px side padding)                    │
│                                                                     │
│   Settings hub  → 3 columns at x ≈ 120 / 540 / 960                 │
│   Manage Apps   → 4 columns at x ≈ 54 / 694 / 1334 / 1974          │
│   Detail pages  → single column, x ≈ 155, width ≈ 2 340            │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

- **App bar:** 316 px tall (≈ 20 % of the viewport) with a subtle top‑to‑bottom purple gradient. Title left at x≈150; trailing user/action label right‑aligned.
- **Content:** full width, 40–60 px side padding, no max‑width container — text lines can run to ~2 400 px, which hurts readability on long descriptions.
- **Vertical rhythm:** 40 px between title and description, 110–150 px per settings row, 40 px before a group header.
- **Two densities observed:** one set of frames has ~46 px titles and ~135 px rows; another has ~40 px titles and ~110 px rows (display‑size change, Appendix A).

### 3.4 Components

| Component | Tablet behaviour |
|---|---|
| **App bar** | 316 px, gradient purple, title left, contextual action right ("Default User", "EXIT", "Manage users") |
| **Settings row** | Icon + title + 2–5 line description, chevron or toggle on the right; 110–150 px tall |
| **Group header** | 17 sp semibold, full width, 40 px above first row |
| **Primary button** | Purple fill, uppercase, 8 px radius; either full‑width or ~420 px wide right‑aligned |
| **Chip** | Rounded, `#F0F0F0` fill, purple when active ("Sort", "Filter", "Auto Approve : Off") |
| **App grid cell** | 68 px icon + name + optional category, 4 per row, 640 px pitch |
| **Dialog** | Centred white card (e.g. 780–1800 px wide), 60 % scrim, CANCEL/OK at bottom‑right |
| **Time picker** | Two‑column hour/minute dial + AM/PM, `COPY TO ALL DAYS` |
| **Clock picker** | Analogue 12‑hour dial for "Specify time of the day" |
| **Week grid** | 7 day columns × 24 hour rows, orange‑red blocked cells |
| **Hero card** | White rounded card centred over a full‑bleed background for Remote Control onboarding |
| **Empty/permission state** | Step counter ("3/4"), checklist of Android settings, `GRANT PERMISSION` |

### 3.5 Iconography

- Purple outline icons at 48–56 px in settings rows.
- App icons are rendered at ~68 px in the Manage Apps grid.
- System dialogs (Android/MIUI) use their own iconography and dark surfaces — a visual break from the app.

---

## 4. Screen inventory

All 64 captures are 2560 × 1600.

| # | Time | Screen | Key content |
|---|---|---|---|
| 1–6 | 12:31 | App time limit | AMdroid · Daily Time Limit · Unrestricted · UPDATE DAILY TIME LIMITS · Block Schedule |
| 7–8 | 12:31 | Time scroll | 22:58 / 23:59 / 00:00 picker columns |
| 9 | 12:31 | Block schedule | 12 PM → 11 PM rows, `Block all timeslots`, `Clear all timeslots` |
| 10–13 | 12:31 | Manage Apps | 4‑column app grid, search, Sort/Filter, Internet Access dialog |
| 14 | 12:31 | Sort dialog | Alphabetical / Recently Installed / Recently Used |
| 15 | 12:31 | Filter dialog | All Apps / Only Selected / Unselected |
| 16 | 12:31 | Add Category | Name, Color, CANCEL/OK |
| 17–18 | 12:31 | Manage users | User list + `Manage users` action |
| 19–20 | 12:31–32 | Select User | Default User |
| 21 | 12:32 | Add user | Name, User PIN (optional) |
| 22–23 | 12:32 | Settings hub | Licence, 3‑column groups, plugin dialogs |
| 24–29 | 12:32 | Parental Controls | Lock home button, Auto restart apps, Auto Start, Block Play Store, Airplane mode, Reporting, Wi‑Fi |
| 30 | 12:32 | Whitelist | Add New Whitelist Package |
| 31 | 12:32 | Permission onboarding | "3/4", App info, usage, battery, notifications, Display over other apps |
| 32 | 12:32 | Remote Control intro | "Manage Kids Place Remotely Independently" |
| 33 | 12:32 | Data Collected consent | Per‑data‑type explanation, CONTINUE |
| 34 | 12:33 | Manage Remotely | Cloud console link `https://app.kiddoware.com` |
| 35–36 | 12:33 | This device | 25099RP13G · e39becce86d893c7 · reliability · Device Admin |
| 37 | 12:33 | Device‑admin confirm | "Attention" dialog |
| 38 | 12:33 | **Android Settings** | "Activate device admin app?" — KidsPlaceSecurity |
| 39 | 12:33 | **MIUI Security Center** | "Danger" — Control device permission, 6 s countdown |
| 40 | 12:33 | Wallpaper settings | Kids Select Wallpaper, Rotating Wallpapers |
| 41–43 | 12:33 | Wallpaper gallery | Stories, Playground, Animals, Cars, Kids Place Classics, Customize |
| 44 | 12:33 | **Photo picker** | "Choose a cloud media app" |
| 45 | 12:34 | Rotating frequency | Minute / Hourly / Daily / Weekly / Never |
| 46–49 | 12:34 | Kids Place Timer | No time lock, 5/15/30 min, 1 hour, minutes, time of day |
| 50 | 12:34 | Clock picker | Analogue dial + AM/PM |
| 51 | 12:34 | Select User | Default User |
| 52–53 | 12:34 | Parental Controls | Advanced Timer, Background mode, Tamper proofing, Brute‑force PIN |
| 54–56 | 12:34 | User Interface Settings | Category view, icon layout, icon size 96, font size 14 |
| 57 | 12:34 | Backup and Restore | Account, device list, CREATE NEW BACKUP |
| 58 | 12:35 | Manage Account | Linked e‑mail, DELETE ACCOUNT, UNLINK |
| 59 | 12:35 | Privacy Settings | Crash Reporting, Analytics, Error Logging |
| 60 | 12:35 | Kids Place Security | Change PIN, security type, forget password |
| 61 | 12:35 | Kids Place Plugins | Stories, Tasks, display shortcuts |
| 62 | 12:35 | Usage details | Per‑app usage + day limit + user |
| 63 | 12:35 | **Help** (child mode) | 9 numbered instructions, OK |
| 64 | 12:36 | Child home | KidsPlace · 23:58 · EXIT, HOME, AMdroid |

---

## 5. What actually changes on a tablet

This is the analytically important part: the tablet build is a **genuine responsive redesign**, not a stretched phone layout.

| Area | Phone (1080 × 2400) | Tablet (2560 × 1600) |
|---|---|---|
| App bar | ~200 px, flat purple | **316 px, purple gradient**, proportionally much taller |
| Manage Apps | 1 column, ~5 rows visible, **dark** `#323232` | **4 columns**, ~7 rows visible, **light** `#FFFFFF` |
| Settings hub | 1 column, ~6 rows visible | **3 columns** (x ≈ 120 / 540 / 960) |
| Detail pages | 1 column, x ≈ 40 | 1 column, x ≈ 155, **much longer lines** |
| Dialogs | Bottom‑aligned, 60 % scrim | **Centred cards**, 60 % scrim |
| Remote Control onboarding | Not present | **3‑step funnel + hero card + consent** |
| Timer lock options | No time lock / minutes | **+ 5/15/30 min, 1 hour, time of day (analogue)** |
| Child mode | No exit affordance | **EXIT button in the top bar** |
| Help | Tips overlay only | **Full 9‑step Help screen** |
| Permissions | Not captured | **"3/4" checklist → GRANT PERMISSION** |
| Privacy | Not captured | **Crash / Analytics / Error logging** |
| Plugins | Two install dialogs | **Dedicated Plugins page** (Stories, Tasks, shortcuts) |

### 5.1 Manage Apps — 4‑column grid

```
┌───────────────────────────────────────────────────────────────────────┐
│ ‹ Manage Apps                        Default User      (manage users)│ 316 px
│ HOME                                                                  │
│ [ Q  Search for an application ]                                      │
│ (Sort) (Filter)  Auto Approve : Off                                   │
├───────────────┬───────────────┬───────────────┬───────────────────────┤
│ ▢ AMdroid     │ ▢ Authenticator│ ▢ HOLY BIBLE │ ▢ Bible App for Kids │
│ ▢ Calculator  │ ▢ Calendar     │ ▢ Camera      │ ▢ Clock              │
│ ▢ Contacts    │ ▢ Copilot      │ ▢ Docs        │ ▢ Downloads          │
│ ▢ Drive       │ ▢ EX File      │ ▢ Edge        │ ▢ Excel              │
│ ▢ File Manager│ ▢ Files        │ ▢ Find Hub    │ ▢ GCash              │
│ ▢ Gallery     │ ▢ Gemini       │ ▢ Gmail       │ ▢ GoTyme PH          │
│ ▢ Google      │ ▢ Google Earth │ ▢ Google One  │ ▢ Google TV          │
└───────────────┴───────────────┴───────────────┴───────────────────────┘
```

- **Row‑major alphabetical order**, 4 per row, 640 px pitch, ~135 px row height.
- Each cell: 68 px icon at x+0, label at x+110, optional grey category beneath.
- The header keeps the `HOME` tab label and the user selector.
- Inline coaching bubbles appear over the grid ("To allow apps, check this box!", "You can set time limit for any app from here!", "Tap this button to manage users!").
- Dialogs (Sort, Filter, Add Category, Internet Access, Auto Approve) open **centred over the grid** with a 60 % scrim.

### 5.2 Settings hub — 3 columns

```
┌───────────────────────────────────────────────────────────────────────┐
│ ‹ Settings                                                            │
│ Kiddoware Suite License — You have a Kiddoware Suite Subscription...  │
│                                                                       │
│ CUSTOMIZE CONTENT AND CONTROLS                                        │
│   ▸ Manage Apps        ▸ Manage Browser        ▸ Manage Videos        │
│                                                                       │
│ PREMIUM FEATURES                                                      │
│   ▸ Wallpaper          ▸ Timer                 ▸ Screen Time Ctrl     │
│   ▸ Manage Users       ▸ Advanced Security     ▸ User Interface       │
│   ▸ Backup and Restore ▸ Manage Account        ▸ Remote Control       │
│                                                                       │
│ PERSONAL SETTINGS                                                     │
│   ▸ Privacy  ▸ Security  ▸ Contact Us  ▸ License  ▸ Plugins  ▸ Exit   │
└───────────────────────────────────────────────────────────────────────┘
```

- Group headers span the full width; items are distributed **3 per row** at x ≈ 120 / 540 / 960.
- Two rows open **modal install prompts** (Safe Browser plugin, Video Player plugin) rather than pages.
- Because the layout is column‑major, the reading order is not obvious — a scan of "Manage Apps → Manage Browser → Manage Videos" is natural, but "Wallpaper → Timer → Screen Time" wraps differently.

### 5.3 Detail pages — wide single column

Parental Controls, Privacy, Security, Plugins, UI Settings, Wallpaper and the app‑limit editor all use a **single column at x ≈ 155** with content running to x ≈ 2 400. Descriptions sit under titles and frequently run to 3–5 lines of 150+ characters, e.g.:

> "Note: This settings is different from standard 'Start On Device Reboot' option as that only works if device is rebooted while Kids Place is running. This setting will always start Kids Place when device is started even when Kids Place is not running."

At 2 400 px line length this is ~200 characters per line — well beyond comfortable reading measure.

### 5.4 Timer — presets and analogue picker

The tablet timer offers a much richer lock menu than the phone capture:

| Option | Behaviour |
|---|---|
| No time lock | Session has no limit |
| **Lock in 5 minutes** | Preset |
| **Lock in 15 minutes** | Preset |
| **Lock in 30 minutes** | Preset |
| **Lock in 1 hour** | Preset |
| Specify time in minutes | Numeric entry |
| **Specify time of the day** | **Analogue 12‑hour clock dial** with AM/PM and a live `12:34` readout |

`Clear temporary timer on exit` is a persistent toggle; `APPLY` is right‑aligned.

### 5.5 Remote Control onboarding — card over hero

Three steps, each a **white card centred over a photographic/illustrated background**:

1. **Intro** — "Kids Place Remote Control / Manage Kids Place Remotely Independently / Kids Place Remote Control app allows parents to remotely manage Kids Place - Parental Control app. Once the app is installed on your kids device, you can synchronize Kids Place configuration from our server to one or more devices or from your device to our server." → **GET STARTED** · `TERMS & CONDITIONS`
2. **Data Collected** — per‑item justification:
   - *List Applications installed on your phone. Only application name and package name data is stored…*
   - *To remotely allow/block apps… also used to display a list of apps to control screen time.*
   - *Usage times of these applications when KidsPlace is not installed — To display reporting data on admin console.*
   - *KidsPlace settings — To view/update Kids Place current settings on admin console.*
   - Consent line + **CONTINUE**
3. **Manage Remotely** — "Kids Place configuration is automatically updated on the server whenever any app data is changed. You can then visit Kids Place Cloud console…" + `https://app.kiddoware.com` + **CONTINUE**

Then **THIS DEVICE** (`25099RP13G` / `e39becce86d893c7`) with two hardening actions:

- **Higher communication reliability** — "Requests ignoring battery optimization to reliably communicate with your device when it is in sleep mode."
- **Enable Device Administrator Permission** — "Enabling Device Admin will disable uninstall option from app manager making it harder for kids trying to uninstall this app. Please go to Device settings > Security > Device administrator and uncheck Kids Place Security option to uninstall the app."

Tapping either opens an **Attention** confirmation, then the system dialogs:

- `com.android.settings` — **"Activate device admin app?"** → *KidsPlaceSecurity* · Activate this device admin app / Cancel / Uninstall app
- `com.miui.securitycenter` — **"Danger"** → *Control device* is a highly sensitive permission (Erase all data / Monitor and control screen lock / Restrict camera usage) with a **6‑second countdown** on `OK (6)`

This is the most security‑critical flow in the product and it is handled entirely through OS dialogs with no in‑app summary of what was granted.

---

## 6. Screen‑by‑screen detail

### 6.1 Per‑app time limit and block schedule

```
┌───────────────────────────────────────────────────────────────────────┐
│ ‹ AMdroid                                        Default profile      │ 316 px
│                                                                       │
│ Daily Time Limit                                                      │
│ Limit usage time of amdroid per day of the week.                      │
│                                                                       │
│                            [ Unrestricted ▾ ]                         │
│                                                                       │
│  [                UPDATE DAILY TIME LIMITS                         ]  │ full-width
│                                                                       │
│ Block Schedule                                                        │
│ Select time slots when amdroid is not allowed to be used.             │
│                                                                       │
│  12 PM  1 PM   2 PM   3 PM  … 11 PM        [Block all timeslots]      │
│  ▢      ▢      ▢      ▢                        [Clear all timeslots]   │
│  ▢      ▢      ▢      ▢                                               │
│  … 24 rows × 7 day columns …                                          │
│                                                                       │
│  [ COPY TO ALL DAYS ]                                       [ OK ]    │
└───────────────────────────────────────────────────────────────────────┘
```

- **Unrestricted** renders as a purple dropdown chip, not a toggle.
- **UPDATE DAILY TIME LIMITS** is a full‑width purple bar directly under the control.
- The schedule grid uses **row labels down the left edge** (`12 PM`, `1 PM`, …) and **7 day columns**; blocked cells are `#FF3D01`.
- `Block all timeslots` / `Clear all timeslots` are pushed to the far right (x ≈ 2 277), far from the grid they affect.
- `COPY TO ALL DAYS` sits bottom‑left, `OK` bottom‑right.

### 6.2 Manage Apps

Covered in §5.1. Additional behaviour:

- **Sort** → Alphabetical / Recently Installed / Recently Used
- **Filter by** → All Apps / Only Selected / Unselected
- **Auto Approve** → "Automatically approve newly installed apps. Recommended for devices that are not shared with kids." → Enable / Disable
- **Add Category** → Name + Color swatches
- **Internet Access** → Internet On / Internet Off / Custom Filtering (MANAGE)
- **Manage users** → user list with per‑user app selection

### 6.3 Settings hub and plugin prompts

Covered in §5.2. Plugin prompts (modal, centred):

| Prompt | Copy |
|---|---|
| Manage Browser | "Managing websites and parental web controls requires the Kiddoware Kids Place Safe Browser plugin. It is not installed on this device. Do you want to open the app store to install it now?" → **CANCEL / INSTALL** |
| Manage Videos | "Managing videos and safe YouTube viewing requires the Kiddoware Kids Place Video Player plugin…" → **CANCEL / INSTALL** |

### 6.4 Parental Controls

| Setting | Description (verbatim) | Tablet note |
|---|---|---|
| **Lock home button** | "Enhances home button security by changing Android home temporarily. *Recommended*" | Not in the phone capture |
| **Auto restart apps** | "Restart a launched app if kid accidentally exits." | **Not in the phone capture** |
| **Auto Start Kids Place** | "Set KidsPlace to start automatically every day, for better screen time behavior." | **Not in the phone capture** |
| Close Kids Place Block Overlay | Allow closing blocking overlay after a while | |
| Block Play Store | Block apps from accessing marketplace | |
| Block Google App | Disable Google Assistant, search and other quick actions | |
| Airplane mode | Disable all wireless connections | |
| Allow phone calls | Display incoming call alerts | |
| Keep home screen on | Disables screen timeout when checked. Uncheck to save power. | |
| Start on device reboot | Check to start Kids Place on device reboot… | |
| Allow apps switcher | Allow switching apps from task manager/recent apps | |
| Whitelist | Whitelist Apps/Process that are not visible in launcher | |
| Enable Reporting | Collect app usage; live report at `http://kidsplace.kiddoware.com/` | |
| Display Status bar | — | |
| Lock Notification Bar | Locks the notification bar | |
| Enable Higher Reliability | Requires ignore battery optimization permission | |
| Allow Internet Connection | Affects Wi‑Fi only, not data | |
| Wifi Toggle | Allow kids to turn Wifi on/off | |
| Always Start WiFi | Starts WiFi when app is on Kids Place Home Screen | |
| Web content filtering | Filter websites on this device, including in browsers | |
| Block Unapproved Apps Started From Approved Apps | May block some approved apps | |
| Allow kids to switch users. | With optional per‑user PIN | |
| Lock device volume control | — | |
| Lock device brightness control | — | |
| Change volume | — | |

### 6.5 Whitelist

```
┌───────────────────────────────────┐
│ ‹ Whitelist                       │
│ Add New Whitelist Package:        │
│ [ com.example.whitelistapp      ] │
│ White listed packages (apps) are  │
│ not shown on Kids Place Home      │
│ Screen.                           │
│                                   │
│ White Listed Packages             │
│  …                                │
└───────────────────────────────────┘
```

A **package‑name** based allow‑list that hides apps from the launcher — a power‑user feature with no explanation of the difference from the "allow" checkbox in Manage Apps.

### 6.6 Permission onboarding ("3/4")

```
┌───────────────────────────────────────────────────────────────────────┐
│ ‹ Kids Place                                                         │
│ The following permissions are required for Parental Controls Features  │
│ to work.                                                    3/4       │
│                                                                       │
│  App Info                                                             │
│  usage                                                                │
│  Data usage details                                                   │
│  Battery usage                                                        │
│  Manage notifications                                                 │
│  Permissions                                                          │
│  Display over other apps                                              │
│  Font Camera display                                                  │
│  Alarms & reminders                                                   │
│  App details in store                                                 │
│                                                                       │
│  App Critical Settings                                                │
│  [                GRANT PERMISSION               ]                    │
│  PRIVACY POLICY · HELP                                                │
└───────────────────────────────────────────────────────────────────────┘
```

- A **step counter (3/4)** implies a multi‑step permission wizard; only this step was captured.
- Each row is a link into an Android settings page.
- `App Critical Settings` is a second action below the list.

### 6.7 Wallpaper

- **Settings:** `Kids Select Wallpaper` toggle, `Rotating Wallpapers` with a frequency dialog (Minute / Hourly / Daily / Weekly / Never).
- **Gallery:** vertical list of packs — Stories, Playground, Animals, Cars, Kids Place Classics, **Customize**.
- Selecting **Customize** opens `com.google.android.photopicker` ("Choose a cloud media app" / "To also see your backed up photos and videos…"), i.e. a **custom photo wallpaper**.
- A **GALLERY** button and **DISABLE WALLPAPER** action sit bottom‑right.

### 6.8 Timer

Covered in §5.4.

### 6.9 Users

- **Select User** → `Default User`
- **Add user** → Name + `User Pin (optional)` + OK/CANCEL
- **Manage users** → user list with per‑user app assignment

### 6.10 User Interface Settings

| Setting | Options / value |
|---|---|
| Default Category View | Uncategorized / Categorized / Last Used |
| App title text color | colour swatches |
| App Icon Layout | Comfortable (wider icons) / Compact (more productive UI) |
| App Icon size | **96** (phone default was 64) |
| App Font Size | 14 |
| Icon Stretching | "Strech app icons to fit border" |
| Hide setting button | Hide in full‑screen mode |
| Show Grown Up Mode on start and exit | Grown Up Mode |

The **96 px icon size** is a tablet‑specific default — the phone used 64.

### 6.11 Backup, Account, Privacy, Security, Plugins

**Backup and Restore** — full‑purple branded screen with a logo block, account name/e‑mail, device list (`Xiaomi 25099RP13G`) and `CREATE NEW BACKUP` (bottom, full width).

**Manage Account** — "rongiancarlogiongco@gmail.com is currently linked to KidsPlace." + `DELETE ACCOUNT` + `UNLINK`.

**Privacy Settings**

| Setting | Description |
|---|---|
| Crash Reporting | Report crashes to Kiddoware to improve app quality |
| Analytics | Gather anonymous analytics to improve app experience |
| Error Logging | Enable logging to help Kiddoware support to troubleshoot an issue |

**Kids Place Security**

- `Update Kids Place Pin` → **CHANGE PIN**
- **Choose Security Type** → `Pin` / `Kiddoware Account Password` ("Will fallback to pin when internet is not available")
- **Forget Password Settings** → `Use Kiddoware Account`
- CANCEL / SAVE

**Kids Place Plugins**

- `View Kids Place plugins` → "Kids Place addons"
- `Display Plugins Shortcuts` → "Display Kids Place Plugins on Home Screen, at the bottom, as short cut."
- Entries: **Stories**, **Tasks**

### 6.12 Usage details

```
┌───────────────────────────────────┐
│ Day of week      Wednesday        │
│ Show usage By    App              │
│ Sort by          Time usage       │
│                                   │
│ KIDSPLACE                         │
│   Time used:   00:00:50           │
│   Day limit:   no limits          │
│   User:        Default User       │
│                                   │
│ AMdroid                           │
│   Time used:   00:00:00           │
│   Day limit:   23:59:00           │
│   User:        Default User       │
└───────────────────────────────────┘
```

A per‑app usage table with **day limit** and **user** columns — the same data the phone exposes in its child‑mode usage strip.

### 6.13 Help (child mode)

A full‑page, numbered help screen reached from child mode (`KidsPlace · 23:58 · EXIT`):

1. Go to *Settings > Manage Apps* to add/remove applications from Kids Place.
2. Application launched from Kids Place will be locked based on settings.
3. Pressing Home (or any other unauthorized action) while using apps launched from Kids Place will take the user back to Kids Place home screen. Pin is required to exit from app.
4. Go to *Settings > Parental Controls* to fine tune lock settings.
5. Checking **Lock home button** makes Kids Place more secure by changing Android home temporarily. *Recommended*
6. Checking **Auto restart apps** causes a launched app to restart within a few seconds of it terminating.
7. Checking **Airplane mode** turns off all wireless signals while Kids Place is running to prevent exposure to device radiation.
8. Checking **Enable wallpaper** uses the chosen image; when unchecked a black background is used.
9. Checking **Start on device reboot** causes Kids Place to start on reboot.

The `OK` button is bottom‑right (x ≈ 1 720).

### 6.14 Child mode (tablet)

```
┌───────────────────────────────────────────────────────────────────────┐
│ KidsPlace                                23:58                  EXIT  │ purple bar
│ HOME                                                                  │
│                                                                       │
│            [ full-bleed wallpaper: yellow / teal illustration ]       │
│                                                                       │
│   ▢ AMdroid                                                           │
└───────────────────────────────────────────────────────────────────────┘
```

- **EXIT** is a tablet‑only affordance in the top bar (the phone build has none).
- `HOME` label under the bar.
- Approved apps are laid out over the wallpaper; only `AMdroid` was legible in the capture.

---

## 7. Navigation model

```
Child launcher ──EXIT──► parent gate ──► Settings hub (3 columns)
                                            ├── Manage Apps (4-col grid) ──► app ──► limits
                                            ├── Manage Browser  (plugin modal)
                                            ├── Manage Videos   (plugin modal)
                                            ├── Parental Controls
                                            ├── Tasks / Remote Control
                                            ├── Wallpaper ──► gallery ──► photo picker
                                            ├── Timer ──► presets / clock
                                            ├── Screen Time Controls
                                            ├── Manage Users
                                            ├── Advanced Security
                                            ├── User Interface
                                            ├── Backup and Restore
                                            ├── Manage Account
                                            └── Privacy / Security / Plugins / Help
```

- Landscape gives room for **three‑ and four‑column layouts**, but the app still uses **back‑arrow only** navigation — no rail, no tabs, no breadcrumb.
- Because the hub is column‑major, the visual order does not match the logical grouping; users must scan 2‑D.
- Remote Control onboarding is a **linear 3‑step funnel** that terminates in OS settings — the only place the app hands control to another app.

---

## 8. Cross‑cutting patterns

| Pattern | Tablet implementation |
|---|---|
| **Parent gate** | 4‑digit PIN; brute‑force lockout (2 min after 3 failures) |
| **PIN recovery** | Kiddoware account password, or `app.kiddoware.com/forgotpin` |
| **Save semantics** | Explicit `UPDATE…` / `OK` / `APPLY` / `SAVE`; no autosave |
| **Destructive actions** | Red text (`DELETE ACCOUNT`, `UNLINK`, `DISABLE WALLPAPER`); no confirmation captured |
| **Dialogs** | Centred card + 60 % scrim; `CANCEL` left, primary right |
| **System hand‑offs** | Device admin (`com.android.settings`), MIUI danger dialog, photo picker |
| **Empty states** | Remote Control intro doubles as an empty state; no dedicated "no data" screens |
| **Theme** | Light throughout; system dialogs are dark → jarring contrast |
| **Localisation** | English only in this capture |
| **Accessibility** | Large targets; but 200‑character lines, grey‑on‑white 14 sp text, and icon‑only EXIT |

---

## 9. Implied data model

```
Device {
  model, manufacturer, android_id,
  form_factor: PHONE | TABLET,
  ui { icon_size (64|96), font_size, icon_layout, category_view, title_color },
  wallpaper { pack, item | custom_uri, rotation },
  users[], time_profiles[],
  security { pin, type: PIN|ACCOUNT_PASSWORD, forget_policy },
  remote { enabled, device_admin, battery_unrestricted, server_sync },
  plugins { safe_browser, video_player, stories, tasks }
}

AppPolicy {
  package, category, selected,
  daily_limit[7] | UNRESTRICTED,
  block_slots[7][24],
  internet: ON|OFF|CUSTOM,
  hidden_from_launcher: bool          // whitelist packages
}
```

---

## 10. UX / UI issues

| # | Severity | Issue |
|---|---|---|
| 1 | High | **Line length** — detail pages run to ~2 340 px (≈ 200 characters); long helper paragraphs are unreadable |
| 2 | High | **Theme split** — Manage Apps is light on tablet, dark on phone; the same component renders differently per form factor |
| 3 | High | **PIN is not masked** on entry screens |
| 4 | High | **Device‑Admin / MIUI danger dialogs** are system‑owned, with no in‑app record of what was granted or how to revoke |
| 5 | Medium | **Column‑major settings hub** — reading order is ambiguous; 3 columns × N rows with no visual grouping |
| 6 | Medium | Duplicated paths: Advanced Timer, Screen Time Controls, Timer and Parental Controls all expose overlapping schedule settings |
| 7 | Medium | `Block all timeslots` / `Clear all timeslots` are placed far right, disconnected from the grid |
| 8 | Medium | `EXIT` in the child top bar is a prominent, unconfirmed escape hatch next to the timer |
| 9 | Medium | Spelling/grammar: "Strech", "Regrequires", "WiFl", "Brc", "Settings is different", "for kids trying" |
| 10 | Medium | Whitelist (hide apps) vs. Manage Apps "allow" checkbox — two similar concepts with no explanation |
| 11 | Low | Status‑bar clock `12:31 Wed, Nov 18` competes with the in‑app timer `23:59` |
| 12 | Low | Display‑size change mid‑capture altered type scale — suggests no `dp`-based scaling discipline |
| 13 | Low | No tablet‑specific empty states; Remote Control intro is the only onboarding |
| 14 | Low | Inconsistent naming: "Kids Place" / "KidsPlace" / "Kidsplace" |

---

## 11. Recommendations for a re‑implementation (AnchorGuard / Guardify)

1. **Constrain measure.** Set a max content width (~900–1 100 px) and centre it on tablet; never run text to 2 300 px.
2. **Make the theme single‑sourced.** One component library, one dark/light decision, driven by system setting — not per form factor.
3. **Exploit landscape properly.** Use a **left navigation rail** on tablets (≥ 840 dp) instead of a column‑major settings grid; keep a single‑column detail pane with a max width.
4. **Two‑pane detail.** In Manage Apps, use a **master list + detail pane** (app list left, policy editor right) instead of modal‑per‑app.
5. **Group schedules into presets** (School, Bedtime, Homework) and provide drag‑paint on the week grid.
6. **Surface security state.** A "Device protection" summary showing Device Admin, battery exemption, accessibility and overlay grants, each with a *Revoke* path.
7. **Mask secrets** and add a proper numeric keypad; show lockout countdown.
8. **Rewrite settings copy** to ≤ 2 lines with "Learn more"; move Device Admin/battery/polling details into an expandable "Advanced" block.
9. **Fix reading order** in the hub: one column on phone, two on tablet, with clear group headers and sticky section titles.
10. **Instrument form factor** so the web console can show whether a device is a phone or tablet and adapt its remote commands (e.g., icon size 96 vs 64).

---

## Appendix A — Capture anomalies

| Observation | Explanation |
|---|---|
| Two different type scales across the set | System display‑size/font‑scale changed during capture (e.g. icon size 64 → 96, title 46 px → 40 px) |
| Manage Apps is white here, dark in the phone set | Either a system dark‑theme difference or a per‑form‑factor theme; no dark variant appears in this capture set |
| Dialogs render the page at `#666666` | 60 % black scrim |
| `SUN` label inside the app grid | Likely a category/first‑letter chip from the app list, not a weekday |
| MIUI/Android dialogs are dark | System UI theme, outside the app's control |
| No navigation bar captured | Gesture navigation or immersive mode |

## Appendix B — Accuracy statement

- All 64 images were processed with PP‑OCR (RapidOCR); text was cross‑checked against pixel geometry and column‑cluster analysis.
- No PIN, password or token value is reproduced in this document.
- The Android version, exact app version and device model string were not visible beyond the names shown in Backup/Remote Control.
- Loading, error and offline states were not captured and are therefore **not** documented.