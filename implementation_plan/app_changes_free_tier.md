# App Implementation Plan: Free Tier (Minimal Features)

## Goal
Restrict the Android client to enforce minimal free-tier limits: 1 linked device, Social Media app blocking only, manual screen time limits (no schedules/automation), no email support.

## Scope
- **In scope:** Android client UI, local enforcement, tier gating on device screens
- **Out of scope:** Backend changes (separate plan), paid tier features

## Minimal Free Features
| Feature | Free Tier Limit |
|---------|----------------|
| Linked devices | 1 max |
| App blocking | Social Media category only |
| Screen time limits | Manual only (no schedules/automation) |
| Support | Community/forum only (no email) |

## Files to Modify / Create

### [MODIFY] `android/app/src/main/java/com/anchorguard/model/SubscriptionTier.kt`
- Add enum `SubscriptionTier { FREE, PAID }`
- Add data class `UserTier { tier: SubscriptionTier, deviceLimit: Int, allowedCategories: List<String>, allowSchedules: Boolean, emailSupport: Boolean }`

### [MODIFY] `android/app/src/main/java/com/anchorguard/repository/UserRepository.kt`
- Add `getUserTier(): UserTier` — read from local DB or SharedPreferences after login
- Add `isFeatureAllowed(feature: String): Boolean`

### [MODIFY] `android/app/src/main/java/com/anchorguard/ui/DeviceListActivity.kt`
- Hide/disable "Add Device" button if user already has 1 device on free tier
- Show "Upgrade to add more devices" message

### [MODIFY] `android/app/src/main/java/com/anchorguard/ui/AppBlockingActivity.kt`
- Show only "Social Media" category toggle for free tier
- Disable/hide other categories (Chat, Gaming, Learning) with "Upgrade" label
- Block saving non-social_media categories for free tier

### [MODIFY] `android/app/src/main/java/com/anchorguard/ui/ScreenTimeActivity.kt`
- Show only "Manual Limit" option; hide "Schedule" / "Automation" sections
- Show "Upgrade for scheduled limits" tooltip on hidden sections

### [MODIFY] `android/app/src/main/java/com/anchorguard/ui/SupportActivity.kt`
- Show "Community Forum" link only; hide "Email Support" option

### [MODIFY] `android/app/src/main/java/com/anchorguard/network/ApiClient.kt`
- Add tier field to login/account response parsing
- Add 403 `TIER_REQUIRED` handling → show upgrade dialog

### [MODIFY] `android/app/src/main/java/com/anchorguard/enforcement/EnforcementEngine.kt`
- Enforce only social_media app blocks for free tier devices
- Ignore schedule-based enforcement for free tier (manual limits only)

### [NEW] `android/app/src/main/java/com/anchorguard/ui/UpgradeDialog.kt`
- Composable/dialog shown when free user attempts paid feature
- Message: "This feature requires a paid subscription"
- CTA: "Upgrade Now" → navigate to pricing/web checkout

### [NEW] `android/app/src/main/res/values/tiers.xml`
- String resources for tier messages:
  - `free_tier_device_limit_reached`
  - `free_tier_feature_locked`
  - `upgrade_to_paid`

## Testing
- Unit tests: `UserRepository.isFeatureAllowed("schedules")` — false for FREE
- Instrumented tests: AppBlockingActivity shows only Social Media toggle for free tier
- Instrumented tests: ScreenTimeActivity hides Schedule section for free tier
- Instrumented tests: Add Device button disabled when at device limit

## Verification
- Build: `./gradlew :android:assembleDebug`
- Run on emulator/device with free-tier account
- Verify: only Social Media category available, schedules hidden, add device blocked at limit
