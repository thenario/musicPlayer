<template>
  <el-menu :default-active="activePath" mode="horizontal" class="navbar-container" :ellipsis="false" router>
    <div class="logo-section" @click="router.push('/')">
      <span class="text-xl font-bold text-white cursor-pointer">音乐播放器</span>
    </div>

    <el-menu-item index="/songs">歌曲库</el-menu-item>
    <el-menu-item index="/playlists">歌单</el-menu-item>

    <div class="grow" />

    <div class="flex items-center gap-4 px-5">

      <template v-if="userStore.isAuthenticated">
        <el-button type="success" :icon="Upload" round @click="router.push('/upload')">
          上传歌曲
        </el-button>

        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-profile flex items-center cursor-pointer">
            <el-avatar :size="32" class="mr-2">{{ userStore.user?.user_name?.charAt(0) }}</el-avatar>
            <span class="text-white text-sm">
              {{ userStore.user ? userStore.user.user_name : "未知昵称" }}
            </span>
            <el-icon class="el-icon--right text-white">
              <ArrowDown />
            </el-icon>
          </div>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item :icon="User">个人中心</el-dropdown-item>
              <el-dropdown-item :icon="SwitchButton" command="logout" divided>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>

      <div v-else class="auth-buttons">
        <el-button link @click="router.push('/login')">登录</el-button>
        <el-button type="primary" @click="router.push('/register')">注册</el-button>
      </div>
    </div>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter, useRoute } from 'vue-router'

import { Upload, ArrowDown, SwitchButton, User } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const activePath = computed(() => route.path)

const handleCommand = (command: string) => {
  if (command === 'logout') {
    logout()
  }
}

const logout = async () => {
  if (!userStore.user) return
  await userStore.logout(userStore.user.user_id)
  router.push('/login')
}
</script>

<style scoped>
.navbar-container {
  @apply bg-gray-800 border-none px-4 items-center h-16;
  --el-menu-bg-color: #1f2937;
  --el-menu-text-color: #d1d5db;
  --el-menu-active-color: #ffffff;
  --el-menu-hover-bg-color: #374151;
}

.logo-section {
  @apply flex items-center h-full mr-8 outline-none;
}

.flex-grow {
  flex-grow: 1;
}

:deep(.el-menu--horizontal .el-menu-item.is-active) {
  border-bottom: 2px solid #3b82f6 !important;
  background-color: transparent !important;
}

.user-profile:hover {
  opacity: 0.8;
}
</style>