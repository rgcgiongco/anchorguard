import { api } from './client'
import type {
  AppPolicy, AuthTokens, Device, DeviceDetail, DevicePolicy, DeviceSettings,
  MessageResponse, Parent, UsageAggregate
} from './types'

export const authApi = {
  login: (email: string, password: string) =>
    api.post<AuthTokens>('/auth/login', { email, password }).then((r) => r.data),
  logout: (refreshToken: string) =>
    api.post<MessageResponse>('/auth/logout', { refreshToken }).then((r) => r.data)
}

export const accountApi = {
  me: () => api.get<Parent>('/account/me').then((r) => r.data),
  update: (payload: { name?: string | null }) => api.patch<Parent>('/account/me', payload).then((r) => r.data),
  changePassword: (currentPassword: string, newPassword: string) =>
    api.post<MessageResponse>('/account/change-password', { currentPassword, newPassword }).then((r) => r.data),
  delete: () => api.delete<MessageResponse>('/account/me').then((r) => r.data)
}

export const devicesApi = {
  list: () => api.get<Device[]>('/devices').then((r) => r.data),
  get: (id: string) => api.get<DeviceDetail>(`/devices/${id}`).then((r) => r.data),
  remove: (id: string) => api.delete(`/devices/${id}`).then(() => undefined),
  patch: (id: string, payload: { name?: string | null }) => api.patch<DeviceDetail>(`/devices/${id}`, payload).then((r) => r.data)
}

export const policyApi = {
  get: (deviceId: string) => api.get<DevicePolicy>(`/devices/${deviceId}/policy`).then((r) => r.data),
  replace: (deviceId: string, payload: { settings?: DeviceSettings; apps?: AppPolicy[] }) =>
    api.put<DevicePolicy>(`/devices/${deviceId}/policy`, payload).then((r) => r.data),
  patch: (deviceId: string, payload: { settings?: Partial<DeviceSettings>; apps?: Partial<AppPolicy>[] }) =>
    api.patch<DevicePolicy>(`/devices/${deviceId}/policy`, payload).then((r) => r.data),
  apps: (deviceId: string) => api.get<AppPolicy[]>(`/devices/${deviceId}/apps`).then((r) => r.data),
  patchApp: (deviceId: string, packageName: string, payload: Partial<AppPolicy>) =>
    api.patch<AppPolicy>(`/devices/${deviceId}/apps/${encodeURIComponent(packageName)}`, payload).then((r) => r.data),
  getSchedule: (deviceId: string) => api.get<{ deviceId: string; weeklySchedule?: unknown }>(`/devices/${deviceId}/schedule`).then((r) => r.data),
  setSchedule: (deviceId: string, weeklySchedule: unknown) =>
    api.put<{ deviceId: string; weeklySchedule?: unknown }>(`/devices/${deviceId}/schedule`, { weeklySchedule }).then((r) => r.data)
}

export const reportsApi = {
  usage: (params: { deviceId: string; from?: string; to?: string; packageName?: string }) =>
    api.get<UsageAggregate[]>('/reports/usage', { params }).then((r) => r.data),
  exportUrl: (deviceId: string) => `${api.defaults.baseURL}/reports/export?deviceId=${encodeURIComponent(deviceId)}`
}
