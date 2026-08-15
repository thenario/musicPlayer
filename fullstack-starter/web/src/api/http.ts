import axios from 'axios'
export type ApiResponse<T> = { code: number; message: string; data: T }
export const http = axios.create({ baseURL: import.meta.env.VITE_API_URL ?? '/api', timeout: 10_000 })
