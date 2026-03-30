<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden bg-gray-950 text-white">
    <div class="absolute inset-0 bg-linear-to-br from-gray-900 via-purple-900 to-blue-900"></div>
    <div class="absolute inset-0 opacity-10"
      style="background-image: linear-gradient(to right, #ffffff0a 1px, transparent 1px), linear-gradient(to bottom, #ffffff0a 1px, transparent 1px); background-size: 50px 50px;">
    </div>

    <div
      class="absolute top-20 left-10 w-64 h-64 bg-linear-to-r from-cyan-500/20 to-blue-500/20 rounded-full blur-3xl animate-pulse-slow">
    </div>
    <div
      class="absolute bottom-20 right-10 w-80 h-80 bg-linear-to-r from-purple-500/20 to-pink-500/20 rounded-full blur-3xl animate-pulse-slow">
    </div>

    <div class="w-full max-w-md z-10 animate-fade-in">
      <div class="text-center mb-10">
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
      </div>

      <div class="bg-gray-900/70 backdrop-blur-xl rounded-2xl shadow-2xl border border-gray-800/50 p-8">
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
      </div>

      <div class="mt-8 text-center">
        <p class="text-gray-500 text-sm">© 2026 音乐播放器 · 享受您的音乐时光</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { usePlayerStore } from '../stores/player'

const userStore = useUserStore()
const playStore = usePlayerStore()
const router = useRouter()
const loading = ref(false)

const form = reactive({
  user_name: '',
  password: ''
})

const errors = reactive({
  user_name: '',
  password: ''
})

const validate = () => {
  let isValid = true
  errors.user_name = form.user_name.length < 3 ? '用户名长度至少为 3 位' : ''
  errors.password = form.password.length < 6 ? '密码长度至少为 6 位' : ''

  if (errors.user_name || errors.password) isValid = false
  return isValid
}

const handleLogin = async () => {
  if (!validate()) return

  loading.value = true
  try {
    const result = await userStore.login(form.user_name, form.password)
    if (result.success) {
      ElMessage.success("登录成功，欢迎回来！")
      playStore.fetchUserQueues()
      playStore.fetchCurrentQueue()
      router.push('/')
    } else {
      ElMessage.error(result.message || "登录失败，请检查用户名或密码")
    }
  } catch (err) {
    console.log(err)
    alert('网络连接超时，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in {
  animation: fade-in 0.8s ease-out forwards;
}

@keyframes pulse-slow {

  0%,
  100% {
    opacity: 0.2;
    transform: scale(1);
  }

  50% {
    opacity: 0.4;
    transform: scale(1.05);
  }
}

.animate-pulse-slow {
  animation: pulse-slow 8s ease-in-out infinite;
}
</style>