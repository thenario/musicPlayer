<template>
    <el-container class="default-layout">
        <!-- 侧边栏，几个页面跳转，下面是用户，写一个dropdown，有logout和个人主页等 -->
        <el-aside class="sidebar" :width="isCollapse ? '64px' : '224px'" @transitionend="handleSidebarTransitionEnd">
            <el-container class="sidebar-menu-container">
                <el-menu class="sidebar-menu" :collapse="isCollapse" router :default-active="route.path"
                    :collapse-transition="false">
                    <div class="sidebar-header">
                        <Transition name="brand-fade">
                            <router-link v-if="showBrand" to="/home" class="sidebar-brand">
                                <span class="sidebar-title">聆听</span>
                                <span class="current-song-title">
                                    {{ playerStore.currentSong?.song_title || '暂未播放' }}
                                </span>
                            </router-link>
                        </Transition>
                        <!-- <div class="music-bars">
                                <span class="music-bar" v-for="(bar, index) in visualizerBars" :key="index"
                                    :style="{ height: `${bar}%` }"></span>
                            </div> -->

                        <div v-if="isCollapse">
                            <el-tooltip content="展开" placement="right">
                                <el-button class="collapse-button" aria-label="展开侧栏" @click="toggleSidebar" link>
                                    <el-icon size="18px">
                                        <Expand />
                                    </el-icon>
                                </el-button>
                            </el-tooltip>
                        </div>
                        <div v-else-if="!isCollapse">
                            <el-tooltip content="折叠" placement="right">
                                <el-button class="collapse-button" aria-label="折叠侧栏" v-if="!isCollapse" @click="toggleSidebar" link>
                                    <el-icon size="18px">
                                        <Fold />
                                    </el-icon>
                                </el-button>
                            </el-tooltip>
                        </div>
                    </div>
                    <div class="sidebar-section-label" aria-hidden="true">音乐空间 <span>LIBRARY</span></div>
                    <el-menu-item class="sidebar-menu-item" index="/songs" aria-label="歌曲库">
                        <el-icon class="el-menu-item-icon">
                            <House />
                        </el-icon>
                        <template #title>歌曲库</template>
                    </el-menu-item>

                    <el-menu-item class="sidebar-menu-item" index="/playlists" aria-label="歌单">
                        <el-icon>
                            <List />
                        </el-icon>
                        <template #title>歌单</template>

                    </el-menu-item>

                    <el-menu-item class="sidebar-menu-item" index="/history" aria-label="播放历史">
                        <el-icon>
                            <Clock />
                        </el-icon>
                        <template #title>我听过</template>
                    </el-menu-item>
                </el-menu>
                <!-- 用户底栏 -->
                <el-dropdown trigger="hover" class="user-dropdown" @command="handleCommand">
                    <div class="el-dropdown-trigger">
                        <el-icon size="16px">
                            <User />
                        </el-icon>
                        <span v-if="!isCollapse">{{ user ? user.user_name : "未登录" }}</span>
                    </div>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="user-profile">
                                <el-tooltip :disabled="!isCollapse" content="个人资料" placement="right">
                                    <div>
                                        <el-icon>
                                            <Document />
                                        </el-icon>
                                        <span v-if="!isCollapse">
                                            个人资料
                                        </span>
                                    </div>
                                </el-tooltip>

                            </el-dropdown-item>
                            <el-dropdown-item command="logout" v-if="user">
                                <el-tooltip :disabled="!isCollapse" content="登出" placement="right">
                                    <div>
                                        <el-icon>
                                            <SwitchButton />
                                        </el-icon>
                                        <span v-if="!isCollapse">
                                            登出
                                        </span>
                                    </div>
                                </el-tooltip>
                            </el-dropdown-item>
                            <el-dropdown-item command="login" v-else-if="!user">
                                <el-tooltip :disabled="!isCollapse" content="登录" placement="right">
                                    <div>
                                        <el-icon>
                                            <Lock />
                                        </el-icon>
                                        <span v-if="!isCollapse">
                                            登录
                                        </span>
                                    </div>
                                </el-tooltip>
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </el-container>


        </el-aside>
        <!-- 内容区域 -->
        <el-main class="el-main-content">
            <router-view />
            <PlayerBar />
            <QueueDrawer />
        </el-main>
    </el-container>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { Clock, Document, Expand, Fold, House, List, Lock, SwitchButton, User } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { reportError } from '@/utils/feedback';
import { usePlayerStore } from '@/stores/player'
import { storeToRefs } from 'pinia';
import { useRoute, useRouter } from 'vue-router';
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const playerStore = usePlayerStore()
const { user } = storeToRefs(userStore)
const isCollapse = ref<boolean>(false)
const showBrand = ref<boolean>(true)

const toggleSidebar = () => {
    if (isCollapse.value) {
        isCollapse.value = false
    } else {
        showBrand.value = false
        isCollapse.value = true
    }
}

const handleSidebarTransitionEnd = (event: TransitionEvent) => {
    if (
        event.target === event.currentTarget &&
        event.propertyName === 'width' &&
        !isCollapse.value
    ) {
        showBrand.value = true
    }
}

const handleCommand = async (command: string) => {
    switch (command) {
        case "logout": {
            const res = await userStore.logout()
            if (res.success) {
                ElMessage.success("登出成功")
                await router.replace('/login')
            }
            else {
                reportError(res)
            }
            return
        }
        case "user-profile":
            {
                router.push({ name: 'user-profile' })
                return
            }
        case "login": {
            router.push({ path: '/login' })
            return
        }
    }
}


</script>
<style scoped>
.default-layout {
    height: 100%;
    min-height: 0;
    overflow: hidden;
    background: var(--music-canvas);
}

.sidebar {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    flex-shrink: 0;
    overflow: hidden;
    border-right: 1px solid var(--music-border);
    background: var(--music-inset);
    transition: width 0.3s ease-in-out;
}

.sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    min-height: 112px;
    height: 112px;
    gap: 8px;
    padding: 24px 18px;
    box-sizing: border-box;
    border-bottom: 1px solid var(--music-border);
    margin-bottom: 24px;
}

.sidebar-brand {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
    padding: 6px 8px;
    text-decoration: none;
}

.brand-fade-enter-active {
    transition:
        opacity 180ms ease-out,
        transform 180ms ease-out;
}

.brand-fade-enter-from {
    opacity: 0;
    transform: translateX(-8px);
}

.brand-fade-enter-to {
    opacity: 1;
    transform: translateX(0);
}

.brand-fade-leave-active {
    transition:
        opacity 100ms ease-in,
        transform 100ms ease-in;
}

.brand-fade-leave-to {
    opacity: 0;
    transform: translateX(-6px);
}

.sidebar-title {
    color: var(--music-primary);
    font-family: 'Songti SC', SimSun, serif;
    font-size: 29px;
    font-weight: 400;
    letter-spacing: 0.18em;
    line-height: 1.2;
}

.current-song-title {
    overflow: hidden;
    color: var(--music-muted);
    max-width: 120px;
    font-size: 10px;
    line-height: 1.4;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.music-bars {
    height: 20px;
    display: flex;
    align-items: flex-end;
    gap: 3px;
}

.music-bar {
    display: block;
    width: 3px;
    min-height: 4px;
    border-radius: 999px;
    background: var(--music-primary);
    transition: height 80ms linear;
}

.collapse-button:hover {
    color: var(--music-primary);
    background: var(--music-primary-soft);
}

.collapse-button {
    flex-shrink: 0;
    width: 28px;
    height: 30px;
    color: var(--music-muted);
    border-radius: 8px;
    transition: color 160ms ease, background 160ms ease;
}

.sidebar-menu-container {
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-height: 0;
}

.sidebar-menu {
    width: 100%;
    display: flex;
    flex: 1;
    flex-direction: column;
    justify-content: flex-start;
    border-right: 0;
    background: transparent;
    --el-menu-item-height: 48px;
    --el-menu-text-color: var(--music-secondary);
    --el-menu-active-color: var(--music-primary);
    --el-menu-hover-bg-color: var(--music-hover);
}

.sidebar-section-label {
    display: flex;
    justify-content: space-between;
    padding: 0 26px 14px;
    color: var(--music-muted);
    font-size: 10px;
    letter-spacing: 0.08em;
    white-space: nowrap;
}

.sidebar-section-label span {
    font-size: 8px;
    opacity: 0.65;
}

:deep(.sidebar-menu.el-menu--collapse .sidebar-section-label) {
    height: 14px;
    padding: 0;
    visibility: hidden;
}

:deep(.el-menu--collapse) {
    width: 100%;
    box-sizing: border-box;
}

.sidebar-menu-item {
    position: relative;
    width: calc(100% - 28px);
    margin: 4px 14px;
    padding: 0 14px !important;
    gap: 8px;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 500;
    transition: background 160ms ease, color 160ms ease;
}

.sidebar-menu-item.is-active {
    color: var(--music-primary);
    background: var(--music-primary-soft);
    font-weight: 650;
}

.sidebar-menu-item.is-active::before {
    position: absolute;
    top: 15px;
    bottom: 15px;
    left: -10px;
    width: 3px;
    border-radius: 0 3px 3px 0;
    background: var(--music-primary);
    content: '';
}

:deep(.sidebar-menu.el-menu--collapse .el-menu-item) {
    width: calc(100% - 20px);
    margin-inline: 10px;
    padding: 0 !important;
    justify-content: center;
}

:deep(.sidebar-menu.el-menu--collapse .el-menu-tooltip__trigger) {
    padding: 0;
    justify-content: center;
}

:deep(.sidebar-menu.el-menu--collapse .el-menu-item .el-icon) {
    margin: 0;
}

:deep(.sidebar-menu.el-menu--collapse .sidebar-header) {
    justify-content: center !important;
    padding-inline: 8px;
}

.user-dropdown {
    width: 100%;
    min-height: 64px;
    display: block;
    outline: none;
    cursor: pointer;
    border-top: 1px solid var(--music-border);
    background: var(--music-inset);
}

.el-dropdown-trigger {
    width: 100%;
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    min-height: 64px;
    gap: 10px;
    color: var(--music-secondary);
    font-size: 13px;
    transition: background 160ms ease;
}

.el-dropdown-trigger:hover {
    background: var(--music-primary-soft);
}

.el-main-content {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-width: 0;
  min-height: 0;
  padding: 0;
  overflow: hidden;
}

.el-main-content > :first-child {
  flex: 1 1 0%;
  min-height: 0;
  min-width: 0;
}

@media (max-width: 600px) {
  /* 不改变折叠状态，只用 CSS 将导航收成窄栏。 */
  .sidebar {
    width: 64px !important;
  }

  .sidebar-header {
    height: 72px;
    min-height: 72px;
    gap: 0;
    justify-content: center !important;
    padding-inline: 8px;
  }

  .sidebar-header::before {
    display: grid;
    width: 34px;
    height: 34px;
    place-items: center;
    border-radius: 11px;
    color: var(--music-primary);
    background: var(--music-primary-soft);
    font-size: 19px;
    font-weight: 700;
    content: '聆';
  }

  .sidebar-brand,
  .sidebar-section-label,
  .collapse-button,
  .sidebar-menu-item :deep(span),
  .el-dropdown-trigger > span {
    display: none;
  }

  .sidebar-menu-item {
    width: 44px;
    margin-inline: 10px;
    padding: 0 !important;
    justify-content: center;
    font-size: 0;
  }

  .sidebar-menu-item :deep(.el-icon) {
    margin: 0;
    font-size: 19px;
  }
}
</style>
