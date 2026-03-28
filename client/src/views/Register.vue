<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden bg-slate-950">
    <div class="absolute inset-0 bg-linear-to-tr from-blue-950 via-indigo-900 to-pink-900"></div>
    <div class="absolute inset-0 opacity-10"
      style="background-image: linear-gradient(to right, #ffffff0a 1px, transparent 1px), linear-gradient(to bottom, #ffffff0a 1px, transparent 1px); background-size: 50px 50px;">
    </div>

    <div class="w-full max-w-md z-10">
      <div class="text-center mb-8">
        <h1 class="text-4xl font-bold bg-linear-to-r from-cyan-400 to-blue-300 bg-clip-text text-transparent">加入我们
        </h1>
        <p class="text-gray-300 mt-2">创建账号，开启您的音乐之旅</p>
      </div>

      <div class="bg-gray-900/70 backdrop-blur-xl rounded-2xl shadow-2xl border border-gray-800 p-8">
        <form @submit.prevent="handleRegister" class="space-y-5">

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">用户名</label>
            <input v-model="form.user_name" type="text" placeholder="设置您的用户名"
              class="w-full bg-gray-800/50 border border-gray-700 rounded-xl px-4 py-3 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all">
            <p v-if="errors.user_name" class="text-red-400 text-xs mt-1">{{ errors.user_name }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">邮箱地址</label>
            <input v-model="form.user_email" type="email" placeholder="输入您的邮箱"
              class="w-full bg-gray-800/50 border border-gray-700 rounded-xl px-4 py-3 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all">
            <p v-if="errors.user_email" class="text-red-400 text-xs mt-1">{{ errors.user_email }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">密码</label>
            <input v-model="form.password" type="password" placeholder="设置登录密码"
              class="w-full bg-gray-800/50 border border-gray-700 rounded-xl px-4 py-3 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all">
            <p v-if="errors.password" class="text-red-400 text-xs mt-1 text-[10px]">{{ errors.password }}</p>
            <p v-else class="text-[10px] text-gray-500 mt-1">建议包含字母、数字和符号</p>
          </div>

          <button type="submit" :disabled="loading"
            class="w-full h-12 mt-4 bg-linear-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white font-bold rounded-xl shadow-lg transform transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed">
            <span v-if="loading">注册中...</span>
            <span v-else>创建账号</span>
          </button>

          <div class="mt-6 pt-4 border-t border-gray-800 text-center">
            <span class="text-gray-400 text-sm">已有账号？</span>
            <button type="button" @click="router.push('/login')" class="text-blue-400 text-sm hover:underline ml-1">
              立即登录 →
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const form = reactive({ user_name: '', user_email: '', password: '' })
const errors = reactive({ user_name: '', user_email: '', password: '' })

let redirectTimer: number | null = null

const validate = () => {
  let isValid = true
  errors.user_name = form.user_name.length < 3 ? '用户名至少3个字符' : ''
  errors.user_email = /^\S+@\S+\.\S+$/.test(form.user_email) ? '' : '邮箱格式不正确'
  errors.password = form.password.length < 8 ? '密码至少8位' : ''

  if (errors.user_name || errors.user_email || errors.password) isValid = false
  return isValid
}

const handleRegister = async () => {
  if (!validate()) return
  loading.value = true
  try {
    const result = await userStore.register(form)
    if (result.success) {
      ElMessage.success('注册成功！即将跳转登录页')
      redirectTimer = globalThis.setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      ElMessage.error(result.message || '注册失败')
    }
  } catch (err) {
    console.log(err)
    ElMessage.error('网络请求失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (redirectTimer) clearTimeout(redirectTimer)
})
</script>