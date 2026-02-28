<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
    <!-- 多层次背景 -->
    <div class="absolute inset-0 bg-gradient-to-br from-gray-900 via-purple-900 to-blue-900"></div>
    
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
    <div class="absolute top-20 left-10 w-64 h-64 bg-gradient-to-r from-cyan-500/20 to-blue-500/20 rounded-full blur-3xl"></div>
    <div class="absolute bottom-20 right-10 w-80 h-80 bg-gradient-to-r from-purple-500/20 to-pink-500/20 rounded-full blur-3xl"></div>
    <div class="absolute top-1/2 left-1/4 w-40 h-40 bg-gradient-to-r from-yellow-500/10 to-orange-500/10 rounded-full blur-2xl"></div>
    <div class="absolute bottom-1/3 right-1/4 w-60 h-60 bg-gradient-to-r from-green-500/10 to-teal-500/10 rounded-full blur-2xl"></div>
    
    <!-- 内容层 -->
    <div class="w-full max-w-md z-10">
      <!-- 卡片头部 -->
      <div class="text-center mb-10">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-blue-500 to-purple-600 rounded-2xl shadow-lg mb-4">
          <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
            <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"></path>
          </svg>
        </div>
        <h1 class="text-4xl font-bold bg-gradient-to-r from-blue-400 to-purple-300 bg-clip-text text-transparent">
          欢迎回来
        </h1>
        <p class="text-gray-300 mt-2">登录您的音乐空间，发现更多精彩</p>
      </div>

      <!-- 表单卡片 -->
      <div class="bg-gray-900/70 backdrop-blur-lg rounded-2xl shadow-2xl border border-gray-800/50 p-8 transform transition-all">
        <form @submit.prevent="handleLogin" class="space-y-6">
          <!-- 用户名输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              用户名
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
              </div>
              <input 
                v-model="form.user_name"
                type="text" 
                required
                class="w-full pl-10 pr-4 py-3 bg-gray-800/50 border border-gray-700 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300"
                placeholder="请输入用户名"
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
                class="w-full pl-10 pr-4 py-3 bg-gray-800/50 border border-gray-700 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all duration-300"
                placeholder="请输入密码"
              >
            </div>
          </div>

          <!-- 提交按钮 -->
          <button 
            type="submit"
            :disabled="loading"
            :class="['w-full text-white font-semibold py-3.5 rounded-xl shadow-lg transition-all duration-300', 
                     loading ? 'bg-gray-700 cursor-not-allowed' : 'bg-gradient-to-r from-blue-600 to-purple-600 hover:shadow-xl hover:scale-[1.02] active:scale-[0.98]']"
          >
            <span v-if="loading" class="flex items-center justify-center">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              登录中...
            </span>
            <span v-else>立即登录</span>
          </button>

          <!-- 错误提示 -->
          <div v-if="error" class="mt-4 p-4 bg-red-900/30 border border-red-700/50 rounded-xl backdrop-blur-sm">
            <p class="text-red-300 text-center text-sm flex items-center justify-center">
              <svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
              </svg>
              {{ error }}
            </p>
          </div>

          <!-- 注册链接 -->
          <div class="pt-6 border-t border-gray-800">
            <p class="text-center text-gray-400 text-sm">
              还没有账号？
              <router-link 
                to="/register" 
                class="ml-1 font-semibold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-300 hover:from-blue-300 hover:to-purple-200 transition-all duration-300"
              >
                立即注册 →
              </router-link>
            </p>
          </div>
        </form>
      </div>
      
      <!-- 底部版权信息 -->
      <div class="mt-8 text-center">
        <p class="text-gray-500 text-sm">© 2025 音乐播放器 · 享受您的音乐时光</p>
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

// 表单数据
const form = ref({
  user_name: '',  // 使用 user_name 与后端保持一致
  password: ''
})

// UI状态
const loading = ref(false)
const error = ref('')
const inputFocused = ref(null)

// 登录处理
const handleLogin = async () => {
  // 重置状态
  loading.value = true
  error.value = ''
  
  try {
    // 输入验证
    if (!form.value.user_name.trim()) {
      error.value = '用户名不能为空'
      loading.value = false
      return
    }
    
    if (!form.value.password) {
      error.value = '密码不能为空'
      loading.value = false
      return
    }
    
    // 调用登录接口
    const result = await userStore.login(form.value.user_name, form.value.password)
    
    if (result.success) {
      // 登录成功，跳转到首页
      router.push('/')
    } else {
      // 登录失败，显示错误信息
      error.value = result.error || '登录失败，请检查用户名和密码'
    }
  } catch (err) {
    // 网络或其他错误
    error.value = '网络错误，请稍后重试'
    console.error('登录错误:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 自定义动画 */
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes pulse-slow {
  0%, 100% { 
    opacity: 0.3;
    transform: scale(1);
  }
  50% { 
    opacity: 0.5;
    transform: scale(1.05);
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

.animate-fade-in {
  animation: fade-in 0.8s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.animate-pulse-slow {
  animation: pulse-slow 8s ease-in-out infinite;
}

.animate-shake {
  animation: shake 0.5s ease-in-out;
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(to bottom, #6366f1, #8b5cf6);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(to bottom, #4f46e5, #7c3aed);
}

/* 输入框焦点效果 */
input:focus {
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

/* 响应式调整 */
@media (max-width: 640px) {
  .p-8 {
    padding: 1.5rem;
  }
  
  .text-4xl {
    font-size: 2rem;
  }
  
  .w-96, .w-80 {
    width: 300px;
    height: 300px;
  }
}

/* 打印样式 */
@media print {
  .absolute, 
  .bg-gradient-to-br,
  .backdrop-blur-lg,
  .shadow-2xl {
    display: none !important;
  }
  
  .bg-gray-900 {
    background: white !important;
  }
  
  .text-white {
    color: black !important;
  }
}
</style>