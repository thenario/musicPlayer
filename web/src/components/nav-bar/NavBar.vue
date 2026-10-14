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
import { reportError } from '@/utils/feedback'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { logout: logoutAction } = useNavBar()
const logout = async () => {
  const result = await logoutAction()
  if (!result.success) return reportError(result)
  ElMessage.success(result.message || '登出成功')
  await router.replace('/login')
}
</script>

<style scoped>
.navbar-menu {
  background-color: var(--music-inset);
  /* gray-800 */
  border-bottom: 1px solid var(--music-border);
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
  flex-grow: 1;
}

.navbar-actions {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 0 16px;
}

.navbar-user {
  display: flex;
  gap: 8px;
  align-items: center;
}

.navbar-avatar {
  color: var(--music-on-primary);
  background: var(--music-primary);
}

.navbar-item-label--logo {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: .08em;
}

.navbar-logout {
  color: var(--el-color-danger);
}

.navbar-register {
  display: flex;
  height: 100%;
  align-items: center;
}
</style>
