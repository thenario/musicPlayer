import { defineStore } from 'pinia'
import { ref } from 'vue'

import { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'

import { userApi } from '../../api/userApi'
import { IUser } from '../../type'

export const useUserStore = defineStore('user', () => {
  const localuser = localStorage.getItem('user')
  const user = ref<IUser | null>(localuser ? JSON.parse(localuser) : null)
  const isAuthenticated = ref(false)

  const login = async (user_name: string, password: string) => {
    const res = await userApi.login(user_name, password)
    if (res.success) {
      ElMessage.success('登录成功')
      isAuthenticated.value = true
      user.value = res.user || null
    } else ElMessage.error(`登录失败,请稍后重试`)
  }

  const register = async (userData: {
    user_name: string
    password: string
    user_email: string
  }) => {
    const res = await userApi.register(userData)
    if (res.success) ElMessage.success('注册成功')
    else ElMessage.error('注册失败,请稍后重试')
  }

  const logout = async (user_id: number) => {
    const res = await userApi.logout(user_id)
    if (res.success) {
      ElMessage.success('登出成功')
      user.value = null
      isAuthenticated.value = false
    } else ElMessage.error('登出失败,请稍后重试')
  }

  return {
    user,
    login,
    register,
    logout,
  }
})
