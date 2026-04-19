import request from './axios'
import type { ILogin, ILogout, IRegister, IAxiosRes } from '../type'

export async function encryptPassword(password: string) {
  const msgUint8 = new TextEncoder().encode(password)
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgUint8)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map((b) => b.toString(16).padStart(2, '0')).join('')
}

async function login(user_name: string, password: string) {
  const encryptedPassword = await encryptPassword(password)

  const res = await request.post<any, IAxiosRes<any>>('/users/login', {
    user_name: user_name,
    password: encryptedPassword,
  })

  if (!res.success) {
    return { success: false, message: res.message } as ILogin
  }
  console.log('后端返回全貌:', res)
  localStorage.setItem('user', JSON.stringify(res.data.user))
  localStorage.setItem('token', res.data.token)

  return {
    success: true,
    message: res.message,
    user: res.data.user,
    token: res.data.token,
  }
}

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

  if (!res.success) return { success: false, message: res.message } as IRegister

  return {
    success: true,
    message: res.message,
  }
}

async function logout() {
  const res = await request.post<any, IAxiosRes<any>>('/users/logout')

  if (!res.success) return { success: false, message: res.message } as ILogout

  localStorage.removeItem('user')
  localStorage.removeItem('token')

  return { success: true, message: res.message }
}

async function getUSerCover() {
  const res = await request.get<any, IAxiosRes<any>>(`/users/cover`)

  if (!res.success) return { success: false, message: res.message }

  return {
    success: res.success,
    user_cover_url: res.data.user_cover_url,
  }
}

async function editUserProfile(formdata: FormData) {
  const res = await request.patch<any, IAxiosRes<any>>('/users/me', formdata)

  if (!res.success) return { success: false, message: res.message }

  return {
    success: true,
    user_name: res.data.user_name,
    user_cover_url: res.data.user_cover_url,
  }
}

export const userApi = { login, logout, register, editUserProfile, getUSerCover }
