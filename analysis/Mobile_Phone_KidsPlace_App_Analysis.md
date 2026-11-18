# Kids Place — Android Application UI Analysis
## Child Device & Parent Mode (com.kiddoware.kidsplace)

**Analysis date:** 2026-11-18
**Evidence base:** 98 screenshots in `/screenshot/Mobile Phone/`
**Package:** `com.kiddoware.kidsplace` (Kiddoware Kids Place)
**Device profile:** 1080 × 2400 px Android phone (the backup screen lists Xiaomi models), captures taken 2026-11-18 between 10:17 and 12:48
**Method:** OCR transcription (RapidOCR / PP‑OCR) plus pixel‑level colour, geometry and component analysis. Every frame was processed; no live device was used.

> **Confidence note.** Text was recovered by OCR, so a few glyphs are uncertain (e.g. `PiN`, `Manaae Browser`, `Selectewallpaper`, `Strech`). Layout, colour and component geometry are measured from pixels and are reliable. No password or PIN was visible in cleartext beyond the digits the user typed in the PIN‑creation frame.

---

## 1. Executive summary

Kids Place for Android is the **launcher / parental‑control agent that runs on the child's device**, with a built‑in **parent mode** for on‑device administration. It is the counterpart to the web console analysed separately: the phone app holds the policy engine, the child‑facing launcher, and a full parent settings hub.

Key characteristics:

| Aspect | Observation |
|---|---|
| Platform | Native Android app, phone portrait only (1080 × 2400) |
| Two identities | **Child mode** (launcher with approved apps, wallpaper, timer) and **Parent mode / "Grown Up Mode"** (full settings, PIN‑gated) |
| Onboarding | 3 marketing slides → role selection → age → PIN → (optional) account link |
| Information architecture | Flat settings hub with ~12 top‑level sections and deep, single‑purpose sub‑screens |
| Visual language | Flat Material‑ish, single purple brand `#5C49E0`, white/`#FAFAFA` surfaces, one dark surface (Manage Apps) |
| Density | Medium: large tap targets, one primary action per screen, long scrolling lists |
| Distinctive features | Tasks & rewards, Stories, wallpapers, timer profiles, per‑app schedules, plugins |
| Maturity | Feature‑rich but inconsistent: mixed light/dark surfaces, duplicated settings paths, spelling errors, and several screens that dead‑end |

---

## 2. Device and build context

- **Resolution:** 1080 × 2400 (20:9), no gesture bar captured; the Android status bar shows a clock and network/battery glyphs.
- **Captured app flows:** the same device was used twice — 10:17–10:18 (cold start, onboarding) and 12:39–12:48 (full setup and settings tour).
- **Branding:** "Kids Place" (two words) on the splash and web; "KidsPlace" (one word) in the child‑mode top bar; "Kiddoware" for the company.
- **Account shown:** `rongiancarlogiongco@gmail.com` (Ron Gian Carlo Giongco) — used only to describe the account screens; the same account is used by the web console.
- **Devices referenced:** `Xiaomi 25099RP13G`, `Xiaomi M2102J20SG`.

---

## 3. Design system

### 3.1 Colour

| Token | Value | Usage |
|---|---|---|
| Brand purple | `#5C49E0` (92,73,224) | App bar, primary buttons, selected states, links |
| Brand purple (dark) | `#4032CE` (64,50,206) | Header gradient / pressed states |
| Deep purple | `#241C57` (36,28,87) | Dark surfaces, scrims, bottom bar |
| App background | `#FAFAFA` / `#FFFFFF` | Settings and list screens |
| Dark surface | `#323232` (50,50,50) | **Manage Apps** page only |
| Mid grey | `#CECECF` (206,206,207) | Inactive chips, dividers, time‑picker dials |
| Secondary text | `#757575` | Descriptions, helper copy |
| Success / allowed | `#00AD5B` (0,173,91) | "Unrestricted", allowed slots |
| Danger / blocked | `#FF3D00` (255,61,0) | Blocked schedule slots, destructive actions |
| Warning / reward | `#FCC466` (252,196,102) | Reward chips, streak elements |
| Wallpaper accent | `#FDE61E` (253,230,30) | Default kids wallpaper (yellow) |

The palette is **purple + neutral**, with green and orange reserved for state. The dark Manage Apps screen and the yellow wallpaper are the only large departures from the purple/white system.

### 3.2 Typography

System sans‑serif (Roboto‑like), measured at 1080 px width (≈ 2.75× density, so 40 px ≈ 14.5 dp):

| Role | Size (px) | ≈ dp/sp | Weight | Colour |
|---|---|---|---|---|
| Splash wordmark "Kids Place" | ~64 | 23 | 700 | white |
| Child‑mode bar title | ~52 | 19 | 700 | white |
| Screen title (e.g. "Settings") | ~54 | 20 | 600 | `#212121` |
| Section header (e.g. "Parental Controls") | ~48 | 17 | 600 | `#212121` |
| Body / list row | ~44 | 16 | 400 | `#212121` |
| Helper / caption | ~38 | 14 | 400 | `#757575` |
| Button label | ~44 | 16 | 600 | white / purple |
| Tab / chip | ~40 | 15 | 500 | `#616161` |

- Most screens use **sentence case**; buttons are **UPPERCASE** (`GET STARTED`, `CONTINUE`, `SET DEVICE TIME LIMITS`, `UPDATE DAILY TIME LIMITS`).
- Long helper paragraphs are used extensively — many settings screens read like documentation rather than UI.

### 3.3 Layout and spacing

- **Screen grid:** 1080 px wide, 40–48 px side margins (8–9 %).
- **Top bar:** purple, ~150–200 px tall (status bar + title), title left‑aligned at x≈150.
- **Bottom action bar:** 1 primary button, full‑width minus margins, ~110–125 px tall, sitting at y≈2050–2170.
- **List rows:** 120–160 px tall; section groups separated by 40–60 px.
- **Cards:** 16–20 px radius, 1 px `#E0E0E0` border, no shadow on light screens.
- **Dividers:** 1 px `#E6E6E6`, inset 0 (full‑bleed) in settings lists.
- **Safe areas:** status bar at y 0–80; content starts at y≈150; bottom actions end at y≈2170, leaving ~230 px for the system navigation area.

### 3.4 Core components

| Component | Description |
|---|---|
| **App bar** | Purple `#5C49E0`, 56 dp, white title, optional back arrow and trailing action ("Default User", "CHANGE MODE"). |
| **Section header** | 17 sp semibold, used to group settings ("Customize Content and Controls", "Premium Features", "Personal Settings"). |
| **Setting row** | Title (16 sp) + optional 2–4 line description (14 sp grey) + trailing toggle/chevron/value. |
| **Toggle switch** | Purple track when ON, grey when OFF, 56 × 32 px. |
| **Primary button** | Purple fill, white uppercase label, 8 px radius, full width or 360 px wide pill (splash). |
| **Secondary button** | White with purple 1 px border, purple label. |
| **Text button** | Purple uppercase, no background ("CONTACT SUPPORT", "PRIVACY POLICY"). |
| **Chip / segmented control** | Rounded rect, `#F0F0F0` fill, purple text when selected ("Sort", "Filter", "Auto Approve : Off"). |
| **Radio card** | Full‑width selectable card with title + description; purple border + check when selected. |
| **List with checkboxes** | App rows with a square checkbox on the right ("To allow apps, check this box!"). |
| **Time picker** | Two‑column hour/minute dial with AM/PM, purple selection, "COPY TO ALL DAYS" action. |
| **Week grid** | 7 columns × 24 rows (or 48 half‑hour rows), purple = allowed, orange‑red = blocked. |
| **Dialog** | White card, 24 px radius, 60 % black scrim, title + OK/CANCEL. |
| **Empty state** | Illustration + short copy ("No devices connected yet", "Create tasks for Kids"). |
| **Bottom sheet / action bar** | Sticky bottom CTA ("CREATE A NEW TASK", "DONE"). |
| **Snackbar / tip card** | "Kids Place Tips" overlay with "REMIND ME LATER" / "DON'T SHOW ME AGAIN". |

### 3.5 Iconography and illustration

- Outline icons in the settings lists (purple or grey), 48–56 px.
- Flat, colourful character illustrations for **Stories** (Vector the smart cat, Pel, Pebbles the curious bunny, Milo the little dino) and for wallpaper packs (Cars, Animals, Playground, Kids Place Classics).
- The app icon/logo is a purple rounded square with a white "Kids Place" mark.

---

## 4. Screen inventory

All 98 captures are 1080 × 2400. Rows are **grouped by flow**, not in capture order; the *Time* column is the device clock shown in the status bar.

| # | Time | Screen | Key content |
|---|---|---|---|
| 1–3 | 10:17 | Splash / onboarding slides | "Tools to Help Parents Manage a Child's Digital Experience", "A Full Parental Control Toolset at a Lower Cost", "An Android Parental Control App with Millions of Downloads" → **GET STARTED** |
| 4–5 | 10:17 / 12:39 | Role selection | "How will you use Kids Place?" → *This is my child's device* / *I'm the parent – manage remotely* |
| 6 | 10:18 | Parent‑mode prompt | "Have you installed Kids Place on your child's device?" → **NO / YES** |
| 7 | 10:18 | Remote info | "You can manage child device remotely with our remote solutions." → **CONTINUE** |
| 8 | 10:18 | Account link | "Kiddoware Account – Link or create Kiddoware account" → Google Sign In / Email |
| 9 | 12:40 | Device owner | "Who will be using this device?" → **CHILD ONLY / PARENT & CHILD / PARENT ONLY** |
| 10 | 12:40 | Age gate | "Your kid's age" → Under 3 / 4–8 / 9–12 / 13+ / I don't want to answer |
| 11 | 12:40 | PIN creation | "Create a PIN for parental controls" + 4‑digit entry + **CONFIRM PIN** |
| 12–13 | 12:41 | Stories intro | "Choose a character and let the adventures begin!" |
| 14 | 12:41 | Story playback | Story Playback Settings (Auto Play, Language) |
| 15 | 12:41 | Language picker | Device Default / English / Spanish / German / French |
| 16 | 12:41 | Parent gate | **Enter PIN** + "Forgot PIN? … /forgotpin" |
| 17 | 12:41 | Quick parent menu | Categories, Wallpaper, Select User, Timer, Usage Details, Enable Full‑Screen Mode, Refresh, Help |
| 18 | 12:42 | Child home | "HOME" + wallpaper + remaining time (23:59) |
| 19–20 | 12:42 | Wallpaper picker | Cars, Kids Place Classics |
| 21 | 12:42 | Select user | Default User |
| 22 | 12:42 | Kids Place (child mode) | Day of week, Show usage by, Sort by, Time used / Day limit / User |
| 23 | 12:42 | Settings (root) | License, Customize Content and Controls, Premium Features, Personal Settings |
| 24–37 | 12:42–12:43 | Manage Apps (dark) | App list, sort, filter, auto‑approve, categories, per‑app limits |
| 38–43 | 12:42–12:43 | App limit editor | Daily Time Limit, Block Schedule, COPY TO ALL DAYS |
| 44–47 | 12:43 | Internet access dialog | Internet On / Off / Custom Filtering |
| 48–52 | 12:43–12:44 | App list scrolling & dialogs | Add Category, Auto Approve, filters |
| 53–56 | 12:44 | Settings (cont.) | Safe Browser plugin, Video Player plugin, advanced security |
| 57–62 | 12:44 | Advanced security toggles | Block overlay, Play Store, Google App, Airplane mode, calls, Wi‑Fi, reporting, etc. |
| 63–66 | 12:44–12:45 | Tasks | Task list, create task, rewards, notifications |
| 67–70 | 12:45 | Create task | Task name, Reward, Value, Frequency, GIF picker |
| 71 | 12:45 | Task approval prompt | "Approve tasks from anywhere" → ENABLE |
| 72–73 | 12:45 | Wallpaper gallery | Stories / Playground / Animals / Cars / Classics |
| 74 | 12:45 | Wallpaper settings | Kids Select Wallpaper, Rotating Wallpapers |
| 75–76 | 12:46 | Kids Place Timer | Temporary Timer, lock options |
| 77–80 | 12:46 | Screen Time Controls | Device usage overview, device limits, individual app limits, Timer Settings |
| 81–82 | 12:46 | Timer Profiles | Default profile, new profile, rename |
| 83 | 12:46 | Kids Place usage | RESET ALL, Show usage by, Sort by |
| 84–85 | 12:47 | Select / add user | Name, User PIN (optional) |
| 86–89 | 12:47 | Parental Controls settings | Advanced Timer, Background mode, Icon in block message, Boot start, Tamper proofing, App title, Keep alive, Brute‑force PIN, Blocking frequency |
| 90–91 | 12:47 | User Interface Settings | Default category view, title colour, icon layout, icon size, font size, stretching, hide settings, Grown Up Mode |
| 92 | 12:47 | Backup and Restore | Account, device list, CREATE NEW BACKUP |
| 93 | 12:47 | Manage Account | Linked e‑mail, DELETE ACCOUNT, UNLINK |
| 94–95 | 12:48 | Feedback survey | "How was your first experience?", improvement reasons |
| 96 | 12:48 | Kids Place Tips | "Start on device reboot" recommendation |
| 97–98 | 12:48 | Home (background) | Wallpaper + bottom bar |

---

## 5. Onboarding and setup flow

### 5.1 Splash / marketing carousel

Three full‑bleed purple slides, each with a headline, a short paragraph, a flat illustration and a fixed white **GET STARTED** pill.

```
┌─────────────────────────────┐
│ ▣ Kids Place                │  purple #5C49E0, status bar white
│                             │
│   Tools to Help Parents     │
│   Manage a Child's Digital  │
│   Experience..              │
│                             │
│        [ illustration ]     │  colourful flat art, y 850–1450
│                             │
│                             │
│      ( GET STARTED )        │  white pill 360×125, y≈1985–2110
│                             │
│   By Kiddoware              │
└─────────────────────────────┘
```

Slides observed:

1. **"Tools to Help Parents Manage a Child's Digital Experience.."**
2. **"A Full Parental Control Toolset at a Lower Cost"** — bullets: *View App Usage · Manage Screen Time · Set Content and App Restrictions · Manage Access*
3. **"An Android Parental Control App with Millions of Downloads"**

No page indicator dots were detected; the carousel advances by swipe.

### 5.2 Role selection — "How will you use Kids Place?"

Two large selectable cards:

| Card | Title | Sub‑title | CTA |
|---|---|---|---|
| 1 | This is my child's device | Set up parental controls on this phone or tablet | **Set up this device** |
| 2 | I'm the parent – manage remotely | Monitor and control from your own device | **Set up remote control** |

Card 1 carries a small **"Most common"** badge. The first card is the primary (purple) action; the second is secondary.

### 5.3 Parent‑mode probe

If the parent path is chosen, the app asks **"Have you installed Kids Place on your child's device?"** with a two‑button choice (**NO** / **YES**) and a small Kids Place logo. Selecting *Yes* leads to a remote‑management explanation ("You can manage child device remotely with our remote solutions.") and **CONTINUE**.

### 5.4 Device owner — "Who will be using this device?"

Three mutually exclusive radio cards:

- **CHILD ONLY**
- **PARENT & CHILD**
- **PARENT ONLY**

A **SKIP** text button sits at the bottom. This is a licence/entitlement switch that determines whether the launcher is installed as the default home.

### 5.5 Age gate — "Your kid's age"

Single‑choice list:

`Under 3 years` · `4 to 8 years` · `9 to 12 years` · `13 and older` · `I don't want to answer`

with an **OK** button (bottom‑right). The selection presumably tunes default content rules.

### 5.6 Kiddoware account (optional)

```
┌─────────────────────────────┐
│ Kiddoware Account           │
│ Link or create Kiddoware    │
│ account                     │
│ Use the same account across │
│ all Kiddoware Apps, and     │
│ avail additional services,  │
│ including pin reset         │
│                             │
│  [ Google Sign In        ]  │
│  [ Continue with your     ] │
│  [ Email Address         ]  │
│                             │
│ PRIVACY POLICY              │
└─────────────────────────────┘
```

The value proposition is explicitly **PIN reset** — the account is the recovery path for a locked device.

### 5.7 PIN creation

```
┌─────────────────────────────┐
│ Create a PIN for parental   │
│ controls                    │
│ This enables you to modify  │
│ parental control settings,  │
│ approve screen-time apps,   │
│ and much more.              │
│                             │
│  Please enter your 4 digit  │
│  PiN:                       │
│        [ • • • • ]          │  numeric keypad implied
│                             │
│        [ CONFIRM PIN ]      │
│ PRIVACY POLICY              │
└─────────────────────────────┘
```

- Exactly 4 digits, no visible keypad in the capture (system numeric keyboard).
- The typed digits are **not masked** in the capture — a should‑be‑masked field.
- This PIN gates every parent action and can be reset via the linked account.

---

## 6. Child mode (launcher)

### 6.1 Home screen

```
┌─────────────────────────────┐
│ KidsPlace            23:59  │  purple/plum top bar, remaining time
│                             │
│            HOME             │
│                             │
│      [ full-bleed wallpaper ]│  yellow #FDE61E default,
│                             │  gradient to deep teal at bottom
│                             │
│    ▢        ▢        ▢      │  approved app icons near bottom
│                             │
│  (dark blue gradient dock)  │
└─────────────────────────────┘
```

- The top bar shows the **app name** (customisable via *Change Application Title*) and the **remaining screen time** (`23:59`).
- The wallpaper is full‑bleed; the default is bright yellow with a teal‑to‑navy gradient at the bottom.
- Approved apps appear as icons over the lower third; the launcher is otherwise empty (no widgets, no search, no page indicator).
- "HOME" is a persistent label directly under the top bar.

### 6.2 Child‑mode usage strip

A secondary child‑mode view shows:

| Field | Value |
|---|---|
| Day of week | Wednesday |
| Show usage by | App |
| Sort by | Time usage |
| Time used | 00:00:55 |
| Day limit | no limits |
| User | Default User |

This is the child's own usage summary — notably it exposes the **day limit** and **user** to the child.

### 6.3 Exit / parent gate

- **Enter PIN** screen with a numeric entry field and the recovery link:
  `Forgot PIN? Go here to retrieve: https://app.kiddoware.com/forgotpin`
- The parent gate is the only way into the parent settings from child mode.

---

## 7. Parent mode

### 7.1 Settings root

```
┌─────────────────────────────┐
│ ‹ Settings                  │
│ Kiddoware Suite License     │
│  You have a Kiddoware Suite │
│  Subscription active        │
│                             │
│ CUSTOMIZE CONTENT AND       │
│ CONTROLS                    │
│  Manage Apps            ›   │
│  Manage Browser         ›   │
│  Manage Videos          ›   │
│  Parental Controls      ›   │
│  Tasks                  ›   │
│  Kids Place Remote Ctrl ›   │
│                             │
│ PREMIUM FEATURES            │
│  Wallpaper              ›   │
│  Timer                  ›   │
│  Screen Time Controls   ›   │
│  Manage Users           ›   │
│  Advanced Security      ›   │
│  User Interface         ›   │
│  Backup and Restore     ›   │
│  Manage Account         ›   │
│                             │
│ PERSONAL SETTINGS           │
│  Privacy / Security /       │
│  Contact Us / License       │
│  Status / Plugins /         │
│  Refer friends / Exit       │
└─────────────────────────────┘
```

- Three groups, ~18 rows, each with a purple outline icon, 16 sp label and chevron.
- The licence banner at the top doubles as a status indicator.
- Two rows open **dialogs** rather than pages (Manage Browser → install Safe Browser plugin; Manage Videos → install Video Player plugin).

### 7.2 Manage Apps (dark screen)

```
┌─────────────────────────────┐
│ ‹ Manage Apps   Default User│  purple bar
│           HOME              │
│ [ Search for an application]│
│ (Sort) (Filter) AutoApprove:Off
│ ─────────────────────────── │
│ ▢ AMdroid                   │  dark #323232 surface
│ ▢ Authenticator             │
│ ▢ HOLY BIBLE      Bible     │
│ ▢ Bing       Internet browser│
│ ▢ Business Suite            │
│ ▢ CCF                       │
│ ▢ CIMB       CIMB Bank PH   │
│ ▢ Calculator                │
│ …                           │
└─────────────────────────────┘
```

- **Only dark surface in the app.** Header stays purple; body is `#323232` with white text.
- Each row: checkbox, app icon, name, and an optional **category** in grey.
- Toolbar chips: **Sort**, **Filter**, **Auto Approve : Off**.
- Helper banners appear inline: *"To allow apps, check this box!"*, *"You can set time limit for any app from here!"*, *"Tap this button to manage users!"* — onboarding hints injected into the list.
- **Sort** dialog: Alphabetical / Recently Installed / Recently Used.
- **Filter by**: All Apps / Only Selected / Unselected.
- **Auto Approve** dialog: "Automatically approve newly installed apps. Recommended for devices that are not shared with kids." → **Enable / Disable**.
- **Add Category** dialog: Name + Color + CANCEL/OK.

### 7.3 Per‑app time limit & block schedule

```
┌─────────────────────────────┐
│ ‹ AMdroid                   │
│   Default profile           │
│                             │
│ Daily Time Limit            │
│ Limit usage time of amdroid │
│ per day of the week.        │
│                             │
│  [ Unrestricted ▾ ]         │
│  [ UPDATE DAILY TIME LIMITS]│
│                             │
│ Block Schedule              │
│ Select time slots when      │
│ amdroid is not allowed to   │
│ be used.                    │
│                             │
│  [Block all timeslots]      │
│  [Clear all timeslots]      │
│                             │
│   12 AM   1 AM … 11 PM      │
│   ▢       ▢       ▢         │
│                             │
│  [COPY TO ALL DAYS]    [OK] │
└─────────────────────────────┘
```

- **Daily Time Limit** is a per‑day selector (Unrestricted / custom minutes) with an explicit *Update* action.
- **Block Schedule** is a 24‑hour grid; blocked cells are `#FF3D00` orange‑red, allowed cells white.
- **COPY TO ALL DAYS** is the bulk action (essential for 7 × 24 cells).
- **Internet Access** dialog per app: *Internet On* / *Internet Off* / *Custom Filtering* (**MANAGE**).

### 7.4 Screen Time Controls

```
┌─────────────────────────────┐
│ ‹ Screen Time Controls      │
│   Default profile           │
│                             │
│ Device Usage Overview       │
│        00:01   TODAY        │  circular/linear usage indicator
│                             │
│ Set device-wide time limits │
│ [ SET DEVICE TIME LIMITS ]  │
│                             │
│ Individual App Limits       │
│ Set time limits for specific│
│ apps and categories         │
│ ( APPROVED APPS | CATEGORIES )│
│  AMdroid   Bing   HOLY      │
│  BIBLE     GoTyme PH …      │
│                             │
│ Timer Settings          [OK]│
└─────────────────────────────┘
```

- Two levels: **device‑wide** and **per‑app / per‑category**.
- **Timer Settings** dialog:
  - *Advanced Timer* — "Enable to set up Screen Time Controls. Permanent schedule based settings for apps/categories for each day of the week."
  - *Display Timer Popup* — "Shows remaining time as overlay on approved apps."
  - *Display App Usage* — "Shows app usage time on home screen menu."

### 7.5 Timer and profiles

**Kids Place Timer**
- *Temporary Timer* — "Settings for this session".
- Lock options: **No time lock** / **Specify time in minutes** / **Clear temporary timer on exit**.
- Apply button (bottom).

**Timer Profiles**
- Profile list: **Default profile → Default User**.
- **New time profile** dialog (Time profile name).
- Rename inline (`tezt` typed).
- A profile switcher appears in the Screen Time Controls title bar (`tezt`).

### 7.6 Tasks & rewards

```
┌─────────────────────────────┐
│ ‹ Tasks          Default User│
│                             │
│         [ illustration ]    │
│  Create tasks for Kids,     │
│  and reward them for good   │
│  behavior.                  │
│  Swipe here to know more.   │
│                             │
│  ┌───────────────────────┐  │
│  │ Clean The Room        │  │
│  │ Reward: Extra TV Time │  │
│  │ 10 mins        6:30 PM│  │
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │ Put away toys         │  │
│  │ Reward: Pocket Money  │  │
│  └───────────────────────┘  │
│                             │
│ [ CREATE A NEW TASK ]       │
└─────────────────────────────┘
```

**Create a new task** form:

| Field | Options |
|---|---|
| Task name | Free text + **Frequent phrases** picker (Brush teeth, Wash hands, Make my bed, Put away toys, Clean my room, Tidy my desk, Feed the pet, Water the plants, Set the table, Help with groceries, Homework, Read a book, Pack my school bag) |
| Reward | Text + GIF picker |
| Value | e.g. `10 mins` |
| Frequency | Daily / Weekly / Once |
| Actions | **DONE** |

**Approval banner:** "Approve tasks from anywhere. Turn on Remote Control to approve your kids tasks from your phone" → **ENABLE**.

**Task notifications settings:**

| Setting | Behaviour |
|---|---|
| Pending Tasks on Device | Remind kids at 4 PM and 7 PM |
| Pending Tasks via Notification | Email about pending tasks at 4 PM |
| Kids home screen → Pending tasks banner | After 4 PM, show unfinished tasks and the streak at the top of the kids home screen |

### 7.7 Wallpapers and Stories

- **Select wallpaper** gallery with packs: **Stories**, **Playground**, **Animals**, **Cars**, **Kids Place Classics**.
- Detail view: "Select a wallpaper to be displayed in kids mode".
- **Wallpaper settings**:
  - *Kids Select Wallpaper* — allow kids to change it.
  - *Rotating Wallpapers* — Minute / Hourly / Daily / Weekly / Never.
- **Stories**: character selection (Vector the smart cat, Pel, Pebbles the curious bunny, Milo the little dino), a story player, and *Story Playback Settings* (Auto Play, Language).

### 7.8 Users

- **Select User** → `Default User` (only one profile in the capture).
- **Add user** dialog: `Name` + `User Pin (optional)` + OK/CANCEL.
- "Allow kids to switch users" setting, with optional per‑user PIN.
- Manage Apps has a **user selector** in the title bar; the "Tap this button to manage users!" hint points at it.

### 7.9 Parental Controls (advanced)

| Setting | Description (verbatim) |
|---|---|
| Enable Advanced Timer | "Advanced Timer to control how long kids can use device or a specific app/category and during what time of the day. Creates time restriction schedule for each day of the week." |
| Run In Background Mode | "App will run in background mode. Kids Place home screen will not be shown. Note: This setting is recommended when you want to use the app for older kids or employees." |
| Kids Place Icon In Block Message | "Show Kids Place icon and action buttons in block message." |
| Always Start on Device Boot. | "App will always start when device is powered on. Note: This settings is different from standard 'Start On Device Reboot' option…" |
| Enable Tamper Proofing | "Requires activating Device Administrator permission. Provides additional security by disabling uninstall option from app manager…" |
| Change Application Title | "Customize the application name displayed on main screen top bar. Ideal for personalization or removing Kids Place name for older kids." |
| Keep Alive Kids Place | "If Android OS kills Kids Place to free up memory, start the app again…" |
| Enable Brute Force PiN Protection | "Disable PIN prompt for 2 minutes upon 3 failed PIN entries." |
| Blocking Frequency in MilliSeconds | "Lower value improves blocking speed." (value `500`) |

This screen is the clearest evidence that the product is designed for **tech‑savvy administrators**, not typical parents: it exposes Device Admin, battery optimisation, watchdog and polling intervals.

### 7.10 User Interface Settings

| Setting | Options / value |
|---|---|
| Default Category View | Uncategorized / Categorized / Last Used |
| App title text color | colour swatches |
| App Icon Layout | Comfortable (wider icons) / Compact (more productive UI) |
| App Icon size | `64` |
| App Font Size | `14` |
| Icon Stretching | stretch icons to fit border |
| Hide setting button | hide settings button in full‑screen mode |
| Show Grown Up Mode on start and exit | Grown Up Mode |

### 7.11 Backup and Restore

- Account header: `Ron Gian Carlo Giongco` / `rongiancarlogiongco@gmail.com`.
- Device list to restore from: `Xiaomi 25099RP13G`, `Xiaomi M2102J20SG`.
- **CREATE NEW BACKUP** (bottom).
- Branded purple screen with a light‑purple pattern.

### 7.12 Manage Account

- `rongiancarlogiongco@gmail.com is currently linked to KidsPlace.`
- **DELETE ACCOUNT** (bottom) · **UNLINK** (secondary).

### 7.13 Plugins

Two plugin install prompts (dialogs over the Settings list):

1. **Manage Browser** — "Managing websites and parental web controls requires the Kiddoware Kids Place Safe Browser plugin. It is not installed on this device. Do you want to open the app store to install it now?" → **CANCEL / INSTALL**
2. **Manage Videos** — "Managing videos and safe YouTube viewing requires the Kiddoware Kids Place Video Player plugin…" → **CANCEL / INSTALL**

This is a **modular architecture**: the core app is a shell; web filtering and video filtering are separate APKs.

### 7.14 Feedback and tips

**Feedback survey** (two steps):
1. "How was your first experience? Your feedback helps us make Kids Place better for your family." → **IT WENT AS EXPECTED** / **IT CAN IMPROVE**
2. "What can we improve? Select everything that applies." → *Parental controls didn't work*, *A feature I needed was missing*, *It wasn't what I expected*, *I couldn't add the apps I wanted*, *I ran into a bug or error* → **CONTACT SUPPORT**

**Kids Place Tips** overlay:
> "Please go to Kids Place settings > Parental Controls and check 'Start on device reboot' setting. This will make sure app is restarted if your kid reboot the device, from Kids Place, to get out of parental control and exit Kids Place."
> **REMIND ME LATER** / **DON'T SHOW ME AGAIN**

### 7.15 Advanced security toggles (Settings → Advanced Security Settings)

| Setting | Description |
|---|---|
| Close Kids Place Block Overlay | Allow closing blocking overlay after a while |
| Block Play Store | Block apps from accessing marketplace |
| Block Google App | Disable Google Assistant, search and other quick actions |
| Airplane mode | Disable all wireless connections |
| Allow phone calls | Display incoming call alerts |
| Keep home screen on | Disables screen timeout when checked |
| Start on device reboot | Start Kids Place on device reboot |
| Allow apps switcher | Allow switching apps from task manager / recent apps |
| Whitelist | Whitelist apps/processes not visible in launcher |
| Enable Reporting | Collect app usage data (links to `kidsplace.kiddoware.com`) |
| Display Status bar | Show the status bar |
| Lock Notification Bar | Locks the notification bar |
| Enable Higher Reliability | Requires ignore battery optimisation permission |
| Allow Internet Connection | Affects Wi‑Fi only, not data |
| Wifi Toggle | Allow kids to turn Wi‑Fi on/off |
| Always Start WiFi | Starts Wi‑Fi when Kids Place home screen is on |
| Web content filtering | Filter websites including in browsers |
| Block Unapproved Apps Started From… | May block some approved apps |
| Allow kids to switch users | With optional per‑user PIN |
| Lock device volume control | — |
| Lock device brightness control | Change volume |
| Change volume | — |

---

## 8. Navigation model

```
Splash carousel ──► Role ──► Parent probe ──► Device owner ──► Age ──► PIN ──► Account
                                                                          │
                                                                          ▼
                                                          Child mode (launcher)
Child launcher ──(PIN / Grown Up Mode)──► Parent gate ──► Settings hub
                                                  ├── Manage Apps ──► App ──► Daily limit
                                                  │                    └──► Block schedule
                                                  ├── Manage Browser  (plugin dialog)
                                                  ├── Manage Videos   (plugin dialog)
                                                  ├── Parental Controls
                                                  ├── Tasks ──► Create task ──► Reward
                                                  ├── Remote Control
                                                  ├── Wallpaper ──► Gallery ──► Settings
                                                  ├── Timer ──► Temporary timer
                                                  │         └──► Profiles
                                                  ├── Screen Time Controls ──► App limits
                                                  ├── Manage Users
                                                  ├── Advanced Security
                                                  ├── User Interface
                                                  ├── Backup and Restore
                                                  ├── Manage Account
                                                  └── Personal settings
```

**Characteristics**

- Every parent screen is a **single‑purpose page with one primary action**; there are no multi‑pane layouts.
- Navigation is exclusively via the **back arrow**; there is no hamburger, no bottom navigation, no breadcrumb.
- The child launcher has **no navigation UI at all** — only app icons.
- No deep link from the web console into a specific phone screen was observed; the phone is a separate surface.

---

## 9. Cross‑cutting patterns

| Pattern | Implementation |
|---|---|
| **Parent gate** | 4‑digit PIN on every entry to parent mode; brute‑force lockout after 3 attempts (2 min) |
| **PIN recovery** | Linked Kiddoware account, or `https://app.kiddoware.com/forgotpin` |
| **Profiles** | Timer profiles and users are separate concepts but both appear in title bars; easy to confuse |
| **Save semantics** | Explicit *Update* / *OK* / *Apply* buttons per screen; no autosave |
| **Destructive actions** | Red text buttons; no confirmation dialog captured |
| **Empty states** | Illustration + copy + single CTA (Tasks, Safe Browser devices) |
| **Onboarding hints** | Contextual purple banners inside lists (Manage Apps) and overlays (Tips) |
| **Theming** | Mostly light; **Manage Apps is dark**; wallpapers can be light or dark |
| **Localisation** | 5 languages for Stories (Device Default, English, Spanish, German, French); UI copy itself is English‑only in the capture |
| **Accessibility** | Large touch targets; but text over wallpaper and grey‑on‑white helper copy are low contrast |

---

## 10. Implied data model

```
Account {
  id, email, linked_at, pin_reset_token,
  suite_subscription { active, plan }
}

Device {
  id, model (25099RP13G / M2102J20SG), manufacturer (Xiaomi),
  owner_mode: CHILD_ONLY | PARENT_AND_CHILD | PARENT_ONLY,
  child_age_band: UNDER_3 | 4_8 | 9_12 | 13_PLUS | UNSPECIFIED,
  app_title, wallpaper { pack, item, rotation },
  ui { category_view, icon_size, font_size, icon_layout, ... }
}

Pin { hash, attempts, locked_until }

User {
  id, name, pin?, tasks[], time_profile_id
}

TimeProfile {
  id, name, per_day_limits[7], block_slots[7][48], device_wide: bool
}

AppPolicy {
  package, category?, selected: bool,
  daily_limit_minutes[7] | UNRESTRICTED,
  block_slots[7][48],
  internet: ON | OFF | CUSTOM
}

Task {
  id, user_id, name, reward { label, gif? }, value, frequency,
  status: PENDING | APPROVED | DONE, streak
}

Communication / Usage {
  app, seconds_used, day
}
```

---

## 11. UX / UI issues

| # | Severity | Issue |
|---|---|---|
| 1 | High | **PIN is displayed in cleartext** while typing (both create and enter screens) |
| 2 | High | No visible confirmation for destructive actions (Delete Account, Unlink, Reset All) |
| 3 | High | **Mixed themes** — Manage Apps is dark while every other screen is light; on a dark‑wallpaper device this is jarring |
| 4 | High | Duplicate settings paths: *Advanced Timer* appears in Parental Controls, Timer Settings and Screen Time Controls |
| 5 | Medium | Long paragraphs of helper text on settings rows (5–7 lines) hurt scannability |
| 6 | Medium | Timer **profiles** and **users** are separate but both shown in title bars — confusing mental model |
| 7 | Medium | 7 × 24/48 schedule grids are heavy; only *Copy to all days* mitigates, no presets (bedtime, school hours) |
| 8 | Medium | Spelling/grammar errors: "Strech", "PiN", "Manaae Browser", "Selectewallpaper", "WiFl", "Settings is different" |
| 9 | Medium | Onboarding asks for age but the effect is invisible; no explanation of why it matters |
| 10 | Medium | Account link is optional but PIN recovery depends on it — the risk is not stated clearly |
| 11 | Medium | Child‑mode home exposes *Day limit* and *User* to the child |
| 12 | Low | No page indicator on the 3‑slide onboarding carousel |
| 13 | Low | "SKIP" is visually equal to the primary radio options on the device‑owner screen |
| 14 | Low | Grey `#757575` helper text on white ≈ 4.6:1 (borderline for 14 sp) |
| 15 | Low | Inconsistent naming: "Kids Place" vs "KidsPlace" vs "Kidsplace" |
| 16 | Low | Feedback survey appears immediately after setup, before the parent has used the product |

---

## 12. Recommendations for a re‑implementation (AnchorGuard / Guardify)

1. **One theme.** Pick light (or system‑driven) and apply it everywhere; reserve dark for the app allow‑list if contrast testing supports it.
2. **Mask the PIN** and add a numeric keypad with haptic feedback; show a lockout countdown after 3 failures.
3. **Consolidate time rules** into a single model: *device → profile → app*, with one editor reused at every level. Remove the duplicate "Advanced Timer" entry points.
4. **Presets for schedules**: School hours, Bedtime, Homework, Weekends — one tap applies a 7‑day block.
5. **Rewrite settings copy** to one line + optional "Learn more"; move Device Admin / battery / polling details into an "Advanced (for admins)" collapsed section.
6. **Merge users and profiles** or clearly separate "Who" (users) from "When" (time profiles) in the UI.
7. **Task redesign**: show streak, approval state and reward history; replace the GIF picker with a curated sticker set (GIFs need network + moderation).
8. **Child‑mode privacy**: hide day limits and profile names; show only remaining time.
9. **Accessibility**: 48 dp minimum targets, WCAG AA contrast, content descriptions for icon‑only controls, and support for display size/font scale.
10. **Instrumentation**: the app already reports to `kidsplace.kiddoware.com`; define a product‑analytics taxonomy so the web console can show the same numbers the phone collects.

---

## Appendix A — Capture anomalies

| Observation | Explanation |
|---|---|
| Manage Apps body is `#323232` | Only dark‑themed screen; header stays purple |
| Dialogs darken the whole screen | 60 % black scrim (`#666666` measured) |
| Some screens show typed PIN digits | Parent typed during capture; the system numeric keypad is not visible in the frames |
| `HOME` label under the title bar on several screens | Likely a tab/section label; no sibling tabs were captured |
| `23:59` / `23:58` in the child top bar | Remaining screen time, not a clock |
| App icon labels on the child home screen were not OCR‑readable | Icons sit over a high‑contrast wallpaper; labels are small |

## Appendix B — Accuracy statement

- All 98 images were processed with PP‑OCR (RapidOCR); text was cross‑checked against pixel geometry and colour sampling.
- No loading skeleton, crash dialog or permission rationale screen was captured, so those states are **not** documented.
- The device's Android version, DPI bucket and app version were not visible in the captures.
- PIN digits entered during the capture are deliberately not reproduced in this document; the PIN screens are referenced for their UI behaviour only.