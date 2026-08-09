import request from './axios'
import type { IAxiosRes } from '@/types'
import { tokenStorage, userStorage } from '@/utils/storage'
import { encryptPassword } from '@/utils/crypto'

//登录函数
async function login(user_name: string, password: string) {
  const encryptedPassword = await encryptPassword(password)

  const res = await request.post<any, IAxiosRes<any>>('/users/login', {
    user_name: user_name,
    password: encryptedPassword,
  })

  userStorage.set(res.data.user)
  tokenStorage.set(res.data.token)

  return {
    success: true,
    message: res.message,
    user: res.data.user,
    token: res.data.token,
  }
}

//注册函数
async function register(registerFormdata: {
  user_name: string
  password: string
  user_email: string
}) {
  const encryptedPassword = await encryptPassword(registerFormdata.password)

  const res = await request.post<any, IAxiosRes<any>>('/users/register', {
    user_name: registerFormdata.user_name,
    user_email: registerFormdata.user_email,
    password: encryptedPassword,
  })

  return {
    success: true,
    message: res.message,
  }
}

//登出函数
async function logout() {
  const res = await request.post<any, IAxiosRes<any>>('/users/logout')

  userStorage.remove()
  tokenStorage.remove()

  return { success: true, message: res.message }
}

//获取用户头像的url
async function getUserCover() {
  const res = await request.get<any, IAxiosRes<any>>(`/users/cover`, { silent: true })

  return {
    success: res.success,
    user_cover_url: res.data.user_cover_url,
  }
}

//编辑用户信息
async function editUserProfile(formdata: FormData) {
  const res = await request.patch<any, IAxiosRes<any>>('/users/me', formdata)

  return {
    success: true,
    user_name: res.data.user_name,
    user_cover_url: res.data.user_cover_url,
  }
}

export const userApi = { login, logout, register, editUserProfile, getUserCover }
