import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../utils/api' 

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const isAuthenticated = ref(false)
  
  const login = async (username, password) => {
    try {
      const response = await api.post('/api/login', {
        user_name: username,
        password: password
      })
      
      user.value = response.data
      isAuthenticated.value = true
      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.error || '登录失败' 
      }
    }
  }
  
  const register = async (userData) => {
    try {
      const response = await api.post('/api/register', userData)
      return { success: true, data: response.data }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.error || '注册失败' 
      }
    }
  }
  
  const logout = async () => {
    try {
      await api.post('/api/logout')
      user.value = null
      isAuthenticated.value = false
    } catch (error) {
      console.error('Logout failed:', error)
    }
  }
  
  const checkAuth = async () => {
    try {
      const response = await api.get('/api/getuser')
      user.value = response.data
      isAuthenticated.value = true
      return true
    } catch (error) {
      user.value = null
      isAuthenticated.value = false
      return false
    }
  }
  
  return {
    user,
    isAuthenticated,
    login,
    register,
    logout,
    checkAuth
  }
})