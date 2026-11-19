# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: dashboard.e2e.spec.ts >> AnchorGuard Parent Dashboard E2E >> sidebar navigation contains all required items
- Location: tests/dashboard.e2e.spec.ts:72:3

# Error details

```
Error: page.evaluate: Target page, context or browser has been closed
```

```
Error: write EPIPE
```

# Test source

```ts
  1   | import { test, expect } from '@playwright/test'
  2   | 
  3   | test.use({ browserName: 'chromium', headless: true })
  4   | 
  5   | const BASE_URL = 'https://anchorguard.tail78464.ts.net'
  6   | 
  7   | async function loginViaBrowser(page: any) {
  8   |   // Navigate to login page to establish same-origin context
  9   |   await page.goto(`${BASE_URL}/login`, { waitUntil: 'commit', timeout: 20000 })
  10  |   await expect(page).toHaveURL(/.*\/login/, { timeout: 10000 })
  11  | 
  12  |   // Set sessionStorage via browser fetch (same-origin, no CORS)
> 13  |   await page.evaluate(async () => {
      |   ^ Error: write EPIPE
  14  |     const res = await fetch('/api/v1/auth/login', {
  15  |       method: 'POST',
  16  |       headers: { 'Content-Type': 'application/json' },
  17  |       body: JSON.stringify({ email: 'parent@anchorguard.com', password: 'TestParent123!' }),
  18  |     })
  19  |     if (!res.ok) throw new Error(`Login failed: ${res.status}`)
  20  |     const tokens = await res.json()
  21  |     sessionStorage.setItem('anchorguard.session.tokens', JSON.stringify(tokens))
  22  |   })
  23  | 
  24  |   // Full page navigation to dashboard - React will remount and read sessionStorage
  25  |   await page.goto(`${BASE_URL}/dashboard`, { waitUntil: 'load', timeout: 30000 })
  26  |   await expect(page).toHaveURL(/.*\/dashboard/, { timeout: 10000 })
  27  | 
  28  |   // Wait for dashboard content
  29  |   await expect(page.locator('h1')).toContainText('Dashboard', { timeout: 15000 })
  30  | }
  31  | 
  32  | test.describe('AnchorGuard Parent Dashboard E2E', () => {
  33  |   test('unauthenticated access redirects to login', async ({ page }) => {
  34  |     await page.goto(`${BASE_URL}/dashboard`, { waitUntil: 'commit', timeout: 20000 })
  35  |     await expect(page).toHaveURL(/.*\/login/, { timeout: 10000 })
  36  |   })
  37  | 
  38  |   test('login redirects to dashboard and renders all dashboard sections', async ({ page }) => {
  39  |     const errors: string[] = []
  40  |     page.on('console', (msg: any) => {
  41  |       if (msg.type() === 'error') errors.push(msg.text())
  42  |     })
  43  | 
  44  |     await loginViaBrowser(page)
  45  | 
  46  |     // Stat cards row
  47  |     await expect(page.locator('text=Active Devices')).toBeVisible({ timeout: 10000 })
  48  |     await expect(page.locator('text=Today\'s Screen Time')).toBeVisible()
  49  |     await expect(page.locator('text=Blocked Attempts')).toBeVisible()
  50  |     await expect(page.locator('text=Current Status')).toBeVisible()
  51  |     await expect(page.locator('text=Secured')).toBeVisible()
  52  | 
  53  |     // App Blocking & Controls - exact text match avoids activity log entries like "TikTok (Social Apps)"
  54  |     await expect(page.getByText('Social Apps', { exact: true })).toBeVisible()
  55  |     await expect(page.getByText('Chat', { exact: true })).toBeVisible()
  56  |     await expect(page.getByText('Gaming', { exact: true })).toBeVisible()
  57  |     await expect(page.getByText('Learning', { exact: true })).toBeVisible()
  58  | 
  59  |     // Charts
  60  |     await expect(page.locator('text=Screen Time & Limits')).toBeVisible()
  61  |     await expect(page.locator('text=App Usage')).toBeVisible()
  62  | 
  63  |     // Activity Log
  64  |     await expect(page.locator('text=Real-time Activity Log')).toBeVisible()
  65  |     await expect(page.locator('text=Live')).toBeVisible()
  66  | 
  67  |     if (errors.length > 0) {
  68  |       console.log('Console errors:', errors.slice(0, 5))
  69  |     }
  70  |   })
  71  | 
  72  |   test('sidebar navigation contains all required items', async ({ page }) => {
  73  |     await loginViaBrowser(page)
  74  | 
  75  |     await expect(page.getByRole('link', { name: 'Dashboard' }).first()).toBeVisible()
  76  |     await expect(page.getByRole('link', { name: 'Devices' }).first()).toBeVisible()
  77  |     await expect(page.getByRole('link', { name: 'App Blocker' }).first()).toBeVisible()
  78  |     await expect(page.getByRole('link', { name: 'Screen Time' }).first()).toBeVisible()
  79  |     await expect(page.getByRole('link', { name: 'Location' }).first()).toBeVisible()
  80  |     await expect(page.getByRole('link', { name: 'Reports' }).first()).toBeVisible()
  81  |     await expect(page.getByRole('link', { name: 'Settings' }).first()).toBeVisible()
  82  |     await expect(page.locator('label:has-text("Linked device")')).toBeVisible()
  83  |   })
  84  | 
  85  |   test('sidebar navigation links do not redirect to home', async ({ page }) => {
  86  |     await loginViaBrowser(page)
  87  | 
  88  |     const navLinks = [
  89  |       { name: 'Dashboard', expected: /.*\/dashboard$/ },
  90  |       { name: 'Devices', expected: /.*\/dashboard\/devices$/ },
  91  |       { name: 'Reports', expected: /.*\/dashboard\/reports$/ },
  92  |       { name: 'Settings', expected: /.*\/dashboard\/account$/ },
  93  |     ]
  94  | 
  95  |     for (const { name, expected } of navLinks) {
  96  |       await page.getByRole('link', { name }).first().click()
  97  |       await expect(page).toHaveURL(expected, { timeout: 10000 })
  98  |       // Ensure we did NOT redirect to home/login
  99  |       await expect(page).not.toHaveURL(/.*\/\?.*/, { timeout: 5000 })
  100 |       await expect(page).not.toHaveURL(/.*\/login/, { timeout: 5000 })
  101 |     }
  102 |   })
  103 | 
  104 |   test('device status empty state renders when no devices linked', async ({ page }) => {
  105 |     await loginViaBrowser(page)
  106 | 
  107 |     const emptyState = await page.locator('text=No devices linked').count() > 0
  108 |     expect(emptyState).toBe(true)
  109 | 
  110 |     // Charts render with progress bars
  111 |     await expect(page.locator('text=Screen Time & Limits')).toBeVisible()
  112 |     await expect(page.locator('text=App Usage')).toBeVisible()
  113 |   })
```