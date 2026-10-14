import axios, { type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { router } from '@/router'
import { tokenStorage } from '@/utils/storage'

interface BackendPayload {
  code: number
  message?: string
  data?: unknown
}

/** 请求层只包装并抛出异常，提示交给组件。 */
export class AxiosBusinessError extends Error {
  readonly success = false

  constructor(message: string, public code: number, public data?: unknown) {
    super(message)
    this.name = 'AxiosBusinessError'
  }
}

const pendingMap = new Map<string, AbortController>()
const getRequestKey = (config: InternalAxiosRequestConfig) => {
  if (config.method?.toLowerCase() === 'get' || config.data instanceof FormData) return undefined
  return [config.method, config.url, JSON.stringify(config.params), JSON.stringify(config.data)].join('&')
}

const clearPendingRequest = (config: InternalAxiosRequestConfig) => {
  const key = getRequestKey(config)
  // 旧请求结束时不能移除、取消后发的新请求。
  if (key && pendingMap.get(key)?.signal === config.signal) pendingMap.delete(key)
}

const request = axios.create({ baseURL: '/api', timeout: 5000 })

request.interceptors.request.use((config) => {
  const key = getRequestKey(config)
  if (key && !config.signal) {
    pendingMap.get(key)?.abort()
    const controller = new AbortController()
    config.signal = controller.signal
    pendingMap.set(key, controller)
  }
  const token = tokenStorage.get()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

async function handleUnauthorized(config?: InternalAxiosRequestConfig) {
  const token = tokenStorage.get()
  // 登录密码错误不等于现有会话失效；过时请求也不能清掉刚登录的新 token。
  if (!token || config?.url === '/users/login' || config?.url === '/users/register'
    || config?.headers.Authorization !== `Bearer ${token}`) return
  const { useUserStore } = await import('@/stores/user')
  if (tokenStorage.get() !== token) return
  useUserStore().clearSession()
  if (router.currentRoute.value.path !== '/login') {
    await router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
  }
}

request.interceptors.response.use(
  async (response: AxiosResponse<BackendPayload>) => {
    clearPendingRequest(response.config)
    const res = response.data
    if (!res || typeof res !== 'object' || typeof res.code !== 'number') {
      throw new AxiosBusinessError('接口响应格式不正确', 502)
    }
    if (res.code !== 200) {
      if (res.code === 401) await handleUnauthorized(response.config)
      throw new AxiosBusinessError(res.message || '业务处理失败', res.code || 500, res.data)
    }
    return {
      success: true,
      data: res.data ?? null,
      message: res.message || '请求成功',
      code: 200,
    } as unknown as AxiosResponse
  },
  async (error: unknown) => {
    if (axios.isAxiosError<BackendPayload>(error)) {
      if (error.config) clearPendingRequest(error.config)
      if (axios.isCancel(error)) throw error
      const status = error.response?.status ?? 0
      if (status === 401) await handleUnauthorized(error.config)
      const messages: Record<number, string> = {
        401: '请先登录', 403: '没有访问权限', 404: '资源未找到',
        413: '上传内容超过大小限制', 429: '请求过于频繁，请稍后重试',
        500: '服务器内部错误', 502: '网关错误',
        503: '依赖服务暂不可用，请稍后重试', 504: '网关超时',
      }
      const message = error.response?.data?.message
        || messages[status]
        || (error.code === 'ECONNABORTED' ? '请求超时，请稍后重试'
          : error.response ? `请求失败（${status}）` : '服务器未响应，请检查网络')
      throw new AxiosBusinessError(message, status, error.response?.data)
    }
    if (axios.isCancel(error)) throw error
    throw new AxiosBusinessError(error instanceof Error ? error.message : '请求失败', 500)
  },
)

export default request
