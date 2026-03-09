import { defineStore } from 'pinia'
import { ref } from 'vue'

import { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'

import { userApi } from '../../api/userApi'
import { IUser } from '../../type'

export const useUserStore = defineStore('user', () => {
  const localuser = localStorage.getItem('user') || null
  const user = ref<IUser | null>(localuser ? JSON.parse(localuser) : null)
  const isAuthenticated = ref(!!localuser)

  const login = async (user_name: string, password: string) => {
    const res = await userApi.login(user_name, password)
    if (!res.success) {
      ElMessage.error('登录失败')
      return { success: false }
    }
    user.value = res.user || null
    isAuthenticated.value = true
    return { success: true }
  }

  const register = async (userData: any) => {
    const res = await userApi.register(userData)
    if (!res.success) {
      ElMessage.error('注册失败')
      return { success: false }
    }
    return { success: true }
  }

  const logout = async (user_id: number) => {
    const res = await userApi.logout()
    if (!res.success) {
      ElMessage.error('登出失败')
      return { success: false }
    }
    user.value = null
    isAuthenticated.value = false
    return { success: true }
  }

  return { user, isAuthenticated, login, register, logout }
})
