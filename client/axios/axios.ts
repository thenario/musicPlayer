import axios, { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { router } from '../src/router/index'

// 自定义错误类
class AxiosBusinessError extends Error {
  code: number
  data: any
  success: boolean

  constructor(message: string, code: number, data?: any) {
    super(message)
    this.name = 'AxiosBusinessError'
    this.code = code
    this.data = data
    this.success = false

    Object.setPrototypeOf(this, AxiosBusinessError.prototype)
  }
}

const pendingMap = new Map<string, AbortController>()

const getRequestKey = (config: InternalAxiosRequestConfig) => {
  return [
    config.method,
    config.url,
    JSON.stringify(config.params),
    JSON.stringify(config.data),
  ].join('&')
}

const removePendingRequest = (config: InternalAxiosRequestConfig) => {
  const key = getRequestKey(config)
  if (pendingMap.has(key)) {
    const controller = pendingMap.get(key)
    controller?.abort()
    pendingMap.delete(key)
  }
}

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

request.interceptors.request.use(
  (config) => {
    removePendingRequest(config)
    const controller = new AbortController()
    config.signal = controller.signal
    pendingMap.set(getRequestKey(config), controller)

    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  },
)

request.interceptors.response.use(
  (response: AxiosResponse): any => {
    removePendingRequest(response.config)

    const res = response.data // 后端返回的 JSON 数据 { code, message, data }
    if (res.code !== 200) {
      const errorMsg = res.message || '业务逻辑错误'
      throw new AxiosBusinessError(errorMsg, res.code || 500, res.data)
    }

    return {
      success: true,
      data: res.data || null,
      message: res.message || '请求成功',
      code: 200,
    }
  },
  (error: any) => {
    if (error.config) {
      removePendingRequest(error.config)
    }

    if (axios.isCancel(error)) {
      return new Promise(() => {})
    }

    let message = '网络连接异常'
    let status = 500
    let backendData = null

    if (error.response) {
      status = error.response.status
      backendData = error.response.data

      if (status === 401) {
        message = backendData?.message || '登录已过期，请重新登录'
        localStorage.removeItem('token')
        localStorage.removeItem('user')

        if (router && router.currentRoute.value.path !== '/login') {
          router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath },
          })
        }
      } else {
        const statusMap: Record<number, string> = {
          403: '拒绝访问',
          404: '资源未找到',
          500: '服务器内部错误',
          502: '网关错误',
          504: '网关超时',
        }
        message = backendData?.message || statusMap[status] || `网络错误: ${status}`
      }
    } else if (error.request) {
      message = '服务器未响应，请检查网络'
    } else {
      message = error.message
    }

    throw new AxiosBusinessError(message, status, backendData)
  },
)

export default request
