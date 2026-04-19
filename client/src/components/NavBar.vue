<template>
  <el-menu :default-active="route.path" class="navbar-menu" mode="horizontal" router :ellipsis="false">
    <el-menu-item index="/" class="logo-item">
      <span class="text-xl font-bold tracking-wider">音乐播放器</span>
    </el-menu-item>

    <el-menu-item index="/songs">歌曲库</el-menu-item>
    <el-menu-item index="/playlists">歌单</el-menu-item>

    <div class="grow" />

    <div class="flex items-center px-4 gap-4">
      <template v-if="userStore.isAuthenticated">
        <el-sub-menu index="user-submenu" class="user-sub">
          <template #title>
            <div class="flex items-center gap-2">
              <el-avatar :size="32" class="bg-blue-500" :src="userStore.user?.user_cover">
                {{ userStore.user?.user_name?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.user?.user_name || "用户" }}</span>
            </div>
          </template>
          <el-menu-item index="/userProfile">个人中心</el-menu-item>
          <el-menu-item @click="logout" class="logout-item">
            <span class="text-red-400">退出登录</span>
          </el-menu-item>
        </el-sub-menu>
      </template>

      <template v-else>
        <el-menu-item index="/login">登录</el-menu-item>
        <div class="flex items-center h-full">
          <el-button type="primary" size="small" @click="router.push('/register')">注册</el-button>
        </div>
      </template>
    </div>
  </el-menu>
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

.navbar-menu {
  background-color: #1f2937;
  /* gray-800 */
  border-bottom: 1px solid #374151;
  /* gray-700 */
  align-items: center;
  padding: 0 20px;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: #d1d5db !important;
  /* gray-300 */
}

:deep(.el-menu-item:hover),
:deep(.el-menu-item.is-active) {
  background-color: transparent !important;
  color: #ffffff !important;
}

.el-menu--horizontal {
  border-bottom: none;
}

.flex-grow {
  flex-grow: 1;
}

.logo-item {
  opacity: 1 !important;
  cursor: pointer;
}
</style>