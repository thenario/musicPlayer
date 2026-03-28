import axios, { AxiosError, AxiosResponse } from 'axios'
import { router } from '../src/router/index'

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

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

request.interceptors.request.use(
  (config) => {
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
    return {
      success: response.data?.success,
      data: response.data?.data || null,
      message: response.data?.message || '请求成功',
      code: response.data?.code || 200,
    }
  },
  (error: AxiosError) => {
    let message = '网络连接异常'
    let status = 500
    let backendData = null

    if (error.response) {
      status = error.response.status
      backendData = error.response.data

      switch (status) {
        case 401:
          message = (backendData as any)?.message || '登录已过期，请重新登录'

          localStorage.removeItem('token')
          localStorage.removeItem('user')
          if (router) {
            const currentPath = router.currentRoute.value.fullPath
            if (router.currentRoute.value.path !== '/login') {
              router.push({
                path: '/login',
                query: { redirect: currentPath },
              })
            }
          } else {
            globalThis.location.href = '/login'
          }
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '资源未找到'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = (backendData as any)?.message || `网络错误: ${status}`
      }
    } else if (error.request) {
      message = '服务器未响应，请检查网络'
    }

    throw new AxiosBusinessError(message, status, backendData)
  },
)

export default request
