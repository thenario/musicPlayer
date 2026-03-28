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
    try {
      const res = await userApi.login(user_name, password)
      user.value = res.user || null
      isAuthenticated.value = true
      return { success: true, message: res.message }
    } catch (err: any) {
      console.log(err)
      return { success: false, message: err.message }
    }
  }

  const register = async (userData: any) => {
    try {
      const res = await userApi.register(userData)
      return { success: true, message: res.message }
    } catch (err: any) {
      console.log(err)
      return { success: false, message: err.message }
    }
  }

  const logout = async (user_id: number) => {
    try {
      const res = await userApi.logout()
      user.value = null
      isAuthenticated.value = false
      return { success: true, message: res.message }
    } catch (err: any) {
      console.log(err)
      return { success: false, message: err.message }
    }
  }

  return { user, isAuthenticated, login, register, logout }
})
