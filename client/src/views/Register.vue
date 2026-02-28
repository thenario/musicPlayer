<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
    <!-- 多层次背景 -->
    <div class="absolute inset-0 bg-gradient-to-tr from-blue-950 via-indigo-900 to-pink-900"></div>
    
    <!-- 几何网格背景 -->
    <div class="absolute inset-0 opacity-10">
      <div class="absolute inset-0" style="
        background-image: 
          linear-gradient(to right, #ffffff0a 1px, transparent 1px),
          linear-gradient(to bottom, #ffffff0a 1px, transparent 1px);
        background-size: 50px 50px;
      "></div>
    </div>
    
    <!-- 漂浮装饰元素 -->
    <div class="absolute top-20 right-10 w-64 h-64 bg-gradient-to-r from-cyan-500/20 to-blue-500/20 rounded-full blur-3xl"></div>
    <div class="absolute bottom-20 left-10 w-80 h-80 bg-gradient-to-r from-purple-500/20 to-pink-500/20 rounded-full blur-3xl"></div>
    <div class="absolute top-1/2 right-1/4 w-40 h-40 bg-gradient-to-r from-yellow-500/10 to-orange-500/10 rounded-full blur-2xl"></div>
    <div class="absolute bottom-1/3 left-1/4 w-60 h-60 bg-gradient-to-r from-green-500/10 to-teal-500/10 rounded-full blur-2xl"></div>
    
    <div class="w-full max-w-md z-10">
      <!-- 卡片头部 -->
      <div class="text-center mb-10">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-2xl shadow-lg mb-4">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path>
          </svg>
        </div>
        <h1 class="text-4xl font-bold bg-gradient-to-r from-cyan-400 to-blue-300 bg-clip-text text-transparent">
          加入我们
        </h1>
        <p class="text-gray-300 mt-2">创建账号，开启您的音乐之旅</p>
      </div>

      <!-- 表单卡片 -->
      <div class="bg-gray-900/70 backdrop-blur-lg rounded-2xl shadow-2xl border border-gray-800 p-8">
        <form @submit.prevent="handleRegister" class="space-y-6">
          <!-- 用户名输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              用户名
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"></path>
                </svg>
              </div>
              <input 
                v-model="form.user_name"
                type="text" 
                required
                class="w-full pl-10 pr-4 py-3 bg-gray-800/50 border border-gray-700 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:border-transparent transition-all"
                placeholder="设置您的用户名"
              >
            </div>
          </div>

          <!-- 邮箱输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              邮箱地址
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"></path>
                  <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"></path>
                </svg>
              </div>
              <input 
                v-model="form.email"
                type="email" 
                required
                class="w-full pl-10 pr-4 py-3 bg-gray-800/50 border border-gray-700 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                placeholder="输入您的邮箱"
              >
            </div>
          </div>

          <!-- 密码输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              密码
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                </svg>
              </div>
              <input 
                v-model="form.password"
                type="password" 
                required
                class="w-full pl-10 pr-4 py-3 bg-gray-800/50 border border-gray-700 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-pink-500 focus:border-transparent transition-all"
                placeholder="设置登录密码"
              >
            </div>
            <p class="mt-2 text-xs text-gray-500">建议使用8位以上字母、数字和符号组合</p>
          </div>

          <!-- 提交按钮 -->
          <button 
            type="submit"
            :disabled="loading"
            class="w-full bg-gradient-to-r from-cyan-600 to-blue-600 text-white font-semibold py-3.5 rounded-xl shadow-lg hover:shadow-xl hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 mt-2"
          >
            <span v-if="loading" class="flex items-center justify-center">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              注册中...
            </span>
            <span v-else>创建账号</span>
          </button>

          <!-- 成功/错误提示 -->
          <div v-if="success" class="mt-4 p-3 bg-green-900/30 border border-green-700/50 rounded-xl backdrop-blur-sm">
            <p class="text-green-300 text-center text-sm flex items-center justify-center">
              <svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
              </svg>
              注册成功！2秒后跳转到登录页面
            </p>
          </div>
          
          <div v-if="error" class="mt-4 p-3 bg-red-900/30 border border-red-700/50 rounded-xl backdrop-blur-sm">
            <p class="text-red-300 text-center text-sm flex items-center justify-center">
              <svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
              </svg>
              {{ error }}
            </p>
          </div>

          <!-- 登录链接 -->
          <div class="pt-6 border-t border-gray-800">
            <p class="text-center text-gray-400 text-sm">
              已有账号？
              <router-link 
                to="/login" 
                class="ml-1 font-semibold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-300 hover:from-cyan-300 hover:to-blue-200 transition-all"
              >
                立即登录 →
              </router-link>
            </p>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const form = ref({
  user_name: '',
  email: '',
  password: ''
})
const loading = ref(false)
const error = ref('')
const success = ref(false)

const handleRegister = async () => {
  loading.value = true
  error.value = ''
  success.value = false
  
  const result = await userStore.register(form.value)
  
  if (result.success) {
    success.value = true
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } else {
    error.value = result.error
  }
  
  loading.value = false
}
</script>

<style scoped>
/* 复用相同的动画样式，或添加注册页面特有的样式 */
</style>