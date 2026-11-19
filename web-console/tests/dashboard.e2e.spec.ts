import { test, expect } from '@playwright/test'

test.use({ browserName: 'chromium', headless: true })

const BASE_URL = 'https://anchorguard.tail78464.ts.net'

async function loginViaBrowser(page: any) {
  // Navigate to login page to establish same-origin context
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'commit', timeout: 20000 })
  await expect(page).toHaveURL(/.*\/login/, { timeout: 10000 })

  // Set sessionStorage via browser fetch (same-origin, no CORS)
  await page.evaluate(async () => {
    const res = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: 'parent@anchorguard.com', password: 'TestParent123!' }),
    })
    if (!res.ok) throw new Error(`Login failed: ${res.status}`)
    const tokens = await res.json()
    sessionStorage.setItem('anchorguard.session.tokens', JSON.stringify(tokens))
  })

  // Full page navigation to dashboard - React will remount and read sessionStorage
  await page.goto(`${BASE_URL}/dashboard`, { waitUntil: 'load', timeout: 30000 })
  await expect(page).toHaveURL(/.*\/dashboard/, { timeout: 10000 })

  // Wait for dashboard content
  await expect(page.locator('h1')).toContainText('Dashboard', { timeout: 15000 })
}

test.describe('AnchorGuard Parent Dashboard E2E', () => {
  test('unauthenticated access redirects to login', async ({ page }) => {
    await page.goto(`${BASE_URL}/dashboard`, { waitUntil: 'commit', timeout: 20000 })
    await expect(page).toHaveURL(/.*\/login/, { timeout: 10000 })
  })

  test('login redirects to dashboard and renders all dashboard sections', async ({ page }) => {
    const errors: string[] = []
    page.on('console', (msg: any) => {
      if (msg.type() === 'error') errors.push(msg.text())
    })

    await loginViaBrowser(page)

    // Stat cards row
    await expect(page.locator('text=Active Devices')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('text=Today\'s Screen Time')).toBeVisible()
    await expect(page.locator('text=Blocked Attempts')).toBeVisible()
    await expect(page.locator('text=Current Status')).toBeVisible()
    await expect(page.locator('text=Secured')).toBeVisible()

    // App Blocking & Controls - exact text match avoids activity log entries like "TikTok (Social Apps)"
    await expect(page.getByText('Social Apps', { exact: true })).toBeVisible()
    await expect(page.getByText('Chat', { exact: true })).toBeVisible()
    await expect(page.getByText('Gaming', { exact: true })).toBeVisible()
    await expect(page.getByText('Learning', { exact: true })).toBeVisible()

    // Charts
    await expect(page.locator('text=Screen Time & Limits')).toBeVisible()
    await expect(page.locator('text=App Usage')).toBeVisible()

    // Activity Log
    await expect(page.locator('text=Real-time Activity Log')).toBeVisible()
    await expect(page.locator('text=Live')).toBeVisible()

    if (errors.length > 0) {
      console.log('Console errors:', errors.slice(0, 5))
    }
  })

  test('sidebar navigation contains all required items', async ({ page }) => {
    await loginViaBrowser(page)

    await expect(page.getByRole('link', { name: 'Dashboard' }).first()).toBeVisible()
    await expect(page.getByRole('link', { name: 'Devices' }).first()).toBeVisible()
    await expect(page.getByRole('link', { name: 'App Blocker' }).first()).toBeVisible()
    await expect(page.getByRole('link', { name: 'Screen Time' }).first()).toBeVisible()
    await expect(page.getByRole('link', { name: 'Location' }).first()).toBeVisible()
    await expect(page.getByRole('link', { name: 'Reports' }).first()).toBeVisible()
    await expect(page.getByRole('link', { name: 'Settings' }).first()).toBeVisible()
    await expect(page.locator('label:has-text("Linked device")')).toBeVisible()
  })

  test('sidebar navigation links do not redirect to home', async ({ page }) => {
    await loginViaBrowser(page)

    const navLinks = [
      { name: 'Dashboard', expected: /.*\/dashboard$/ },
      { name: 'Devices', expected: /.*\/dashboard\/devices$/ },
      { name: 'Reports', expected: /.*\/dashboard\/reports$/ },
      { name: 'Settings', expected: /.*\/dashboard\/account$/ },
    ]

    for (const { name, expected } of navLinks) {
      await page.getByRole('link', { name }).first().click()
      await expect(page).toHaveURL(expected, { timeout: 10000 })
      // Ensure we did NOT redirect to home/login
      await expect(page).not.toHaveURL(/.*\/\?.*/, { timeout: 5000 })
      await expect(page).not.toHaveURL(/.*\/login/, { timeout: 5000 })
    }
  })

  test('device status empty state renders when no devices linked', async ({ page }) => {
    await loginViaBrowser(page)

    const emptyState = await page.locator('text=No devices linked').count() > 0
    expect(emptyState).toBe(true)

    // Charts render with progress bars
    await expect(page.locator('text=Screen Time & Limits')).toBeVisible()
    await expect(page.locator('text=App Usage')).toBeVisible()
  })

  test('toggle switches in app blocking are interactive', async ({ page }) => {
    await loginViaBrowser(page)

    // Chat toggle starts unchecked (blocking.chat = false), click to check it
    const toggle = page.locator('input[role="switch"]').nth(1)
    if ((await toggle.count()) > 0) {
      const initiallyChecked = await toggle.isChecked()
      expect(initiallyChecked).toBe(false)
      await toggle.click({ force: true })
      await expect(toggle).toBeChecked()
    }
  })

  test('sign out works from sidebar', async ({ page }) => {
    await loginViaBrowser(page)

    await page.click('button:has-text("Sign out")')
    await expect(page).toHaveURL(/.*\/login/, { timeout: 10000 })
  })
})
