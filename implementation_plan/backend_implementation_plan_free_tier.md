# Backend Implementation Plan: Free Tier (Minimal Features)

## Goal
Restrict the Spring Boot backend and web console to enforce minimal free-tier limits: 1 linked device, Social Media app blocking only, manual screen time limits (no schedules/automation), no email support.

## Scope
- **In scope:** Backend tier enforcement, subscription/gating middleware, web console feature gating, device limit validation
- **Out of scope:** Android client changes (separate app plan), paid tier features (schedules, geofence, multi-device, email support)

## Minimal Free Features
| Feature | Free Tier Limit |
|---------|----------------|
| Linked devices | 1 max |
| App blocking | Social Media category only |
| Screen time limits | Manual only (no schedules/automation) |
| Support | Community/forum only (no email) |

## Files to Modify / Create

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/subscription/SubscriptionTier.kt`
- Add enum `SubscriptionTier { FREE, PAID }`
- Add field to `User` or `Subscription` entity:
  ```kotlin
  var tier: SubscriptionTier = SubscriptionTier.FREE
  var deviceLimit: Int = 1
  var appBlockingCategories: List<String> = listOf("social_media")
  var allowSchedules: Boolean = false
  var emailSupport: Boolean = false
  ```

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/subscription/SubscriptionService.kt`
- Add business logic:
  - `canLinkDevice(userId: Long): Boolean` — check current device count < deviceLimit
  - `getAllowedBlockingCategories(userId: Long): List<String>` — return `["social_media"]` for free
  - `isScheduleAllowed(userId: Long): Boolean` — false for free
  - `getSupportChannel(userId: Long): String` — "community" for free

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/device/DeviceService.kt`
- Add device limit enforcement:
  - `linkDevice(userId: Long, deviceId: Long)` — reject if user already has 1 device on free tier
  - `getDeviceCount(userId: Long): Int`

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/device/DeviceController.kt`
- Add validation on `POST /api/devices` — return 403 if free tier user already has 1 device

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/policy/PolicyService.kt`
- Add category filtering:
  - `getAllowedCategories(userId: Long): List<String>` — free returns only `["social_media"]`
  - `savePolicy(deviceId, categories)` — reject non-social_media categories for free tier devices

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/policy/PolicyController.kt`
- Add endpoint `GET /api/devices/{id}/allowed-categories` — returns categories user can block

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/screentime/ScreenTimeService.kt`
- Restrict to manual limits only:
  - `setManualLimit(deviceId, minutes)` — allowed for free
  - `setSchedule(deviceId, schedule)` — reject for free tier (return 403)

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/screentime/ScreenTimeController.kt`
- Add tier check on schedule endpoints — return 403 `TIER_REQUIRED` for free users

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/support/SupportService.kt`
- Add `getSupportChannel(userId: Long): String` — returns "community" for free, "email" for paid

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/auth/AuthService.kt`
- Add `getUserTier(userId: Long): SubscriptionTier`
- Add `isFeatureAllowed(userId: Long, feature: String): Boolean` — gate schedules, multi-device, email

### [NEW] `backend/src/main/resources/db/migration/Vxx__add_free_tier_fields.sql`
- Flyway migration:
  ```sql
  ALTER TABLE users ADD COLUMN tier VARCHAR(20) NOT NULL DEFAULT 'FREE';
  ALTER TABLE users ADD COLUMN device_limit INT NOT NULL DEFAULT 1;
  ALTER TABLE users ADD COLUMN allow_schedules BOOLEAN NOT NULL DEFAULT FALSE;
  ALTER TABLE users ADD COLUMN email_support BOOLEAN NOT NULL DEFAULT FALSE;
  CREATE INDEX idx_users_tier ON users(tier);
  ```

### [NEW] `backend/src/main/kotlin/com/anchorguard/api/subscription/TierInterceptor.kt`
- HandlerInterceptor or filter to check tier on protected endpoints:
  - Block `/api/screentime/schedule/*` for FREE tier
  - Block device linking beyond limit for FREE tier
  - Block non-social_media categories for FREE tier

### [MODIFY] Web Console (`web-console/src/pages/Dashboard.tsx` or feature pages)
- Gate "Screen Time Schedules" — hide or disable for free tier (show "Upgrade" tooltip)
- Gate "App Categories" — show only "Social Media" toggle; hide/disable others
- Gate "Email Support" — hide email contact option; show "Community Forum" link
- Gate "Add Device" — hide/disable "Add Device" button if user already has 1 device
- Add "Free Tier" badge on dashboard header

### [MODIFY] Web Console (`web-console/src/api/endpoints.ts` or client.ts)
- Add tier field to user/account API response
- Add 403 handling with `TIER_REQUIRED` error code for upgrade prompts

## API Endpoints Summary

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/account/me` | User | Returns tier, deviceLimit, allowedCategories |
| GET | `/api/devices/{id}/allowed-categories` | Parent | Returns categories free user can block |
| POST | `/api/devices` | Parent | Rejects if free tier at device limit (403) |
| POST | `/api/screentime/schedule` | Parent | Rejects for FREE tier (403 TIER_REQUIRED) |
| GET | `/api/support/channel` | User | Returns "community" for free tier |

## Testing
- Unit tests: `SubscriptionService.canLinkDevice` — free tier at limit returns false
- Unit tests: `PolicyService.getAllowedCategories` — free returns only `["social_media"]`
- Integration tests: free tier user tries to add 2nd device → 403
- Integration tests: free tier user tries to save schedule → 403 TIER_REQUIRED
- Contract test: `/api/account/me` returns `tier: "FREE"` for free users

## Risks & Mitigations

| Risk | Mitigation |
|------|-----------|
| Free user bypasses category limit via direct API | Server-side validation in PolicyService, not just UI |
| Free user links 2nd device | DeviceService enforces limit before save |
| Free user accesses schedule endpoint | TierInterceptor returns 403 before reaching controller |
| Paid user downgraded to free retains extra devices | Allow existing devices but block new links until under limit |

## Verification
- `./gradlew :backend:test` — run backend tests
- `curl` new endpoints manually with free-tier auth token
- Web console: verify schedules hidden, only Social Media toggle visible, Add Device disabled at limit
