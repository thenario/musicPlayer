import axios, { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { router } from '../src/router/index'

//自定义错误类
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

    Object.setPrototypeOf(this, AxiosBusinessError.prototype) //将这个变量的类型指向自定义类型
  }
}

const pendingMap = new Map<string, AbortController>() //absortcontroller是一个用来终端异步任务的控制器

//根据配置的具体内容生成哈希key
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
//创建axios
const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})
//发送请求时的拦截器
request.interceptors.request.use(
  (config) => {
    //防止重复请求
    removePendingRequest(config) //发送请求时先中断之前的请求
    const controller = new AbortController() //创建控制器
    config.signal = controller.signal //将控制器和请求绑定
    pendingMap.set(getRequestKey(config), controller) //把新的指令加入字典
    //在请求头加上token
    const token = localStorage.getItem('token') //获取本地存储的token
    if (token) {
      //加上anth请求头，存入token信息
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  },
)

request.interceptors.response.use(
  //正常返回
  (response: AxiosResponse): any => {
    //删除命令
    removePendingRequest(response.config)
    //获取返回的结果，axios会自动调用浏览器把结果反序列化
    const res = response.data
    if (res.code !== 200) {
      //出现错误
      const errorMsg = res.message || '业务逻辑错误'
      //向上层抛出异常，被axios捕获，进入下面的error块
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
    //移除指令
    if (error.config) {
      removePendingRequest(error.config)
    }
    //如果是cancel被中断的指令，进入代码块
    if (axios.isCancel(error)) {
      //返回一个空promise，外层的函数不会有任何反应
      return new Promise(() => {})
    }

    let message = '网络连接异常' //默认的内容
    let status = 500 //默认的状态码
    let backendData = null //默认返回数据为空

    //如果成功响应但是状态码有问题
    if (error.response) {
      status = error.response.status //更新状态码
      backendData = error.response.data //更新数据

      //如果是登录过期，token过期
      if (status === 401) {
        message = backendData?.message || '登录已过期，请重新登录'
        localStorage.removeItem('token') //移除旧的token
        localStorage.removeItem('user') //移除旧的用户信息

        //跳转到登录页
        if (router && router.currentRoute.value.path !== '/login') {
          router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath },
          })
        }
      }
      //非401错误，正常抛出错误，待外层函数捕获处理
      else {
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
