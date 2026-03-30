<template>
  <nav class="navbar-container">
    <div class="logo-section" @click="router.push('/')">
      <router-link to="/songs" class="text-xl font-bold text-white cursor-pointer tracking-wider nav-link"
        :class="{ 'nav-link-active': route.path === '/' }">音乐播放器</router-link>
    </div>

    <div class="flex items-center gap-6">
      <router-link to="/songs" class="nav-link" :class="{ 'nav-link-active': route.path === '/songs' }">
        歌曲库
      </router-link>
      <router-link to="/playlists" class="nav-link" :class="{ 'nav-link-active': route.path === '/playlists' }">
        歌单
      </router-link>
    </div>

    <div class="grow" />

    <div class="flex items-center gap-4 px-5">
      <template v-if="userStore.isAuthenticated">
        <button @click="router.push('/upload')"
          class="flex items-center gap-2 bg-green-600 hover:bg-green-700 text-white px-4 py-1.5 rounded-full text-sm transition-colors">
          <el-icon>
            <Upload />
          </el-icon>
          <span>上传歌曲</span>
        </button>

        <div class="group relative py-2">
          <div class="flex items-center gap-2 cursor-pointer">
            <div class="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white text-xs">
              {{ userStore.user?.user_name?.charAt(0) || 'U' }}
            </div>
            <span class="text-gray-200 text-sm group-hover:text-white transition-colors">
              {{ userStore.user?.user_name || "用户" }}
            </span>
          </div>

          <div
            class="absolute right-0 top-full w-32 bg-gray-800 border border-gray-700 rounded shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-50">
            <button @click="router.push('/profile')"
              class="w-full text-left px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 hover:text-white transition-colors">
              个人中心
            </button>
            <button @click="logout"
              class="w-full text-left px-4 py-2 text-sm text-red-400 hover:bg-gray-700 transition-colors border-t border-gray-700">
              退出登录
            </button>
          </div>
        </div>
      </template>

      <div v-else class="flex items-center gap-3">
        <button @click="router.push('/login')" class="text-gray-300 hover:text-white text-sm transition-colors">
          登录
        </button>
        <button @click="router.push('/register')"
          class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-1.5 rounded text-sm transition-colors">
          注册
        </button>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useUserStore } from '../stores/user'
import { useRouter, useRoute } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const logout = async () => {
  if (!userStore.user) return
  const res = await userStore.logout(userStore.user.user_id)
  if (!res.success) return ElMessage.error("登出失败")
  ElMessage.success("登出成功")
  router.push('/login')
}
</script>

<style scoped>
@reference "../assets/index.css";

.navbar-container {
  @apply flex items-center h-16 bg-gray-900 px-6 border-b border-gray-800 sticky top-0 z-40;
}

.logo-section {
  @apply flex items-center mr-10 shrink-0;
}

.nav-link {
  @apply text-gray-400 hover:text-white text-sm font-medium transition-all relative py-5;
}

.nav-link::after {
  content: '';
  @apply absolute bottom-0 left-0 w-full h-0.5 bg-blue-500 scale-x-0 transition-transform duration-300;
}

.nav-link:hover::after,
.nav-link-active::after {
  @apply scale-x-100;
}

.nav-link-active {
  @apply text-white;
}
</style>