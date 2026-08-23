<template>
    <el-container class="default-layout">
        <!-- 侧边栏，几个页面跳转，下面是用户，写一个dropdown，有logout和个人主页等 -->
        <el-aside class="sidebar" :width="isCollapse ? '48px' : '196px'" @transitionend="handleSidebarTransitionEnd">
            <el-container class="sidebar-menu-container">
                <el-menu class="sidebar-menu" :collapse="isCollapse" router :default-active="route.path"
                    :collapse-transition="false">
                    <div class="sidebar-header"
                        style="width: 100%; display: flex; flex-direction: row; justify-content:space-between; align-items: center;">
                        <Transition name="brand-fade">
                            <div v-if="showBrand" class="sidebar-brand">
                                <span class="sidebar-title">聆听</span>
                                <span class="current-song-title">
                                    {{ playerStore.currentSong?.song_title || '暂未播放' }}
                                </span>
                            </div>
                        </Transition>
                        <!-- <div class="music-bars">
                                <span class="music-bar" v-for="(bar, index) in visualizerBars" :key="index"
                                    :style="{ height: `${bar}%` }"></span>
                            </div> -->

                        <div v-if="isCollapse">
                            <el-tooltip content="展开" placement="right">
                                <el-button class="collapse-button" @click="toggleSidebar" link>
                                    <el-icon size="18px">
                                        <Expand />
                                    </el-icon>
                                </el-button>
                            </el-tooltip>
                        </div>
                        <div v-else-if="!isCollapse">
                            <el-tooltip content="折叠" placement="right">
                                <el-button class="collapse-button" v-if="!isCollapse" @click="toggleSidebar" link>
                                    <el-icon size="18px">
                                        <Fold />
                                    </el-icon>
                                </el-button>
                            </el-tooltip>
                        </div>
                    </div>
                    <el-menu-item class="sidebar-menu-item" index="/home">
                        <el-icon class="el-menu-item-icon">
                            <House />
                        </el-icon>
                        <template #title>主页</template>
                    </el-menu-item>

                    <el-menu-item class="sidebar-menu-item" index="/playlists">
                        <el-icon>
                            <List />
                        </el-icon>
                        <template #title>歌单</template>

                    </el-menu-item>

                    <el-menu-item class="sidebar-menu-item" index="/songs" disabled>
                        <el-icon>
                            <Star />
                        </el-icon>
                        <template #title>我喜欢</template>
                    </el-menu-item>
                </el-menu>
                <!-- 用户底栏 -->
                <el-dropdown trigger="hover" class="user-dropdown" @command="handleCommand">
                    <div class="el-dropdown-trigger">
                        <el-icon size="16px">
                            <User />
                        </el-icon>
                        <span v-if="!isCollapse" style="color:black;">{{ user ? user.user_name : "无昵称" }}</span>
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
                            <el-dropdown-item command="logout">
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
            <SongDetail />
        </el-main>
    </el-container>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { Document, Expand, Fold, House, List, Star, SwitchButton, User } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
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
            }
            else {
                ElMessage.error("登出失败，请稍后重试")
            }
            return
        }
        case "user-profile":
            { router.push({ name: 'user-profile' }) }
    }
}


</script>
<style scoped>
.default-layout {
    height: 100%;
    background: rgb(157, 128, 128);
}

.sidebar {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    background-color: rgb(225, 229, 232);
    transition: width 0.3s ease-in-out
}

.sidebar-header {
    width: 100%;
    height: 48px;
    padding: 2px 0 2px 6px;
}

.sidebar-brand {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
    padding: 6px 4px;
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
    color: #30245c;
    font-size: 17px;
    font-weight: 700;
    letter-spacing: 0.12em;
    line-height: 1.2;
}

.current-song-title {
    overflow: hidden;
    color: #8b86a0;
    font-size: 12px;
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
    background: #7c5ce0;
    transition: height 80ms linear;
}

.collapse-button:hover {
    transform: scale(0.9);
}

.collapse-button {
    transform: tranform 0.2s ease-in out;
}

.sidebar-menu-container {
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.sidebar-menu {
    width: 100%;
    display: flex;
    flex: 1;
    flex-direction: column;
    justify-content: flex-start;
}

:deep(.el-menu--collapse) {
    width: 100%;
    box-sizing: border-box;
}

.sidebar-menu-item {
    padding: 0;
    margin: 0px auto 0px -10px;
    width: 100%;
}

.user-dropdown {
    width: 100%;
    height: 20px;
    display: block;
    outline: none;
    cursor: pointer;
}

.el-dropdown-trigger {
    width: 100%;
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
}

.el-main-content{
    padding: 0 0 0 0;
}
</style>
