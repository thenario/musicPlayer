import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api/userApi'
import type { IUser } from '@/types'
import { userStorage } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const user = ref<IUser | null>(userStorage.get())
  const isAuthenticated = ref(!!user.value)
  const userCoverUrl = ref<string>()

  const login = async (user_name: string, password: string) => {
    try {
      const res = await userApi.login(user_name, password)
      user.value = res.user || null
      isAuthenticated.value = true
      fetchUserCoverUrl()
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

  const logout = async () => {
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

  const fetchUserCoverUrl = async () => {
    try {
      const res = await userApi.getUserCover()
      userCoverUrl.value = res.user_cover_url
    } catch (err) {
      console.log(err)
    }
  }

  return { user, isAuthenticated, userCoverUrl, login, register, logout }
})
