<template>
  <AuthPageLayout bg-class="bg-gray-950 text-white" gradient-class="bg-linear-to-br from-gray-900 via-purple-900 to-blue-900">
    <template #header>
      <div
        class="inline-flex items-center justify-center w-16 h-16 bg-linear-to-r from-blue-500 to-purple-600 rounded-2xl shadow-lg mb-4">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-8 h-8 text-white" viewBox="0 0 24 24" fill="currentColor">
          <path
            d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
        </svg>
      </div>
      <h1 class="text-4xl font-bold bg-linear-to-r from-blue-400 to-purple-300 bg-clip-text text-transparent">欢迎回来
      </h1>
      <p class="text-gray-300 mt-2">登录您的音乐空间，发现更多精彩</p>
    </template>

    <form @submit.prevent="handleLogin" class="space-y-6">
      <div class="space-y-2">
        <label class="block text-sm font-medium text-gray-300 ml-1">用户名</label>
        <div class="relative group">
          <input v-model="form.user_name" type="text" placeholder="请输入用户名"
            class="w-full bg-gray-800/50 border border-gray-700/50 rounded-xl px-4 py-3 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all group-hover:bg-gray-800/80">
        </div>
        <p v-if="errors.user_name" class="text-red-400 text-xs mt-1 ml-1">{{ errors.user_name }}</p>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-medium text-gray-300 ml-1">密码</label>
        <div class="relative group">
          <input v-model="form.password" type="password" placeholder="请输入密码"
            class="w-full bg-gray-800/50 border border-gray-700/50 rounded-xl px-4 py-3 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all group-hover:bg-gray-800/80">
        </div>
        <p v-if="errors.password" class="text-red-400 text-xs mt-1 ml-1">{{ errors.password }}</p>
      </div>

      <button type="submit" :disabled="loading"
        class="w-full h-12 mt-2 bg-linear-to-r from-blue-600 to-purple-600 hover:from-blue-500 hover:to-purple-500 text-white font-bold rounded-xl shadow-lg transform transition-all active:scale-95 disabled:opacity-50">
        <span v-if="loading">登录中...</span>
        <span v-else>立即登录</span>
      </button>

      <div class="mt-8 pt-6 border-t border-gray-800 text-center">
        <span class="text-gray-400 text-sm">还没有账号？</span>
        <button type="button" @click="router.push('/register')"
          class="ml-1 text-sm font-semibold bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent hover:opacity-80 transition-opacity">
          立即注册 →
        </button>
      </div>
    </form>

    <template #footer>
      <p class="text-gray-500 text-sm">© 2026 音乐播放器 · 享受您的音乐时光</p>
    </template>
  </AuthPageLayout>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { usePlayerStore } from '@/stores/player'
import AuthPageLayout from './components/AuthPageLayout.vue'
import { useLogin } from './composables/useLogin'

const playStore = usePlayerStore()
const router = useRouter()
const { form, errors, loading, login } = useLogin()

const handleLogin = async () => {
  const result = await login()
  if (result.success) {
    ElMessage.success("登录成功，欢迎回来！")
    playStore.fetchUserQueues()
    playStore.fetchCurrentQueue()
    router.push('/')
  }
}
</script>
