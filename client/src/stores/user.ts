import { defineStore } from 'pinia'
import { ref } from 'vue'

import 'element-plus/dist/index.css'

import { userApi } from '../../axios/userApi'
import { IUser } from '../../type'

export const useUserStore = defineStore('user', () => {
  const localuser = localStorage.getItem('user') || null
  const user = ref<IUser | null>(localuser ? JSON.parse(localuser) : null)
  const isAuthenticated = ref(!!localuser)

  const login = async (user_name: string, password: string) => {
    const res = await userApi.login(user_name, password)
    if (!res.success) {
      return { success: false, message: res.message }
    }
    user.value = res.user || null
    isAuthenticated.value = true
    return { success: true, message: res.message }
  }

  const register = async (userData: any) => {
    const res = await userApi.register(userData)
    if (!res.success) {
      return { success: false, message: res.message }
    }
    return { success: true, message: res.message }
  }

  const logout = async (user_id: number) => {
    const res = await userApi.logout()
    if (!res.success) {
      return { success: false, message: res.message }
    }
    user.value = null
    isAuthenticated.value = false
    return { success: true, message: res.message }
  }

  return { user, isAuthenticated, login, register, logout }
})
