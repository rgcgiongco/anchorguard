export const brandColors = {
  navy: '#1E3A8A',
  navyDark: '#15306B',
  orange: '#F97316',
  orangeDark: '#C2410C',
  white: '#FFFFFF',
  grayLight: '#F3F4F6',
} as const

export const API_BASE_URL = 'https://anchorguard.tail78464.ts.net/api/v1/'

export const IMAGE_PATHS = {
  logo: '/src/assets/images/logo.png',
  heroHome: '/src/assets/images/hero-home.jpg',
  heroAlt: '/src/assets/images/hero-alt.jpg',
  trustBadges: '/src/assets/images/trust-badges.png',
  appBlocker: '/src/assets/images/app-blocker.jpg',
  locationSafety: '/src/assets/images/location-safety.jpg',
  dashboardHero: '/src/assets/images/dashboard-hero.jpg',
  dashboardUI: '/src/assets/images/dashboard-ui.jpg',
  emptyDevices: '/src/assets/images/empty-devices.jpg',
  emptyActivity: '/src/assets/images/empty-activity.jpg',
} as const

export const APP_NAME = 'AnchorGuard'
export const APP_TAGLINE = 'Guided access, anchored in trust'

export const LOCATION = {
  enabled: true,
  mapProvider: 'openstreetmap',
  tileUrl: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
  attribution: '© OpenStreetMap contributors',
  defaultZoom: 13,
  geofenceEnabled: true,
} as const

export const LOCATION_API_BASE = API_BASE_URL
