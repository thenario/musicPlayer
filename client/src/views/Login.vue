<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
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
          <el-icon :size="32" color="white">
            <UserFilled />
          </el-icon>
        </div>
        <h1 class="text-4xl font-bold bg-linear-to-r from-blue-400 to-purple-300 bg-clip-text text-transparent">欢迎回来
        </h1>
        <p class="text-gray-300 mt-2">登录您的音乐空间，发现更多精彩</p>
      </div>

      <div
        class="bg-gray-900/70 backdrop-blur-xl rounded-2xl shadow-2xl border border-gray-800/50 p-8 custom-login-form">
        <el-form ref="loginFormRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
          <el-form-item label="用户名" prop="user_name">
            <el-input v-model="form.user_name" placeholder="请输入用户名" :prefix-icon="User" clearable />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>

          <el-button type="primary" class="w-full mt-4 login-btn" :loading="loading" @click="handleLogin(loginFormRef)">
            {{ loading ? '登录中...' : '立即登录' }}
          </el-button>

          <div class="mt-8 pt-6 border-t border-gray-800 text-center">
            <span class="text-gray-400 text-sm">还没有账号？</span>
            <el-link type="primary" :underline="false" class="register-link" @click="router.push('/register')">
              立即注册 →
            </el-link>
          </div>
        </el-form>
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
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  user_name: '',
  password: ''
})

const rules = reactive<FormRules>({
  user_name: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, message: '用户名长度至少为 3 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 位', trigger: 'blur' }
  ]
})

const handleLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return

  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const result = await userStore.login(form.user_name, form.password)

        if (result.success) {
          ElMessage.success("登录成功，欢迎回来！")
          router.push('/')
        } else {
        }
      } catch (err) {
        ElMessage.error('网络连接超时，请稍后重试')
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请正确填写登录信息')
    }
  })
}
</script>

<style scoped>
/* 深度修改 Element Plus 样式以适配暗色玻璃主题 */
:deep(.el-form-item__label) {
  color: #d1d5db !important;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 8px !important;
}

:deep(.el-input__wrapper) {
  background-color: rgba(31, 41, 55, 0.5) !important;
  box-shadow: 0 0 0 1px rgba(55, 65, 81, 0.5) inset !important;
  border-radius: 12px;
  padding: 8px 12px;
  transition: all 0.3s;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #6366f1 inset !important;
  background-color: rgba(31, 41, 55, 0.8) !important;
}

:deep(.el-input__inner) {
  color: white !important;
}

.login-btn {
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(to right, #2563eb, #9333ea);
  border: none;
  font-weight: 600;
  font-size: 1rem;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.4);
}

.register-link {
  background: linear-gradient(to right, #60a5fa, #c084fc);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 600;
}

/* 动画保持 */
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