<template>
  <AuthPageLayout bg-class="bg-slate-950 text-white" gradient-class="bg-linear-to-tr from-blue-950 via-indigo-900 to-pink-900">
    <template #header>
      <h1 class="text-4xl font-bold bg-linear-to-r from-cyan-400 to-blue-300 bg-clip-text text-transparent">加入我们
      </h1>
      <p class="text-gray-300 mt-2">创建账号，开启您的音乐之旅</p>
    </template>

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
  </AuthPageLayout>
</template>

<script setup lang="ts">
import { onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import AuthPageLayout from '@/views/user/components/AuthPageLayout.vue'
import { useRegister } from './composables/useRegister'

const router = useRouter()
const { form, errors, loading, register } = useRegister()

let redirectTimer: number | null = null

const handleRegister = async () => {
  const result = await register()
  if (result.success) {
    ElMessage.success('注册成功！即将跳转登录页')
    redirectTimer = globalThis.setTimeout(() => {
      router.push('/login')
    }, 2000)
  }
}

onUnmounted(() => {
  if (redirectTimer) clearTimeout(redirectTimer)
})
</script>
