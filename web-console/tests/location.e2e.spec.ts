import { test, expect } from '@playwright/test'

test.use({ browserName: 'chromium', headless: true })

const BASE_URL = 'https://anchorguard.tail78464.ts.net'

async function loginViaBrowser(page: any) {
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'commit', timeout: 20000 })
  await expect(page).toHaveURL(/.*\/login/, { timeout: 10000 })

  await page.evaluate(async () => {
    const res = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: 'parent@anchorguard.com', password: 'TestParent123!' })
    })
    if (!res.ok) throw new Error(`Login failed: ${res.status}`)
    const tokens = await res.json()
    sessionStorage.setItem('anchorguard.session.tokens', JSON.stringify(tokens))
  })

  await page.goto(`${BASE_URL}/dashboard/locations`, { waitUntil: 'load', timeout: 30000 })
  await expect(page).toHaveURL(/.*\/dashboard\/locations/, { timeout: 10000 })
}

test.describe('AnchorGuard Location E2E', () => {
  test('unauthenticated access redirects to login', async ({ page }) => {
    await page.goto(`${BASE_URL}/dashboard/locations`, { waitUntil: 'commit', timeout: 20000 })
    await expect(page).toHaveURL(/.*\/login/, { timeout: 10000 })
  })

  test('locations page renders map, history, geofence editor, and empty state', async ({ page }) => {
    const errors: string[] = []
    page.on('console', (msg: any) => {
      if (msg.type() === 'error') errors.push(msg.text())
    })

    await loginViaBrowser(page)

    // Page header
    await expect(page.locator('h1')).toContainText('Location', { timeout: 15000 })

    // Device selector present
    await expect(page.locator('select').first()).toBeVisible()

    // Empty state when no location data
    await expect(page.getByText('No location data yet')).toBeVisible({ timeout: 10000 })

    // Tracking toggle present
    await expect(page.getByText(/Enable location tracking/)).toBeVisible()

    // Geofence zone editor panel
    await expect(page.getByText('Geofence zones')).toBeVisible()
    await expect(page.getByPlaceholder('e.g. Home')).toBeVisible()

    if (errors.length > 0) {
      console.log('Console errors:', errors.slice(0, 5))
    }
  })

  test('sidebar location link navigates to /dashboard/locations', async ({ page }) => {
    await loginViaBrowser(page)

    await page.getByRole('link', { name: 'Location' }).first().click()
    await expect(page).toHaveURL(/.*\/dashboard\/locations/, { timeout: 10000 })
    await expect(page).not.toHaveURL(/.*\/login/, { timeout: 5000 })
  })

  test('geofence editor input fields are present', async ({ page }) => {
    await loginViaBrowser(page)

    await expect(page.getByPlaceholder('e.g. Home')).toBeVisible()
    await expect(page.getByPlaceholder('lat')).toBeVisible()
    await expect(page.getByPlaceholder('lng')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Add zone' })).toBeVisible()
  })

  test('map placeholder renders when no location data', async ({ page }) => {
    await loginViaBrowser(page)

    await expect(page.getByText('No location data available')).toBeVisible({ timeout: 10000 })
  })
})
