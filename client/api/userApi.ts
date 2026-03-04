import request from './axios'

import { ILogin, ILogout, IUser, IRegister } from '../type'

import { encryptPassword } from '../src/utils/crypto'

async function login(user_name: string, password: string) {
  try {
    password = encryptPassword(password)
    const res = await request.post<any, ILogin>('api/login', {
      user_name: user_name,
      password: password,
    })
    localStorage.setItem('user', JSON.stringify(res.user))
    localStorage.setItem('token', res.token)
    return {
      success: res.success,
      message: res.message,
      user: res.user,
    }
  } catch (error: any) {
    return {
      error: error,
      success: error.response?.success,
      message: error.response?.message,
    }
  }
}

async function register(registerFormdata: {
  user_name: string
  password: string
  user_email: string
}) {
  try {
    registerFormdata.password = encryptPassword(registerFormdata.password)
    const res = await request.post<any, IRegister>('api/register', {
      user_name: registerFormdata.user_name,
      user_email: registerFormdata.user_email,
      password: registerFormdata.password,
    })

    return {
      success: res.success,
      message: res.message,
      user: res.user,
    }
  } catch (error: any) {
    return {
      error: error,
      success: error.response?.success,
      message: error.response?.message,
    }
  }
}

async function logout(user_id: number) {
  try {
    const res = await request.post<any, ILogout>('api/logout', { user_id: user_id })
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      error: error,
      success: error.response?.success,
      message: error.response?.message,
    }
  }
}

export const userApi = {
  login,
  logout,
  register,
}
