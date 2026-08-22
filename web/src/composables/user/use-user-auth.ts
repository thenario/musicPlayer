import type { Ref } from 'vue'
import { userApi, type RegisterRequest } from '@/api/user-api'
import type { IUser } from '@/types'
import  { useRouter } from 'vue-router'

interface UserAuthContext {
  user: Ref<IUser | null>
  isAuthenticated: Ref<boolean>
  userCoverUrl: Ref<string | undefined>
  clearSession: () => void
}

const errorMessage = (error: unknown) => error instanceof Error ? error.message : '请求失败'

/** Owns account API calls while receiving state mutation callbacks from the store. */
export function createUserAuth(ctx: UserAuthContext) {
  const router = useRouter()
  const fetchUserCoverUrl = async () => {
    try {
      const response = await userApi.getUserCover()
      router.push({name : 'login'})
      ctx.userCoverUrl.value = response.user_cover_url
    } catch (error) {
      console.error(error)
    }
  }

  const login = async (userName: string, password: string) => {
    try {
      const response = await userApi.login(userName, password)
      ctx.user.value = response.user || null
      ctx.isAuthenticated.value = true
      void fetchUserCoverUrl()
      return { success: true, message: response.message }
    } catch (error: unknown) {
      console.error(error)
      return { success: false, message: errorMessage(error) }
    }
  }

  const register = async (userData: RegisterRequest) => {
    try {
      const response = await userApi.register(userData)
      return { success: true, message: response.message }
    } catch (error: unknown) {
      console.error(error)
      return { success: false, message: errorMessage(error) }
    }
  }

  const logout = async () => {
    try {
      const response = await userApi.logout()
      return { success: true, message: response.message }
    } catch (error: unknown) {
      console.error(error)
      return { success: false, message: errorMessage(error) }
    } finally {
      ctx.clearSession()
    }
  }

  return { login, register, logout, fetchUserCoverUrl }
}
