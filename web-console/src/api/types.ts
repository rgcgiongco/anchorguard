export type AuthTokens = {
  accessToken: string
  refreshToken: string
  tokenType?: string
}

export type Parent = {
  id: string
  email: string
  name?: string | null
  emailVerified: boolean
  createdAt: string
}

export type Device = {
  id: string
  hexId: string
  name?: string | null
  model?: string | null
  manufacturer?: string | null
  appVersion?: string | null
  lastSeen?: string | null
  isOnline: boolean
  isPaired: boolean
  createdAt: string
}

export type DeviceDetail = Device & {
  androidId?: string | null
  pairingCode?: string | null
  pairingExpiresAt?: string | null
}

export type AppPolicy = {
  id?: string
  deviceId?: string
  packageName: string
  appName?: string | null
  isAllowed: boolean
  dailyLimitMinutes?: number | null
  blockSchedule?: unknown
  internetAccess: string
  updatedAt?: string
}

export type DeviceSettings = {
  advancedTimer: boolean
  backgroundMode: boolean
  tamperProof: boolean
  bootStart: boolean
  keepAlive: boolean
  bruteForcePin: boolean
  blockIntervalMs: number
  appTitle: string
  weeklySchedule?: unknown
}

export type DevicePolicy = {
  deviceId: string
  settings: DeviceSettings
  apps: AppPolicy[]
}

export type UsageAggregate = {
  deviceId: string
  packageName?: string | null
  eventCount: number
  from: string
  to: string
}

export type MessageResponse = { message: string }
