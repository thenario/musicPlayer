<template>
  <AuthPageLayout bg-class="bg-slate-950 text-white" gradient-class="bg-linear-to-tr from-blue-950 via-indigo-900 to-pink-900">
    <template #header>
      <h1 class="register-title">加入我们
      </h1>
      <p class="register-subtitle">创建账号，开启您的音乐之旅</p>
    </template>

    <form @submit.prevent="handleRegister" class="register-form">
      <div>
        <label class="register-form__label">用户名</label>
        <input v-model="form.user_name" type="text" placeholder="设置您的用户名"
          class="register-form__input">
        <p v-if="errors.user_name" class="register-form__error">{{ errors.user_name }}</p>
      </div>

      <div>
        <label class="register-form__label">邮箱地址</label>
        <input v-model="form.user_email" type="email" placeholder="输入您的邮箱"
          class="register-form__input">
        <p v-if="errors.user_email" class="register-form__error">{{ errors.user_email }}</p>
      </div>

      <div>
        <label class="register-form__label">密码</label>
        <input v-model="form.password" type="password" placeholder="设置登录密码"
          class="register-form__input">
        <p v-if="errors.password" class="register-form__error">{{ errors.password }}</p>
        <p v-else class="register-form__hint">建议包含字母、数字和符号</p>
      </div>

      <button type="submit" :disabled="loading"
        class="register-submit">
        <span v-if="loading">注册中...</span>
        <span v-else>创建账号</span>
      </button>

      <div class="register-footer">
        <span class="register-footer__text">已有账号？</span>
        <button type="button" @click="router.push('/login')" class="register-footer__link">
          立即登录 →
        </button>
      </div>
    </form>
  </AuthPageLayout>
</template>

<script setup lang="ts">
defineOptions({ name: 'RegisterPage' })
import { onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AuthPageLayout from '@/views/user/components/AuthPageLayout.vue'
import { useRegister } from './composables/use-register'

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

<style scoped>
@reference "../../../assets/index.css";

.register-title {
  @apply text-4xl font-bold bg-linear-to-r from-cyan-400 to-blue-300 bg-clip-text text-transparent;
}

.register-subtitle {
  @apply text-gray-300 mt-2;
}

.register-form {
  @apply space-y-5;
}

.register-form__label {
  @apply block text-sm font-medium text-gray-300 mb-1;
}

.register-form__input {
  @apply w-full bg-gray-800/50 border border-gray-700 rounded-xl px-4 py-3 text-white transition-all;
}

.register-form__input:focus {
  @apply outline-none ring-2 ring-blue-500;
}

.register-form__error {
  @apply text-red-400 text-xs mt-1;
}

.register-form__hint {
  @apply text-[10px] text-gray-500 mt-1;
}

.register-submit {
  @apply w-full h-12 mt-4 bg-linear-to-r from-cyan-600 to-blue-600 text-white font-bold rounded-xl shadow-lg transition-all;
}

.register-submit:hover {
  @apply from-cyan-500 to-blue-500;
}

.register-submit:active {
  @apply scale-95;
}

.register-submit:disabled {
  @apply opacity-50 cursor-not-allowed;
}

.register-footer {
  @apply mt-6 pt-4 border-t border-gray-800 text-center;
}

.register-footer__text {
  @apply text-gray-400 text-sm;
}

.register-footer__link {
  @apply text-blue-400 text-sm ml-1;
}

.register-footer__link:hover {
  @apply underline;
}
</style>
