<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
    <div class="absolute inset-0 bg-linear-to-tr from-blue-950 via-indigo-900 to-pink-900"></div>
    <div class="absolute inset-0 opacity-10"
      style="background-image: linear-gradient(to right, #ffffff0a 1px, transparent 1px), linear-gradient(to bottom, #ffffff0a 1px, transparent 1px); background-size: 50px 50px;">
    </div>

    <div class="w-full max-w-md z-10">
      <div class="text-center mb-8">
        <h1 class="text-4xl font-bold bg-linear-to-r from-cyan-400 to-blue-300 bg-clip-text text-transparent">加入我们</h1>
        <p class="text-gray-300 mt-2">创建账号，开启您的音乐之旅</p>
      </div>

      <div
        class="bg-gray-900/70 backdrop-blur-xl rounded-2xl shadow-2xl border border-gray-800 p-8 custom-form-container">
        <el-form ref="registerFormRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
          <el-form-item label="用户名" prop="user_name">
            <el-input v-model="form.user_name" placeholder="设置您的用户名" :prefix-icon="User" />
          </el-form-item>

          <el-form-item label="邮箱地址" prop="email">
            <el-input v-model="form.email" placeholder="输入您的邮箱" :prefix-icon="Message" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="设置登录密码" :prefix-icon="Lock" />
            <p class="text-[10px] text-gray-500 mt-1">建议包含字母、数字和符号</p>
          </el-form-item>

          <el-button type="primary" class="w-full mt-4 submit-btn" :loading="loading" @click="handleRegister">
            {{ loading ? '注册中...' : '创建账号' }}
          </el-button>

          <div class="mt-6 pt-4 border-t border-gray-800 text-center">
            <span class="text-gray-400 text-sm">已有账号？</span>
            <el-link type="primary" :underline="false" @click="$router.push('/login')">
              立即登录 →
            </el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>


<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  user_name: '',
  email: '',
  password: ''
})

const rules = reactive<FormRules>({
  user_name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于 8 位', trigger: 'blur' }
  ]
})

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const result = await userStore.register(form)
        if (result.success) {
          ElMessage.success('注册成功！即将跳转登录页')
          setTimeout(() => {
            router.push('/login')
          }, 2000)
        } else {
        }
      } catch (err: any) {
        ElMessage.error('网络请求失败，请稍后再试')
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请完善表单信息')
    }
  })
}
</script>

<style scoped>
:deep(.el-form-item__label) {
  color: #d1d5db !important;
  padding-bottom: 4px !important;
}

:deep(.el-input__wrapper) {
  background-color: rgba(31, 41, 55, 0.5) !important;
  box-shadow: 0 0 0 1px rgba(75, 85, 99, 0.5) inset !important;
  border-radius: 12px;
  padding: 8px 12px;
}

:deep(.el-input__inner) {
  color: white !important;
}

:deep(.el-input__inner::placeholder) {
  color: #6b7280;
}

.submit-btn {
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(to right, #0891b2, #2563eb);
  border: none;
  font-weight: 600;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.3);
  opacity: 0.9;
}
</style>