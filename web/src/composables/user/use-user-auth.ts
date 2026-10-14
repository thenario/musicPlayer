import type { Ref } from 'vue'
import { userApi, type RegisterRequest } from '@/api/user-api'
import type { ActionResult, IUser } from '@/types'
import { logBackgroundFailure, toActionFailure } from '@/utils/action-result'

interface UserAuthContext {
  user: Ref<IUser | null>
  isAuthenticated: Ref<boolean>
  userCoverUrl: Ref<string | undefined>
  clearSession: () => void
}

/** 账户业务只更新状态、返回结果；页面决定提示和跳转。 */
export function createUserAuth(ctx: UserAuthContext) {
  const fetchUserCoverUrl = async (): Promise<ActionResult> => {
    try {
      const response = await userApi.getUserCover()
      ctx.userCoverUrl.value = response.user_cover_url
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '头像加载失败')
    }
  }

  const login = async (userName: string, password: string): Promise<ActionResult> => {
    try {
      const response = await userApi.login(userName, password)
      ctx.user.value = response.user || null
      ctx.isAuthenticated.value = true
      // 头像加载不影响登录是否成功，后台失败仅记录日志。
      void fetchUserCoverUrl().then(logBackgroundFailure)
      return { success: true, message: response.message }
    } catch (error) {
      return toActionFailure(error, '登录失败')
    }
  }

  const register = async (userData: RegisterRequest): Promise<ActionResult> => {
    try {
      const response = await userApi.register(userData)
      return { success: true, message: response.message }
    } catch (error) {
      return toActionFailure(error, '注册失败')
    }
  }

  const logout = async (): Promise<ActionResult> => {
    try {
      const response = await userApi.logout()
      // 撤销 token 成功后再清理会话；503/网络失败时保留会话以便重试。
      ctx.clearSession()
      return { success: true, message: response.message }
    } catch (error) {
      return toActionFailure(error, '登出失败')
    }
  }

  const auth = async (): Promise<ActionResult> => {
    try {
      const res = await userApi.authUser()
      return { success: true, message: res.message }
    } catch (error) {
      return toActionFailure(error, '认证失败')
    }
  }

  return { login, register, logout, fetchUserCoverUrl, auth }
}
