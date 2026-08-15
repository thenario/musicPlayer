import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi, type RegisterRequest } from '@/api/user-api'
import type { IUser } from '@/types'
import { tokenStorage, userStorage } from '@/utils/storage'

const errorMessage = (error: unknown) => error instanceof Error ? error.message : '请求失败'

export const useUserStore = defineStore('user', () => {
  const user = ref<IUser | null>(userStorage.get())
  const isAuthenticated = ref(!!user.value)
  const userCoverUrl = ref<string>()

  const clearSession = () => {
    tokenStorage.remove()
    userStorage.remove()
    user.value = null
    userCoverUrl.value = undefined
    isAuthenticated.value = false
  }

  const login = async (user_name: string, password: string) => {
    try {
      const res = await userApi.login(user_name, password)
      user.value = res.user || null
      isAuthenticated.value = true
      fetchUserCoverUrl()
      return { success: true, message: res.message }
    } catch (err: unknown) {
      console.error(err)
      return { success: false, message: errorMessage(err) }
    }
  }

  const register = async (userData: RegisterRequest) => {
    try {
      const res = await userApi.register(userData)
      return { success: true, message: res.message }
    } catch (err: unknown) {
      console.error(err)
      return { success: false, message: errorMessage(err) }
    }
  }

  const logout = async () => {
    try {
      const res = await userApi.logout()
      return { success: true, message: res.message }
    } catch (err: unknown) {
      console.error(err)
      return { success: false, message: errorMessage(err) }
    } finally {
      clearSession()
    }
  }

  const fetchUserCoverUrl = async () => {
    try {
      const res = await userApi.getUserCover()
      userCoverUrl.value = res.user_cover_url
    } catch (err) {
      console.error(err)
    }
  }

  return { user, isAuthenticated, userCoverUrl, login, register, logout, clearSession, fetchUserCoverUrl }
})
