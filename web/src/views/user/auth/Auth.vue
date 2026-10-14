<template>
    <main class="auth-page">
        <section class="auth-card">
            <aside class="auth-intro">
                <p class="auth-intro__eyebrow">MY MUSIC</p>
                <h1>把喜欢的音乐<br />留在这里。</h1>
                <p>登录后即可同步歌单、播放进度与个人收藏。</p>
            </aside>

            <section class="auth-panel">
                <header class="auth-panel__header">
                    <p class="auth-panel__eyebrow">欢迎来到聆听</p>
                    <h2>{{ isRegister ? '创建账号' : '欢迎回来' }}</h2>
                    <p>{{ isRegister ? '填写信息，开始你的音乐之旅。' : '登录后继续享受你的音乐。' }}</p>
                </header>

                <div class="auth-form-stage">
                    <Transition name="auth-form" mode="out-in">
                        <form v-if="!isRegister" key="login" class="auth-form" @submit.prevent="handleLogin">
                            <label class="auth-field">
                                <span>用户名</span>
                                <input v-model="loginForm.user_name" autocomplete="username" placeholder="请输入用户名" />
                                <small v-if="loginErrors.user_name">{{ loginErrors.user_name }}</small>
                            </label>

                            <label class="auth-field">
                                <span>密码</span>
                                <input v-model="loginForm.password" type="password" autocomplete="current-password"
                                    placeholder="请输入密码" />
                                <small v-if="loginErrors.password">{{ loginErrors.password }}</small>
                            </label>

                            <button class="auth-submit" type="submit" :disabled="loginLoading">
                                {{ loginLoading ? '登录中…' : '登录' }}
                            </button>
                        </form>

                        <form v-else key="register" class="auth-form" @submit.prevent="handleRegister">
                            <label class="auth-field">
                                <span>用户名</span>
                                <input v-model="registerForm.user_name" autocomplete="username" placeholder="设置用户名" />
                                <small v-if="registerErrors.user_name">{{ registerErrors.user_name }}</small>
                            </label>

                            <label class="auth-field">
                                <span>邮箱</span>
                                <input v-model="registerForm.user_email" type="email" autocomplete="email"
                                    placeholder="输入邮箱地址" />
                                <small v-if="registerErrors.user_email">{{ registerErrors.user_email }}</small>
                            </label>

                            <label class="auth-field">
                                <span>密码</span>
                                <input v-model="registerForm.password" type="password" autocomplete="new-password"
                                    placeholder="至少 8 位" />
                                <small v-if="registerErrors.password">{{ registerErrors.password }}</small>
                            </label>

                            <button class="auth-submit" type="submit" :disabled="registerLoading">
                                {{ registerLoading ? '创建中…' : '创建账号' }}
                            </button>
                        </form>
                    </Transition>
                </div>

                <p class="auth-switch">
                    {{ isRegister ? '已有账号？' : '还没有账号？' }}
                    <button type="button" @click="switchMode">
                        {{ isRegister ? '返回登录' : '创建账号' }}
                    </button>
                </p>
            </section>
        </section>
    </main>
</template>

<script setup lang="ts">
defineOptions({ name: 'AuthPage' })

import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePlayerStore } from '@/stores/player'
import { useLogin } from '@/views/user/login/composables/use-login'
import { useRegister } from '@/views/user/register/composables/use-register'
import { reportError } from '@/utils/feedback'
import { logBackgroundFailure } from '@/utils/action-result'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()
const isRegister = computed(() => route.meta.mode === 'register')

const { form: loginForm, errors: loginErrors, loading: loginLoading, login } = useLogin()
const { form: registerForm, errors: registerErrors, loading: registerLoading, register } = useRegister()

const switchMode = () => {
    router.push({ path: isRegister.value ? '/login' : '/register', query: route.query })
}

const handleLogin = async () => {
    const result = await login()
    if (!result.success) {
        reportError(result)
        return
    }
    ElMessage.success('登录成功，欢迎回来！')
    void playerStore.fetchUserQueues().then(logBackgroundFailure)
    void playerStore.fetchCurrentQueue().then(logBackgroundFailure)

    const redirect = route.query.redirect
    router.push(typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/')
}

const handleRegister = async () => {
    const result = await register()
    if (!result.success) {
        reportError(result)
        return
    }

    ElMessage.success('注册成功，请登录')
    router.replace({ path: '/login', query: route.query })
}
</script>

<style scoped>
.auth-form-stage {
    height: 350px;
}

.auth-page {
    box-sizing: border-box;
    min-height: 100vh;
    display: grid;
    place-items: center;
    padding: 24px;
    color: var(--music-text);
    background: var(--music-canvas);
}

.auth-card {
    width: min(920px, 100%);
    min-height: 580px;
    display: grid;
    grid-template-columns: 0.9fr 1.1fr;
    overflow: hidden;
    border: 1px solid var(--music-border);
    border-radius: 12px;
    background: var(--music-surface);
    box-shadow: 0 24px 72px rgb(0 0 0 / 28%);
}

.auth-intro {
    position: relative;
    isolation: isolate;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    padding: 48px;
    color: #261e17;
    background: #b58c66;
}

.auth-intro::before {
    position: absolute;
    z-index: -1;
    top: 36px;
    right: -140px;
    width: 360px;
    height: 360px;
    border: 1px solid #554635;
    border-radius: 50%;
    background: repeating-radial-gradient(circle, #1c1a18 0 2px, #302a23 3px, #171513 4px 5px);
    box-shadow: 0 12px 24px rgb(0 0 0 / 24%);
    content: '';
}

.auth-intro__eyebrow,
.auth-panel__eyebrow {
    margin: 0 0 12px;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.16em;
}

.auth-intro h1 {
    margin: 0;
    font-family: 'Songti SC', SimSun, serif;
    font-size: clamp(30px, 4vw, 42px);
    font-weight: 400;
    line-height: 1.5;
}

.auth-intro>p:last-child {
    max-width: 250px;
    margin: 18px 0 0;
    color: #463628;
    line-height: 1.7;
}

.auth-panel {
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 48px clamp(32px, 7vw, 72px);
    perspective: 800px;
}

.auth-panel__header {
    margin-bottom: 30px;
}

.auth-panel__eyebrow {
    color: var(--music-primary);
}

.auth-panel h2 {
    margin: 0;
    font-size: 30px;
    letter-spacing: -0.04em;
}

.auth-panel__header>p:last-child {
    margin: 10px 0 0;
    color: var(--music-muted);
    font-size: 14px;
}

.auth-form {
    display: grid;
    gap: 18px;
}

.auth-field {
    display: grid;
    gap: 7px;
    color: var(--music-secondary);
    font-size: 13px;
    font-weight: 600;
}

.auth-field input {
    width: 100%;
    box-sizing: border-box;
    border: 1px solid var(--music-border);
    border-radius: 11px;
    padding: 13px 14px;
    color: var(--music-text);
    background: var(--music-inset);
    font: inherit;
    font-weight: 400;
    outline: none;
    transition: border-color 160ms ease, box-shadow 160ms ease;
}

.auth-field input:focus {
    border-color: var(--music-primary);
    box-shadow: 0 0 0 3px rgb(var(--music-primary-rgb) / 14%);
}

.auth-field small {
    color: var(--el-color-danger);
    font-size: 12px;
    font-weight: 400;
}

.auth-submit {
    min-height: 46px;
    margin-top: 6px;
    border: 0;
    border-radius: 10px;
    color: var(--music-on-primary);
    background: var(--music-primary);
    box-shadow: 0 5px 12px rgb(var(--music-primary-rgb) / 16%);
    font: inherit;
    font-weight: 700;
    cursor: pointer;
    transition: background 160ms ease, transform 160ms ease, box-shadow 160ms ease;
}

.auth-submit:hover:not(:disabled) {
    background: var(--music-primary-hover);
    box-shadow: 0 7px 18px rgb(var(--music-primary-rgb) / 24%);
}

.auth-submit:active:not(:disabled) {
    transform: scale(0.98);
}

.auth-submit:disabled {
    cursor: not-allowed;
    opacity: 0.62;
}

.auth-switch {
    margin: 24px 0 0;
    color: var(--music-muted);
    font-size: 13px;
    text-align: center;
}

.auth-switch button {
    border: 0;
    padding: 0;
    color: var(--music-primary);
    background: transparent;
    font: inherit;
    font-weight: 700;
    cursor: pointer;
    text-underline-offset: 4px;
}

.auth-switch button:hover {
    text-decoration: underline;
}

.auth-form-enter-active,
.auth-form-leave-active {
    transition: opacity 180ms ease, transform 220ms ease;
    transform-style: preserve-3d;
}

.auth-form-enter-from {
    opacity: 0;
    transform: rotateY(-22deg) translateX(18px);
}

.auth-form-leave-to {
    opacity: 0;
    transform: rotateY(22deg) translateX(-18px);
}

@media (max-width: 720px) {
    .auth-page {
        padding: 16px;
    }

    .auth-card {
        min-height: 0;
        grid-template-columns: 1fr;
    }

    .auth-intro {
        min-height: 160px;
        padding: 28px;
    }

    .auth-intro::before {
        width: 180px;
        height: 180px;
        top: -90px;
        right: -30px;
        opacity: 0.18;
        box-shadow: none;
    }

    .auth-intro h1 {
        font-size: 28px;
    }

    .auth-intro>p:last-child {
        display: none;
    }

    .auth-panel {
        padding: 36px 28px;
    }

    .auth-form-stage {
        height: auto;
        min-height: 260px;
    }
}
</style>
