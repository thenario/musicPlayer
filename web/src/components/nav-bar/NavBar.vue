<template>
  <!--default-active高亮-->
  <el-menu :default-active="route.path" class="navbar-menu" mode="horizontal" router :ellipsis="false">
    <el-menu-item v-for="item in NAV_ITEMS" :key="item.path" :index="item.path"
      :class="{ 'logo-item': item.isLogo }">
      <span :class="item.isLogo ? 'navbar-item-label--logo' : ''" class="navbar-item-label">{{ item.label }}</span>
    </el-menu-item>

    <div class="navbar-spacer" />

    <div class="navbar-actions">
      <template v-if="userStore.isAuthenticated">
        <el-sub-menu index="user-submenu" class="user-sub">
          <template #title>
            <div class="navbar-user">
              <el-avatar :size="32" class="navbar-avatar" :src="userStore.userCoverUrl">
                {{ userStore.user?.user_name?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.user?.user_name || "用户" }}</span>
            </div>
          </template>
          <el-menu-item index="/user-profile">个人中心</el-menu-item>
          <!-- index 留空：不触发 router 模式跳转，仅执行 @click 登出 -->
          <el-menu-item @click="logout" :index="''" class="logout-item">
            <span class="navbar-logout">退出登录</span>
          </el-menu-item>
        </el-sub-menu>
      </template>

      <template v-else>
        <el-menu-item index="/login">登录</el-menu-item>
        <div class="navbar-register">
          <el-button type="primary" size="small" @click="router.push('/register')">注册</el-button>
        </div>
      </template>
    </div>
  </el-menu>
</template>

<script setup lang="ts">
defineOptions({ name: 'NavBar' })
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { NAV_ITEMS } from './const'
import { useNavBar } from './composables/use-nav-bar'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { logout } = useNavBar()
</script>

<style scoped>
@reference "../../assets/index.css";

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

.navbar-spacer {
  @apply grow;
}

.navbar-actions {
  @apply flex items-center px-4 gap-4;
}

.navbar-user {
  @apply flex items-center gap-2;
}

.navbar-avatar {
  @apply bg-blue-500;
}

.navbar-item-label--logo {
  @apply text-xl font-bold tracking-wider;
}

.navbar-logout {
  @apply text-red-400;
}

.navbar-register {
  @apply flex items-center h-full;
}
</style>
