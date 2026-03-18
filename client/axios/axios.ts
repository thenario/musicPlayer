import axios, { AxiosError, AxiosResponse } from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    console.log('请求发送失败， catch模块:axios.ts')
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

    if (error.response) {
      status = error.response.status
      switch (status) {
        case 401:
          message = '未授权，请重新登录'
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
          message = `网络错误: ${status}`
      }
    } else if (error.request) {
      message = '服务器未响应，请检查网络'
    }

    return Promise.resolve({
      success: false,
      data: null,
      message: (error.response as any).data?.message || message,
      code: status,
    })
  },
)

export default request
