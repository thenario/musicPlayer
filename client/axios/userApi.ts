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

export const userApi = { login, logout, register }
