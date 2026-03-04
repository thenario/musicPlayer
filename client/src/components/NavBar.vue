<template>
  <nav class="bg-gray-800 shadow-lg">
    <div class="container mx-auto px-4">
      <div class="flex justify-between items-center h-16">
        <!-- 左侧 Logo 和导航链接 -->
        <div class="flex items-center space-x-8">
          <router-link to="/" class="text-xl font-bold text-white">
            音乐播放器
          </router-link>

          <div class="hidden md:flex space-x-4">
            <router-link to="/songs" class="text-gray-300 hover:text-white px-3 py-2 rounded-md">
              歌曲库
            </router-link>
            <router-link to="/playlists" class="text-gray-300 hover:text-white px-3 py-2 rounded-md">
              歌单
            </router-link>
          </div>
        </div>

        <!-- 右侧用户菜单 -->
        <div class="flex items-center space-x-4">
          <!-- 上传歌曲按钮 (带图标和背景色) -->
          <router-link v-if="userStore.isAuthenticated" to="/upload"
            class="flex items-center px-3 py-2 bg-green-600 hover:bg-green-700 text-white rounded-md transition-colors text-sm font-medium shadow-md">
            <!-- 上传图标 -->
            <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path>
            </svg>
            上传歌曲
          </router-link>

          <div v-if="userStore.isAuthenticated" class="relative">
            <button @click="toggleUserMenu" class="flex items-center text-sm text-white focus:outline-none">
              {{ userStore.user ? userStore.user.user_name : "未知昵称" }}
              <svg class="ml-1 w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>

            <div v-if="showUserMenu" class="absolute right-0 mt-2 w-48 bg-gray-700 rounded-md shadow-lg py-1 z-50">
              <button @click="logout" class="block w-full text-left px-4 py-2 text-sm text-gray-300 hover:bg-gray-600">
                退出登录
              </button>
            </div>
          </div>

          <div v-else class="flex space-x-2">
            <router-link to="/login" class="text-gray-300 hover:text-white px-3 py-2">
              登录
            </router-link>
            <router-link to="/register" class="text-gray-300 hover:bg-blue-600 hover:text-white px-4 py-2 rounded-md">
              注册
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const showUserMenu = ref(false)

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const logout = async () => {
  if (!userStore.user) return
  await userStore.logout(userStore.user.user_id)
  showUserMenu.value = false
  router.push('/')
}
</script>