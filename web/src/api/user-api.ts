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

async function register(registerFormdata: RegisterRequest) {
  const encryptedPassword = await encryptPassword(registerFormdata.password)
  const res = await request.post<RegisterRequest, IAxiosRes<null>>('/users/register', {
    ...registerFormdata,
    password: encryptedPassword,
  })
  return { success: true, message: res.message }
}

async function logout() {
  const res = await request.post<never, IAxiosRes<null>>('/users/logout')
  userStorage.remove()
  tokenStorage.remove()
  return { success: true, message: res.message }
}

async function getUserCover() {
  const res = await request.get<never, IAxiosRes<UserCoverResponse>>('/users/cover', { silent: true })
  return { success: res.success, user_cover_url: res.data.user_cover_url }
}

async function editUserProfile(formdata: FormData) {
  const res = await request.patch<never, IAxiosRes<EditProfileResponse>>('/users/me', formdata)
  return { success: true, ...res.data }
}

export const userApi = { login, logout, register, editUserProfile, getUserCover }