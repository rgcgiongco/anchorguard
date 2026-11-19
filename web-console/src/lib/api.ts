import axios, { type AxiosInstance } from 'axios'
import type { AuthTokens } from '@/api/types'

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? 'https://anchorguard.tail78464.ts.net/api/v1'

export const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

export default api
