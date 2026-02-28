import axios from 'axios'

// 创建一个独立的 axios 实例
const api = axios.create({
  // 确保这里地址正确，不要用 localhost，统一用 127.0.0.1
  baseURL: 'http://127.0.0.1:5000', 
  // 关键：在这里强制开启凭证携带
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 响应拦截器：自动处理 401/302 问题
api.interceptors.response.use(
  response => response,
  error => {
    // 如果后端返回 401 (未登录)，可以自动清除前端状态
    if (error.response && error.response.status === 401) {
      console.log("登录已过期");
      // 这里可以做一些强制登出的操作
    }
    return Promise.reject(error)
  }
)

export default api