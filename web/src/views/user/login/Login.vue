<template>
  <AuthPageLayout bg-class="bg-gray-950 text-white" gradient-class="bg-linear-to-br from-gray-900 via-purple-900 to-blue-900">
    <template #header>
      <div
        class="login-logo">
        <svg xmlns="http://www.w3.org/2000/svg" class="login-logo__svg" viewBox="0 0 24 24" fill="currentColor">
          <path
            d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
        </svg>
      </div>
      <h1 class="login-title">欢迎回来
      </h1>
      <p class="login-subtitle">登录您的音乐空间，发现更多精彩</p>
    </template>

    <form @submit.prevent="handleLogin" class="login-form">
      <div class="login-form__field">
        <label class="login-form__label">用户名</label>
        <div class="login-form__input-wrap">
          <input v-model="form.user_name" type="text" placeholder="请输入用户名"
            class="login-form__input">
        </div>
        <p v-if="errors.user_name" class="login-form__error">{{ errors.user_name }}</p>
      </div>

      <div class="login-form__field">
        <label class="login-form__label">密码</label>
        <div class="login-form__input-wrap">
          <input v-model="form.password" type="password" placeholder="请输入密码"
            class="login-form__input">
        </div>
        <p v-if="errors.password" class="login-form__error">{{ errors.password }}</p>
      </div>

      <button type="submit" :disabled="loading"
        class="login-submit">
        <span v-if="loading">登录中...</span>
        <span v-else>立即登录</span>
      </button>

      <div class="login-footer">
        <span class="login-footer__text">还没有账号？</span>
        <button type="button" @click="router.push('/register')"
          class="login-footer__link">
          立即注册 →
        </button>
      </div>
    </form>

    <template #footer>
      <p class="login-copyright">© 2026 音乐播放器 · 享受您的音乐时光</p>
    </template>
  </AuthPageLayout>
</template>

<script setup lang="ts">
defineOptions({ name: 'LoginPage' })
import { useRoute, useRouter } from 'vue-router'
import { usePlayerStore } from '@/stores/player'
import AuthPageLayout from '@/views/user/components/AuthPageLayout.vue'
import { useLogin } from './composables/use-login'

const playStore = usePlayerStore()
const router = useRouter()
const route = useRoute()
const { form, errors, loading, login } = useLogin()

const handleLogin = async () => {
  const result = await login()
  if (result.success) {
    ElMessage.success("登录成功，欢迎回来！")
    playStore.fetchUserQueues()
    playStore.fetchCurrentQueue()
    const redirect = route.query.redirect
    router.push(typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/')
  }
}
</script>

<style scoped>
@reference "../../../assets/index.css";

.login-logo {
  @apply inline-flex items-center justify-center w-16 h-16 bg-linear-to-r from-blue-500 to-purple-600 rounded-2xl shadow-lg mb-4;
}

.login-logo__svg {
  @apply w-8 h-8 text-white;
}

.login-title {
  @apply text-4xl font-bold bg-linear-to-r from-blue-400 to-purple-300 bg-clip-text text-transparent;
}

.login-subtitle {
  @apply text-gray-300 mt-2;
}

.login-form {
  @apply space-y-6;
}

.login-form__field {
  @apply space-y-2;
}

.login-form__label {
  @apply block text-sm font-medium text-gray-300 ml-1;
}

.login-form__input-wrap {
  @apply relative;
}

.login-form__input {
  @apply w-full bg-gray-800/50 border border-gray-700/50 rounded-xl px-4 py-3 text-white transition-all;
}

.login-form__input:focus {
  @apply outline-none ring-2 ring-indigo-500;
}

.login-form__input-wrap:hover .login-form__input {
  @apply bg-gray-800/80;
}

.login-form__error {
  @apply text-red-400 text-xs mt-1 ml-1;
}

.login-submit {
  @apply w-full h-12 mt-2 bg-linear-to-r from-blue-600 to-purple-600 text-white font-bold rounded-xl shadow-lg transition-all;
}

.login-submit:hover {
  @apply from-blue-500 to-purple-500;
}

.login-submit:active {
  @apply scale-95;
}

.login-submit:disabled {
  @apply opacity-50;
}

.login-footer {
  @apply mt-8 pt-6 border-t border-gray-800 text-center;
}

.login-footer__text {
  @apply text-gray-400 text-sm;
}

.login-footer__link {
  @apply ml-1 text-sm font-semibold bg-linear-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent transition-opacity;
}

.login-footer__link:hover {
  @apply opacity-80;
}

.login-copyright {
  @apply text-gray-500 text-sm;
}
</style>
