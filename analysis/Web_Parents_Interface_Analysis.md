# Kids Place — Parent Web Interface (app.kiddoware.com)
## Full UI / UX Analysis

**Analysis date:** 2026-11-18
**Evidence base:** 65 screenshots in `/screenshot/Web Interface for Parents/`
**Primary properties observed:** `app.kiddoware.com` (authenticated parent/admin console), `kiddoware.com` (public marketing + user guide)
**Method:** OCR transcription (RapidOCR / PP-OCR) plus pixel-level colour, geometry and component analysis of every frame. No live account was available, so all statements below are derived from the captured frames only.

> **Confidence note.** Text was recovered with an OCR engine, therefore low‑confidence glyphs exist (e.g. `PROCESSED`, `Delete`, `PiN`, `liguors`). Where a reading is uncertain it is flagged **(OCR)**. Layout, colour and component geometry come from pixel analysis and are reliable to ±2 px at the captured resolution.

---

## 1. Executive summary

Kids Place's parent experience is a **desktop‑first single‑page web console** that manages Android child devices running the Kids Place parental‑control app. The console is organised around a **single global shell** — blue brand header, grouped left navigation, grey context/title bar, white content canvas — into which every feature is rendered.

Key characteristics:

| Aspect | Observation |
|---|---|
| Platform | Responsive web app (`app.kiddoware.com`), Edge‑class desktop browser in dark theme |
| Product pillars | **Kids Place** (app allow‑list, screen time, device control) and **Safe Browser** (URL/category web filtering, browsing schedule) |
| Navigation | Left sidebar with three groups: *Kids Place*, *Safe Browser*, *General*; each group repeats a "Home" item |
| Content model | Account → Devices → (Device settings / Remote control / Reports) + Account settings + Safe Browser + Billing |
| Visual language | Flat, Material‑inspired; single brand blue `#4E73DF`; 8/16/24 px spacing rhythm; white cards on white canvas separated by light grey rules |
| Density | High — settings pages are long single‑column lists; reports and logs are dense tables |
| States | Purpose‑built empty states, modal dialogs with a 40 % black scrim, inline status text (no toasts captured) |
| Maturity | Functional but inconsistent: duplicated labels, mixed capitalisation, duplicated navigation concepts, and several dead‑end screens |

---

## 2. Screen inventory

All captures are 2560 × 1600 except the dashboard, which is 1918 × 962 (no browser chrome).

| # | File (short) | Page / URL | What it shows |
|---|---|---|---|
| 1 | `2026-11-18 10_26_00-` | Home dashboard | Account home: onboarding banner, device list, quick actions, subscription card |
| 2 | `12-54-40-795` | Device settings (sidebar collapsed) | Advanced‑timer / tamper‑proofing toggles |
| 3 | `12-54-48-730` | `/device/e39becce86d893c7/setting` | Device settings header: name, copy‑settings, save/unregister/lock/unlock |
| 4 | `12-54-55-801` | Device communication log | Request/response log table (`deviceStatusRequest`, `configRequest`) |
| 5 | `12-54-57-084` | Device communication log | Log table (continued, older entries) |
| 6 | `12-54-58-277` | Device communication log | Log table (scrolled) |
| 7 | `12-54-59-374` | Device communication log | Log table (scrolled, footer) |
| 8 | `12-55-09-137` | Remote control | Message‑to‑device + sound‑alarm panel |
| 9 | `12-55-13-218` | Remote control | Device list, online status, health status |
| 10 | `12-55-47-560` | `/device/fb9925719eeb782b` | Remote control – Apps tab (manage apps, categories, settings) |
| 11 | `12-55-54-860` | `/kp/reports` | Reports: range picker, top apps, usage table |
| 12 | `12-56-00-377` | `/kp/reports` | Reports – empty state |
| 13 | `12-56-04-085` | `/kp/reports` | Reports – "Not available any used apps" |
| 14 | `12-56-07-724` | `/account/settings` | Account settings (email, name, timezone, language) |
| 15 | `12-56-09-899` | Account → Data management | Delete reports / logs / account |
| 16 | `12-56-11-692` | Account → Notifications | Notification preferences (device / remote / events) |
| 17 | `12-56-13-157` | Account → Notifications | Notification preferences (scrolled) |
| 18 | `12-56-21-629` | `/kpsb/home` | Safe Browser – no devices connected |
| 19 | `12-56-27-815` | `/filters` | Filter rules – modes + category list start |
| 20–32 | `12-56-29` … `12-56-52` | `/filters` | Category taxonomy A→W (sidebar collapsed, scrolled) |
| 33 | `12-56-54-023` | `/filters` + modal | "Add Filter" dialog (white list) |
| 34 | `12-56-57-572` | `/filters` + modal | "Add Keyword" dialog |
| 35 | `12-57-00-296` | `/filters` + modal | "Add Filter" dialog (black list) |
| 36–47 | `12-57-03` … `12-57-22` | `/filters` | Category list (sidebar visible, scrolled) |
| 48 | `12-57-33-421` | `/kpsb/scheduler` | Scheduler – top (enable, scope, bulk ops) |
| 49–52 | `12-57-35` … `12-57-39` | `/kpsb/scheduler` | Weekly 30‑minute grid (scrolled) |
| 53 | `12-57-51-980` | Notifications inbox | Empty state |
| 54 | `12-58-04-343` | `kiddoware.com/kids-place-remote-control-user-guide/` | Public user guide – hero + intro |
| 55–65 | `12-58-06` … `12-58-30` | same | Guide sections: setup, console tour, remote options, reporting, admin app, footer |

---

## 3. Global UI shell

### 3.1 Browser and viewport

- Captured in **Microsoft Edge (dark theme)** at **2560 × 1600**, with the Windows taskbar clock visible (`12:54 Wed, Nov 18`).
- Browser chrome occupies roughly the top **250 px** (tab strip + address bar) when visible; some frames are captured with a reduced **≈60 px** chrome (window edge / auto‑hide), which is why the app header appears at y≈60 in some frames and y≈253 in others.
- The app itself is anchored to the top of the viewport and scrolls internally; the **blue header and the blue footer stay fixed**, the sidebar scrolls with the page.

### 3.2 Colour system

| Token | Value | Usage |
|---|---|---|
| Brand blue | `#4E73DF` (78,115,223) | App header, footer, primary buttons, sidebar icons, links |
| Brand blue (hover/dark) | `#3B5BDB` (59,91,219) | "Install on my phone" button |
| Success / online | `#1CC88A` (28,200,138) | "Online" and "Active" badges |
| Page head band | `#EEEEEE` | Context/title strip under the header |
| Card / subtle fill | `#FAFAFA` | Device card, inset panels |
| Active nav band | `#E8E8E8` | Full‑width background of the selected sidebar row |
| Hero / callout | `#EEF2FF` | Onboarding banner background |
| Body text | `#141414` / `#292929` | Primary labels |
| Secondary text | `#757575` | Descriptions, timestamps |
| Icon / muted | `#9E9E9E` | Dividers, inactive controls |
| Danger | `#ED554E` (237,85,78) | Delete / destructive actions |
| Warning | `#F47F16` (244,127,22) | "Application Stopped" / caution states |
| Modal scrim | `rgba(0,0,0,0.4)` → renders `#999999` | Behind dialogs |

The palette is deliberately narrow: one blue, one green, one red, one orange, plus a grey ramp. No gradients are used in the app chrome; the only multi‑colour element is the logo mark (cyan `#008BD0` + amber `#F7A701` on brand blue).

### 3.3 Typography

- Sans‑serif UI typeface (system/Inter‑like). No serif or display face in the console.
- Scale observed (device px at 2560 wide, i.e. ≈2× CSS px):

| Role | Approx. size | Weight | Colour |
|---|---|---|---|
| Page title (context bar) | 41 px | 600 | `#414141` |
| Card / panel title | 33 px | 600 | `#000000` |
| Sidebar nav label | 30 px | 500 | `#000000` |
| Sidebar group label | 29 px | 600 | `#747474` |
| In‑card section heading | 23 px | 600 | `#656565` |
| Body / list row | 24–30 px | 400 | `#292929` |
| Secondary / caption | 24 px | 400 | `#757575` |
| Button label | 28 px | 600 | white / brand blue |
| Table header | 24 px | 600 | `#757575` |

- Group labels ("Kids Place", "Safe Browser", "General") sit at the same size as nav items but are grey and inset only 32 px, while nav labels are inset 142 px — the hierarchy is carried by colour and indent, not size.
- Lists use a **110 px row pitch** (settings rows and nav rows alike), giving an airy, low‑density feel.

### 3.4 Layout grid

At 2560 px the shell resolves to:

```
0          40                     592  645                        2500  2560
+-----------+----------------------+    +---------------------------+-----+
|  HEADER   |<------ 99 px ------->|    |<------- 99 px ----------->|     |
+-----------+----------------------+----+---------------------------+-----+
|           |                           |                           |
| SIDEBAR   |  CONTEXT / TITLE BAR (~122) |  account  title  action  |
| 592 px    |                           |                           |
| (23%)     +---------------------------+---------------------------+
|           |                           |                           |
|  nav      |        CONTENT CANVAS     |  x 645 → 2500 (1855 px)   |
|  groups   |        (white)            |  60 px right margin       |
|           |                           |                           |
+-----------+---------------------------+---------------------------+
|  FOOTER (99 px, brand blue, credit line "Created by Kiddoware @     |
|  General Solutions and Services, LLC 2026")                          |
+-----------------------------------------------------------------------+
```

- **Sidebar**: 592 px (≈23 % of viewport), white, 1 px `#E0E0E0` right border at x≈592 with a 6 px shadow, then a 48 px gutter before content starts at x≈645. Items are full‑bleed rows (111 px pitch) with a 40 px icon column (78 px icon at x≈34) and a 36 px gap before the label; the **active item is a full‑width `#E8E8E8` band** (x 0 → 592, 111 px tall) with a brand‑blue icon and near‑black label, while inactive rows stay white.
- **Content**: starts at x≈645 and runs to x≈2496, leaving a narrow ≈60 px right margin at 2560. Content is **not centred in a narrow column** — cards and tables stretch to the full content width.
- **Vertical rhythm**: 8 px micro, 24 px intra‑card, 40 px between cards, 56 px between sections.
- At the smaller 1918 px capture the sidebar is 320 px and content starts at x≈330, i.e. the layout is **fluid, not fixed‑width**.

### 3.5 Shared components

| Component | Description |
|---|---|
| **App header** | 99 px brand‑blue bar. Left: 76 px logo mark + "Kidsplace" wordmark (32 px, white). Right: two circular 48–52 px icon buttons (most likely notification bell and account/avatar menu) with white glyphs. |
| **Context / title bar** | 122 px light‑grey (`#EEEEEE`) strip. Three zones: account name (left, aligned to sidebar), page/device title + secondary identifier (centre‑left), primary action button(s) (right). |
| **Sidebar group label** | 29 px, grey `#747474`, sentence case, 32 px left inset, 40 px above first item. |
| **Sidebar item** | 111 px tall row, 30 px label at x≈147, 78 px blue icon at x≈34; active = `#E8E8E8` full‑width band, blue icon, black label. |
| **Card** | White or `#FAFAFA` surface, 16–20 px radius, 1 px `#E6E6E6` border, soft shadow, 24 px padding, 24 px header + optional action link. |
| **Primary button** | Brand blue `#4E73DF`, white 600 label, 48–56 px tall, 8 px radius, uppercase label in dense pages ("SHOW REPORTS", "SAVE SETTINGS TO DEVICE"). |
| **Secondary / ghost button** | White with 1 px `#D0D0D0` border, dark label (e.g. "Not now"). |
| **Destructive button** | Red `#ED554E` outline or text ("DELETE ACCOUNT", "Cancel License"). |
| **Toggle switch** | Pill switch, blue when ON, grey `#E0E0E0` when OFF, 56 × 30 px, label to its left. |
| **Radio group** | Circular radio + label, used for filter modes ("Block all internet", …) and schedule scope ("All Web Activities" / "Specific Website"). |
| **Tabs** | Underlined text tabs in brand blue with a 3 px blue underline ("Apps · Device · Status"). |
| **Badge** | Pill, 24 px radius: green `#1CC88A` "Online"/"Active", orange `#F47F16` "Application Stopped". |
| **Data table** | Header row in grey caps with a 1 px rule; zebra‑free rows separated by hairlines; right‑aligned numeric columns; destructive row action in red. |
| **Empty state** | Centred muted message inside a bordered dashed/solid card ("No devices connected yet.", "Not available any notification yet", "Sorry! I didn't find anything for you"). |
| **Modal** | 720–900 px card, 24 px radius, 40 % black scrim, title + close ×, labelled input, helper text, Cancel / ADD buttons. |
| **Footer** | Full‑width brand‑blue bar with white credit text. |

### 3.6 Information architecture

```
Account (Ron Gian Carlo Giongco)
│
├── Kids Place
│   ├── Home ................. account dashboard (devices, quick actions, billing)
│   ├── Remote Control ....... per‑device console (Apps / Device / Status)
│   ├── Screen Time Controls . advanced timer & schedule
│   └── Reports .............. app usage + launch reports, CSV export
│
├── Safe Browser
│   ├── Home ................. Safe‑Browser device list (empty state in capture)
│   ├── Filter Rules ......... mode, category taxonomy, whitelist, blacklist, keywords
│   ├── Screen Time Controls . per‑category web schedule
│   └── Reports .............. Safe‑Browser browsing reports
│
├── General
│   ├── Notifications ........ in‑app notification inbox
│   ├── Help ................. help centre link
│   ├── Privacy Policy
│   └── View plans ........... subscription / billing
│
└── Account (context bar → /account/settings)
    ├── Profile (email, name, timezone, language, password)
    ├── Data management (delete reports, logs, account)
    └── Notification preferences
```

Notable IA problems:

1. **"Home" appears twice** (once per pillar) with no visual distinction beyond the group label — ambiguous target.
2. **"Screen Time Controls" appears twice**, once for apps and once for web, with identical labels.
3. **Reports appear twice** (Kids Place reports and Safe Browser reports) with identical labels.
4. **Device context is only in the title bar**; there is no persistent device switcher in the sidebar.
5. **Account settings is not in the sidebar** — it lives behind the account name in the title bar and is easy to miss.

---

## 4. Page‑by‑page analysis

### 4.1 Home dashboard (`/`, capture #1)

**Purpose:** post‑login landing page: orient the parent, list devices, offer quick actions, surface subscription state.

**Layout (1918 × 962, no browser chrome):**

```
┌───────────────────────────────────────────────────────────────────────┐ 48 px  #4E73DF
│ ▣ Kidsplace                                        (bell) (avatar)    │
├───────────────┬───────────────────────────────────────────────────────┤
│ Ron Gian      │ ▓ Stay informed in real time            (light blue)  │ 122 px
│ Carlo Giongco │ ▓ Install Kids Place on your own phone in parent      │ banner
│               │ ▓ mode to get instant push notifications ...          │ #EEF2FF
│ Kids Place    │ ▓ [ Install on my phone → ]   Not now                 │
│  Home      ◄──┤───────────────────────────────────────────────────────┤
│  Remote Ctrl  │ Your Devices                        [Search your device] │
│  Screen Time  │ ┌───────────────────────────────────────────────────┐ │
│  Reports      │ │ Gabby's Tablet        e39becce86d893c7             │ │
│               │ │ M2102J20SG            fb9925719eeb782b             │ │ card #FAFAFA
│ Safe Browser  │ └───────────────────────────────────────────────────┘ │
│  Home         │ QUICK ACTIONS                                           │
│  Filter Rules │ [ Remote control ] [ Reports        ]                 │
│  Screen Time  │ [ Manage device  ] [ App & web usage]                 │
│  Reports      │ [ Filter rules   ] [ Notifications  ]                 │
│               │ [ Safe browser   ] [ Alert settings ]                 │
│ General       │ ─────────────────────────────────────────────────────  │
│  Notifications│ Subscriptions                      [● Active]         │
│  Help         │ Kiddoware · Valid through: Dec 18, 2026 · 30 days left│
│  Privacy      │ View Full Cancellation Policy      [ View Plans ]     │
├───────────────┴───────────────────────────────────────────────────────┤
│ Created by Kiddoware @ General Solutions and Services, LLC 2026       │ 49 px #4E73DF
└───────────────────────────────────────────────────────────────────────┘
```

**Elements**

| Element | Detail |
|---|---|
| Onboarding banner | Light‑blue callout with a small illustration/icon, headline "Stay informed in real time", two‑line body, blue CTA "Install on my phone →", ghost dismiss "Not now". Dismissible. |
| Device card | `#FAFAFA` panel listing **two** devices: `Gabby's Tablet / e39becce86d893c7` and `M2102J20SG / fb9925719eeb782b` (name + 16‑hex device id). No status badge on this card. |
| Device search | Search field with magnifier, placeholder "Search your device". |
| Quick actions | 2 × 4 grid of ghost tiles: Remote control, Manage device, Filter rules, Safe browser, Reports, App & web usage, Notifications, Alert settings. |
| Subscription card | Title "Subscriptions", green **Active** pill (right‑aligned), product line "Kiddoware", "Valid through: Dec 18, 2026 · 30 Days left", text link "View Full Cancellation Policy", destructive text action "Cancel License ?", blue "View Plans" button. |
| Sidebar | As §3.6; **Home** active (grey band). |
| Footer | Blue credit bar. |

**Observations**

- The most important object (the child device) is visually weaker than the subscription upsell; the device card has no status indicator (online/offline), no battery, no "last seen".
- The hero banner occupies prime real estate for an app‑install promotion that returning users cannot permanently dismiss (only "Not now").
- "Cancel License ?" has a stray space before the question mark — visible polish defect.

---

### 4.2 Device settings (`/device/{id}/setting`, captures #2–#3)

**Purpose:** edit the child device's Kids Place configuration remotely.

**Layout:**

```
┌──────────────────────────────────────────────────────────────────────┐
│ ▣ Kidsplace                                   (bell) (avatar)         │
├──────────────────────────────────────────────────────────────────────┤
│ Ron Gian Carlo Giongco │ 25099RP13G · e39becce86d893c7  [Get data…] │ grey bar
├───────────────┬──────────────────────────────────────────────────────┤
│ Kids Place    │ Device Name            [ 25099RP13G              ]   │
│  Home      ◄──┤ Copy settings from:    [ 25099RP13G ▾ ]             │
│  Remote Ctrl  │ ─────────────────────────────────────────────────    │
│  Screen Time  │ Enable Advanced Timer                        ( ●)    │
│  Reports      │ Run In Background Mode                       ( ●)    │
│               │ Kids Place Icon In Block Message             ( ●)    │
│ Safe Browser  │ Always Start on Device Boot.                 ( ●)    │
│  Home         │ Enable Tamper Proofing                       ( ●)    │
│  Filter Rules │ Change Application Title   [ Kids Place      ]       │
│  Screen Time  │ Keep Alive Kids Place                        ( ●)    │
│  Reports      │ Enable Brute Force PiN Protection            ( ●)    │
│               │ Blocking Frequency in MilliSeconds [ 500 ]           │
│ General       │ ─────────────────────────────────────────────────    │
│  Notifications│ [ SAVE SETTINGS TO DEVICE ]                          │
│  Help         │ [ UNREGISTER DEVICE ]  [ LOCK THIS DEVICE ]          │
│  Privacy      │ [ UNLOCK THIS DEVICE ]                               │
└───────────────┴──────────────────────────────────────────────────────┘
```

**Controls**

| Control | Type | Notes |
|---|---|---|
| Device Name | Text input | Defaults to the device model (`25099RP13G`, `M2102J20SG`) |
| Copy settings from | Select | Clone another enrolled device's config |
| Enable Advanced Timer | Toggle | Master switch for the timer subsystem |
| Run In Background Mode | Toggle | Keeps the child‑side agent alive |
| Kids Place Icon In Block Message | Toggle | Shows the app icon on the lock screen |
| Always Start on Device Boot | Toggle | Boot persistence |
| Enable Tamper Proofing | Toggle | Anti‑uninstall / anti‑bypass |
| Change Application Title | Text input | Lets the parent rename the child app (default "Kids Place") |
| Keep Alive Kids Place | Toggle | Watchdog |
| Enable Brute Force PiN Protection | Toggle | Lock‑out after failed PIN attempts |
| Blocking Frequency in MilliSeconds | Numeric input | Default `500` |
| Save / Unregister / Lock / Unlock | Buttons | Save is primary; unregister/lock/unlock are destructive/danger |

**Observations**

- All destructive device operations sit on the same plane as "Save" with only colour to differentiate; no confirmation copy is visible in the captured states.
- Technical parameters (milli‑seconds, keep‑alive, tamper proofing) are exposed verbatim — written for administrators, not parents.
- The page is a single long form with **no sectioning, no grouping and no sticky save bar**; the save button is below the fold.
- "Copy settings from" is powerful but has no explanatory text.

---

### 4.3 Remote control console (`/device/{id}`, captures #8–#10)

**Purpose:** real‑time device operations: push config, message, alarm, app management, health.

**Layout:**

```
┌──────────────────────────────────────────────────────────────────────┐
│ header                                                               │
├──────────────────────────────────────────────────────────────────────┤
│ Ron Gian Carlo Giongco │ 25099RP13G · e39becce86d893c7 [Get data…]  │
├───────────────┬──────────────────────────────────────────────────────┤
│ sidebar       │ Device List                       [● Online]         │
│               │ 25099RP13G                                           │
│               │ ── Apps │ Device │ Status ──────────────────────      │
│               │                                                      │
│               │  ┌── Apps ───────────┐ ┌── Device ─────────┐        │
│               │  │ Manage Apps       │ │ Send message to   │        │
│               │  │ Categories &      │ │ device            │        │
│               │  │ Layout            │ │ [____________]    │        │
│               │  │ Screen Time Ctrl  │ │        [ SEND ]   │        │
│               │  │ Kidsplace Settings│ │ [SOUND DEVICE ALARM]       │
│               │  └───────────────────┘ └───────────────────┘        │
│               │                                                      │
│               │  ┌── Status ──────────────────────────────────┐    │
│               │  │ Kids Place        ● Running                │    │
│               │  │ Last opened: 2026-11-18 12:54:04           │    │
│               │  │ Application Stopped   ⚠                    │    │
│               │  └────────────────────────────────────────────┘    │
└───────────────┴──────────────────────────────────────────────────────┘
```

**Tabs**

1. **Apps** — "Manage Apps", "Categories & Layout", "Screen Time Controls", "Kidsplace Settings" (mirrors the child app's own menus).
2. **Device** — "Send message to device" (free‑text + SEND) and "SOUND DEVICE ALARM".
3. **Status** — device list with online badge, health card ("Kids Place", "Last opened: …", "Application Stopped" warning).

**Observations**

- The tab bar is the only way to reach device‑level operations; there are no deep links from the dashboard quick actions to a specific tab.
- "Application Stopped" uses an orange warning colour, but there is **no remediation action** next to it (no "restart app" / "push config").
- The status card is the only place where device health is visible; it is not surfaced on the dashboard.
- Free‑text messaging is a powerful remote‑control primitive with no character limit or preview shown.

---

### 4.4 Device communication log (captures #4–#7)

**Purpose:** diagnostic transcript of device↔cloud traffic.

```
┌───────────────────────────────────────────────────────────────┐
│ Device Communication Logs            [DELETE DEVICE COMMUNICATION LOGS] │
│ ┌──────────────┬────────────┬──────────────────────────────┐  │
│ │ Type         │ Status     │ Timestamp                    │  │
│ ├──────────────┼────────────┼──────────────────────────────┤  │
│ │ deviceStatusRequest │ PROCESSED │ 2026-11-18 12:53:57   │  │
│ │ configRequest       │ PROCESSED │ 2026-11-18 12:53:57   │  │
│ │ deviceStatusRequest │ PROCESSED │ 2026-11-18 12:53:56   │  │
│ │ configRequest       │ PROCESSED │ 2026-11-18 12:53:56   │  │
│ │ …                   │           │                        │  │
│ └──────────────┴────────────┴──────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘
```

- Two request classes only: `deviceStatusRequest` and `configRequest`, both `PROCESSED`.
- Rows are newest‑first; entries arrive in bursts (e.g. `12:53:56–57`) separated by minutes — consistent with a status heartbeat plus a config poll.
- Timestamps use `YYYY-MM-DD HH:mm:ss`.
- Destructive action "DELETE DEVICE COMMUNICATION LOGS" sits in the page header, visually equal to the title.
- The log has **no filtering, no pagination and no level column** — it is a raw firehose.

---

### 4.5 Reports (`/kp/reports`, captures #11–#13)

**Purpose:** app‑usage analytics per device.

```
┌───────────────────────────────────────────────────────────────────────┐
│ Reports                                                               │
├───────────────┬───────────────────────────────────────────────────────┤
│ sidebar       │ Device List [ e39becce86d893c7 ▾ ]                    │
│               │ 2026/11/05 - 2026/11/18        [SHOW REPORTS]         │
│               │ [DELETE REPORTS FOR SELECTED DEVICE]                  │
│               │ ┌── Top Apps Used ──┐ ┌── Top Apps Launched ──┐     │
│               │ │ (list / bars)     │ │ (list / bars)         │     │
│               │ └───────────────────┘ └───────────────────────┘     │
│               │ Usage Report                        [Export to csv]  │
│               │ Application │ Times Launched │ Times Spent │ Times   │
│               │             │                │             │ Spent(s)│
│               │ ...                                                                 │
└───────────────┴───────────────────────────────────────────────────────┘
```

- Date‑range display `2026/11/05 - 2026/11/18` (looks like a read‑only label or a date‑range picker).
- Two summary panels: **Top Apps Used**, **Top Apps Launched** — rendered as text lists, not charts (no saturated chart colours detected in the capture; only the blue action button).
- **Usage Report** table with four columns: `Application`, `Times Launched`, `Times Spent`, `Times Spent (Seconds)`.
- **CSV export** at the right edge of the Usage Report header.
- Empty states: "Sorry! I didn't find anything for you" and "Not available any used apps".
- No per‑day chart, no category breakdown, no filtering by app category — a significant analytics gap versus the child‑app's own reporting.

---

### 4.6 Account settings (`/account/settings`, capture #14)

```
┌────────────────────────────────────────────────────────────┐
│ Your Account Settings                                      │
│  Email                rongiancarlogiongco@gmail.com        │
│                       ✓ Email verified                     │
│  Name                 Ron Gian Carlo Giongco                │
│  Timezone             [ …                    ▾ ]           │
│  Language             en                                   │
│  [ SAVE THESE CHANGES ]                                    │
│  [ DELETE ACCOUNT ]   [ CHANGE PASSWORD ]                  │
└────────────────────────────────────────────────────────────┘
```

- Read‑only email with a green "Email verified" badge.
- Timezone is a select; language is a short code (`en`).
- Two destructive/secondary actions share a row with no visual hierarchy.
- No avatar/photo, no two‑factor, no session list.

---

### 4.7 Data management (capture #15)

| Card | Description | Action |
|---|---|---|
| **API Logs** | Kidsplace API communication logs | `Delete all` |
| **Kidsplace Reports** | "Delete all Kidsplace app device usage reports and statistics" | `DELETE KIDSPLACE REPORTS` |
| **Device communication** | "Delete all device communication logs" | `DELETE DEVICE COMMUNICATION LOGS` |
| **Safe Browser Reports** | "Delete all Safe Browser browsing reports and website data" | `DELETE SAFE BROWSER REPORTS` |

- All four cards are destructive, all use the same red treatment, and none shows record counts or last‑purge time.
- This page is the *only* GDPR‑style data‑erasure surface; there is no export (data‑portability) counterpart even though CSV export exists for reports.

---

### 4.8 Notification preferences (captures #16–#17)

Three preference blocks, each with two independent channels:

| Block | Description (verbatim) | Channels |
|---|---|---|
| Kids Place device status change | "Turn this option on, if you want to receive notification when a Kids Place device status change." | Desktop · Email |
| Kids Place Remote Control status change | "Turn this option on, if you want to receive notification when a Kids Place Remote Control status change." | Desktop · Email |
| Kidsplace events | "Turn this option on if you want notifications for Kids Place app blocks, screen time limits, and Safe Browser website blocks." | Desktop · Email |

- Six toggles in total, no "push to my phone" channel (despite the dashboard banner promoting the parent app for instant push).
- No quiet hours, no per‑device scoping, no digest option.

---

### 4.9 Safe Browser — device list (`/kpsb/home`, capture #18)

```
┌────────────────────────────────────┐
│ Your Devices                       │
│                                    │
│   No devices connected yet.        │
│   Install Safe Browser on your     │
│   child's device to filter the     │
│   web and manage browsing.         │
│   [ HOW TO ADD DEVICE? ]           │
│                                    │
│ Subscriptions            [● Active]│
│ View Full Cancellation Policy      │
│ Kiddoware · Valid through: Dec 18, │
│ 2026 · 30 Days left   [View Plans] │
└────────────────────────────────────+
```

- Empty state is friendly and actionable, but the "HOW TO ADD DEVICE?" link is the only path and it is low‑contrast.
- The subscription card is **duplicated** from the dashboard, reinforcing the upsell but adding noise.

---

### 4.10 Filter rules (`/filters`, captures #19–#47)

**Page structure**

```
┌────────────────────────────────────────────────────────────────────┐
│ Filter Rules                                                       │
├───────────────┬────────────────────────────────────────────────────┤
│ sidebar       │ ( ) Block all internet                             │
│               │ ( ) Block all blacklisted categories               │
│               │ (•) Allow My Whitelist only                        │
│               │ ( ) Block My Blacklist only                        │
│               │ ( ) Block by category's keywords                   │
│               │ ────────────────────────────────────────────────    │
│               │ Categories                                           │
│               │ Abortion            Sites/… (toggle)               │
│               │ Ads                 Advert servers …  (toggle)     │
│               │ Adult               …               (toggle)       │
│               │ …  (78 entries, A→W)                               │
│               │ ────────────────────────────────────────────────    │
│               │ Whitelist  [+ ADD]     Blacklist [+ ADD]           │
│               │ Keywords   [+ ADD]                                 │
└───────────────┴────────────────────────────────────────────────────┘
```

**Filtering modes** (mutually exclusive radio group)

1. Block all internet
2. Block all blacklisted categories
3. Allow My Whitelist only
4. Block My Blacklist only
5. Block by category's keywords

**Category taxonomy** — 78 entries as captured, each with a short description and an on/off control:

`Abortion, Ads, Adult, Aggressive, Alcohol, Antispyware, Artistic nudity, Arts, Astrology, Audio/video downloads, Banking, Books, Business, Celebrity, Childcare, Cleaning, Clothing, Contraception, Crypto Currency, Culinary, Dating, Drugs, Ecommerce, Education, Educational games, Entertainment, File hosting, Gambling, Games, Gardening, General, Government, Hacking, Home repair, Hunting, Hygiene, ISP, Job search, Kids timewasting, Mail, Malware, Manga, Medical, Military, Mobile phones, Movies, Music, Naturism, News, Pets, Podcasts, Politics, Porn, Press, Proxy, Radio, Religion, Ringtones, School Cheating, Search engines, Sect, Sexuality education, Social networking, Sports, Streaming, Technology, Tobacco, Translation, URL shorteners, Vacation, Violence, Virus infected, Warez, Weapons, Weather, Web Based Email, Web Meetings, Webphone`

**Observations**

- The category list is a flat, alphabetically sorted wall of 78 rows with no search, no "select all", no grouping and no indication of how many are enabled.
- Several descriptions are machine‑translated and contain errors: *"Sexuality eductaionion only on beer or liquors"*, *"Sites that does not fit on other catergoies"*, *"All snort sites"*, *"Sites detailind or sellina weapons"*, *"Can harm your device or os"*.
- "Kids timewasting" and "School Cheating" are value‑judgement categories with no explanation.
- The whitelist/blacklist/keyword managers are collapsed into three small rows at the bottom — the most used feature is the least discoverable.

---

### 4.11 Add Filter / Add Keyword dialogs (captures #33–#35)

**Add Filter (white list / black list)**

```
┌───────────────────────────────────────────────┐
│  Add Filter                                 × │
│                                               │
│  URL to add to White list                     │
│  ┌─────────────────────────────────────────┐  │
│  │ https://somesite.com/adult/*            │  │
│  └─────────────────────────────────────────┘  │
│  Use asterisk (*) as wildcard symbol.         │
│  Example: https://somesite.com/adult/*        │
│                                               │
│  Show in start page                           │
│  ( ) Whole Domain   ( ) With subdomains       │
│                                               │
│                         [Cancel]  [ ADD ]     │
└───────────────────────────────────────────────┘
```

**Add Keyword**

```
┌───────────────────────────────────────────────┐
│  Add Keyword                                × │
│  Keyword to Block                             │
│  ┌─────────────────────────────────────────┐  │
│  └─────────────────────────────────────────┘  │
│                              [Cancel] [ADD]   │
└───────────────────────────────────────────────┘
```

- Both dialogs use the same shell, 40 % black scrim, 24 px radius.
- Validation states, duplicate detection and success feedback were not captured.
- "Show in start page" is a Safe‑Browser‑specific option that has no explanation.

---

### 4.12 Safe Browser scheduler (`/kpsb/scheduler`, captures #48–#52)

```
┌────────────────────────────────────────────────────────────────────┐
│ Scheduler                                                          │
├───────────────┬────────────────────────────────────────────────────┤
│ sidebar       │ Enable time limit                             (●)  │
│               │ (•) All Web Activities  ( ) Specific Website       │
│               │ Time limit for category: [ All Web Activities ▾ ]  │
│               │ Schedule for all browser activities                │
│               │ Tap on days/slots to block access                  │
│               │ [Bulk Operations ▾]                                │
│               │         Mon  Tue  Wed  Thu  Fri  Sat  Sun          │
│               │  00:00 | 00:00| 00:00| …                          │
│               │  00:30 | 00:30| 00:30| …                          │
│               │  01:00 | …                                       │
│               │  …  (48 half‑hour rows)                          │
│               │  █ Blocked   ▢ Allowed                            │
└───────────────┴────────────────────────────────────────────────────┘
```

- 7 columns × 48 half‑hour rows = **336 clickable cells**; every cell repeats its time label, which creates visual noise.
- Legend uses a blue swatch for "Blocked" and an empty swatch for "Allowed".
- "Bulk Operations" is the only affordance for filling ranges — essential given 336 cells.
- Scope switch ("All Web Activities" vs "Specific Website") changes the grid target; the "Time limit for category" select is the bridge to the category taxonomy.

---

### 4.13 Notification inbox (capture #53)

```
┌──────────────────────────────────────────┐
│ Notifications                            │
│                                          │
│   Not available any notification yet     │
│                                          │
└──────────────────────────────────────────┘
```

- Empty state only; no filters, no mark‑all‑read, no link to preferences.
- Grammatical error in the empty‑state copy ("Not available any notification yet").

---

### 4.14 Public website — Remote Control user guide (captures #54–#65)

- URL: `kiddoware.com/kids-place-remote-control-user-guide/`
- Public marketing site with top nav: **Kiddoware · Product · Plans · Support · About · Login · English** and a hero "Best Parental Control apps for Android".
- The guide documents the console itself, which makes it a useful specification source:
  - Remote Control must be enabled on the child device first.
  - Parents/admin log in with **their own** e‑mail (the child never needs an e‑mail address).
  - Console capabilities: add/remove apps, manage categories, change settings, advanced timer, view reports.
  - Menu options: **Unregister Device**, **Request Config from Device**, **Save and Push to Device**, plus Kids Place Management / Device Management / Kids Place Health Status.
  - Tabs: Approved Apps, Categories and Layout, Settings, Scheduler; Lock / Unlock / Start Kids Place.
  - Device Management can uninstall a listed app or install from a URL; "push content" sends URLs, files and messages.
- The guide is text‑heavy, single‑column, full‑width (x≈140 → 2400), with numbered lists and inline screenshots.

---

## 5. Cross‑cutting interaction patterns

| Pattern | Implementation |
|---|---|
| **Navigation** | Left sidebar, single‑level groups, grey band for the active item; no breadcrumbs anywhere |
| **Device context** | Global "selected device" shown only in the grey title bar; switching requires going back to the dashboard |
| **Save semantics** | Explicit save buttons per page; **no dirty‑state guard, no autosave, no "unsaved changes" warning observed** |
| **Destructive actions** | Red text/buttons; no confirmation dialog was captured, only `?` suffixes ("Cancel License ?") |
| **Feedback** | Inline status text (`PROCESSED`), badges (`Online`, `Active`), and toast‑free copy; no optimistic UI indicators |
| **Empty states** | Consistent centred copy inside a card; always paired with a remediation link except for logs |
| **Loading** | Not captured; `Get data from device` implies a manual refresh model rather than live sync |
| **Search** | Only on the dashboard device list; no global search |
| **Bulk actions** | Only in the scheduler ("Bulk Operations") and data management ("Delete all") |
| **Export** | CSV on reports only |
| **Localisation** | Language selector (`en`) present but no language switcher in the header; mixed en‑US/en‑GB strings ("customize"/"centre") |

---

## 6. Implied data model

```
Account {
  id, email, email_verified, name, timezone, language, password_hash,
  created_at, subscription { plan, status, valid_through, days_left }
}

Device {
  id, hex_id (16 chars, e.g. e39becce86d893c7),
  name, model (e.g. 25099RP13G / M2102J20SG),
  online, last_seen, app_version,
  settings { advanced_timer, background_mode, block_icon, boot_start,
             tamper_proof, app_title, keep_alive, brute_force_pin,
             block_interval_ms },
  safe_browser { enabled, mode, categories[], whitelist[], blacklist[],
                 keywords[], schedule[7][48] },
  health { kp_running, remote_running, last_opened, last_config_request }
}

CommunicationLog { id, device_id, type: deviceStatusRequest|configRequest,
                   status: PROCESSED, created_at }

UsageReport { device_id, date, app, times_launched, times_spent_sec }
```

The 16‑hex `device_id` is used both as a path segment (`/device/e39becce86d893c7/...`) and as a display identifier — a design that couples the URL to an opaque hardware fingerprint.

---

## 7. UX / UI issues

| # | Severity | Issue |
|---|---|---|
| 1 | High | No device switcher outside the dashboard; every device action requires a round‑trip |
| 2 | High | Duplicate navigation labels ("Home", "Screen Time Controls", "Reports" ×2) with no pillar prefix |
| 3 | High | Long settings form with save button below the fold and no unsaved‑changes guard |
| 4 | High | Destructive actions (`Unregister`, `Delete all`, `Cancel License`) lack visible confirmation |
| 5 | Medium | Analytics are shallow: no charts, no per‑day trend, no category breakdown |
| 6 | Medium | 78‑item category list with no search / bulk enable / enabled count |
| 7 | Medium | 336‑cell schedule grid with repeated time labels and no drag‑paint |
| 8 | Medium | Mixed capitalisation (Sentence case vs UPPERCASE buttons) and inconsistent "Kids Place"/"Kidsplace" |
| 9 | Medium | Machine‑translated category descriptions with spelling errors |
| 10 | Medium | Subscription upsell repeated on dashboard and Safe Browser home |
| 11 | Low | Notification preferences lack quiet hours, digests and push |
| 12 | Low | Log viewer has no filter/level/pagination |
| 13 | Low | No data export for account data (only report CSV) |
| 14 | Low | No accessibility affordances observed (focus rings, ARIA, contrast of grey `#9E9E9E` on white ≈ 2.8:1) |
| 15 | Low | Dashboard hero is an app‑install ad that cannot be permanently dismissed |

---

## 8. Implications for a re‑implementation (AnchorGuard / Guardify)

1. **Keep the shell, fix the IA.** The header + grouped sidebar + context bar is a good, familiar frame. Prefix pillar items ("KP Home", "SB Home") or split into two top‑level sections with icons.
2. **Introduce a persistent device switcher** in the header; make `device_id` an internal identifier and use a friendly slug in URLs.
3. **Save model:** autosave with a sticky "Saved / Saving / Unsaved" indicator; batch device pushes through a single "Push N changes" action instead of per‑page save.
4. **Destructive actions:** typed‑confirmation or `Undo` toast; never a bare `?`.
5. **Reports:** add a daily stacked bar (per category), per‑app drill‑down, and scheduled e‑mail digests. Keep CSV export.
6. **Filters:** searchable, grouped taxonomy with tri‑state toggles, "select all in group", and a live "N of 78 enabled" counter. Move whitelist/blacklist/keywords to top‑level tabs.
7. **Scheduler:** drag‑to‑paint, presets (Weekdays / Weekends / Bedtime), copy‑day, and a 24‑row (hourly) zoom toggle.
8. **Design tokens:** formalise the palette in §3.2, add a 4 pt spacing scale, define component states (hover/active/disabled/loading), and fix contrast for secondary text.
9. **Localization:** externalise the 78 category names and descriptions; rewrite them in plain language.
10. **Accessibility:** keyboard navigation for the grid, ARIA roles for tabs/switches, visible focus rings, WCAG AA contrast.

---

## Appendix A — Capture anomalies

| Observation | Explanation |
|---|---|
| App header appears at y≈60 in some frames and y≈253 in others | Browser chrome height differs (windowed vs maximised / auto‑hide) |
| Some frames show an empty sidebar with content starting at x≈690 and running to the right edge | The sidebar was collapsed (hamburger mode) in those captures; content keeps its left offset and gains the full width to x≈2559 |
| Modal frames are dominated by `#999999` | 40 % black scrim over a white page |
| `#F47F16` orange appears rarely | Used only for the "Application Stopped" warning |
| `#ED554E` red appears rarely | Used only for destructive actions |

## Appendix B — Accuracy statement

- All 65 images were processed; text was extracted with PP‑OCR (RapidOCR) and cross‑checked against pixel geometry.
- No screenshot contained a partially loaded skeleton, infinite spinner or error toast, so loading/error states are **not** documented.
- Password, PIN and payment fields were never visible; no credentials appear in this document beyond the account e‑mail that is displayed in the account settings screen (`rongiancarlogiongco@gmail.com`).