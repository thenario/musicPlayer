import axios, { AxiosError, AxiosResponse } from 'axios'

const request = axios.create({
  baseURL: '127.0.0.1:3000/',
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
    console.log('请求发送失败， catch模块：axios.ts')
    return Promise.reject(error)
  },
)

request.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data || { data: {} }
  },
  (error: AxiosError) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          console.error('未授权，请重新登录')
          break
        case 403:
          console.error('拒绝访问')
          break
        case 404:
          console.error('资源未找到')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error(`网络错误: ${error.response.status}`)
      }
    } else {
      console.error('网络连接异常或服务器宕机')
    }

    return Promise.reject(error)
  },
)

export default request
