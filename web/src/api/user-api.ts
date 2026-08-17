import type { IAxiosRes, IUser } from '@/types'
import request from './axios'
import { tokenStorage, userStorage } from '@/utils/storage'
import { encryptPassword } from '@/utils/crypto'

export interface RegisterRequest {
  user_name: string
  password: string
  user_email: string
}

type LoginRequest = { user_name: string; password: string }
type LoginResponse = { user: IUser; token: string }
type UserCoverResponse = { user_cover_url: string }
type EditProfileResponse = { user_name: string; user_cover_url: string }

/**
 * 用户模块接口。
 *
 * 泛型约定：响应拦截器已把 AxiosResponse 改写为 IAxiosRes（见 axios.ts），
 * 故各方法的第二泛型统一声明「返回包装类型」IAxiosRes<响应体>；
 * 第一泛型在 axios 语义里本是响应体类型，此处借位标注「请求体类型」（仅作可读，不约束入参）。
 */

/** 登录：提交用户名与加密后的密码，成功后缓存 user 与 token 到本地。 */
async function login(userName: string, password: string) {
  const encryptedPassword = await encryptPassword(password)
  const res = await request.post<LoginRequest, IAxiosRes<LoginResponse>>('/users/login', {
    user_name: userName,
    password: encryptedPassword,
    user_email: '',
  })

  userStorage.set(res.data.user)
  tokenStorage.set(res.data.token)
  return { success: true, message: res.message, ...res.data }
}

/** 注册：提交注册表单（密码会先加密再发送）。 */
async function register(registerFormdata: RegisterRequest) {
  const encryptedPassword = await encryptPassword(registerFormdata.password)
  const res = await request.post<RegisterRequest, IAxiosRes<null>>('/users/register', {
    ...registerFormdata,
    password: encryptedPassword,
  })
  return { success: true, message: res.message }
}

/** 登出：通知后端使 token 失效，并清除本地 user/token 缓存。 */
async function logout() {
  const res = await request.post<never, IAxiosRes<null>>('/users/logout')
  userStorage.remove()
  tokenStorage.remove()
  return { success: true, message: res.message }
}

/** 获取当前用户头像地址（silent：失败不集中弹错，由调用方处理）。 */
async function getUserCover() {
  const res = await request.get<never, IAxiosRes<UserCoverResponse>>('/users/cover', { silent: true })
  return { success: res.success, user_cover_url: res.data.user_cover_url }
}

/** 编辑个人资料：multipart 表单（含头像文件）。 */
async function editUserProfile(formdata: FormData) {
  const res = await request.patch<never, IAxiosRes<EditProfileResponse>>('/users/me', formdata)
  return { success: true, ...res.data }
}

export const userApi = { login, logout, register, editUserProfile, getUserCover }
