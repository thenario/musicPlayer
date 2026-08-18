<template>
    <el-container class="default-layout">
        <!-- 侧边栏，几个页面跳转，下面是用户，写一个dragdown，有logout和个人主页等 -->
        <el-aside class="sidebar" :width="isCollapse ? '48px' : '196px'">
            <el-container class="sidebar-menu-container">
                <el-menu class="sidebar-menu" :collapse="isCollapse" router default-active="home"
                    :collapse-transition="false">
                    <el-button @click="isCollapse = !isCollapse" link>
                        <el-icon>
                            <Expand v-if="isCollapse" />
                            <Fold v-else />
                        </el-icon>
                    </el-button>

                    <el-menu-item class="sidebar-menu-item" index="home">
                        <el-icon class="el-menu-item-icon">
                            <House />
                        </el-icon>
                        <template #title>主页</template>
                    </el-menu-item>

                    <el-menu-item class="sidebar-menu-item" index="playlists">
                        <el-icon>
                            <List />
                        </el-icon>
                        <template #title>歌单</template>

                    </el-menu-item>

                    <el-menu-item class="sidebar-menu-item" index="my-likes">
                        <el-icon>
                            <Star />
                        </el-icon>
                        <template #title>我喜欢</template>
                    </el-menu-item>
                </el-menu>
                <!-- 用户底栏 -->
                <el-dropdown trigger="hover" class="user-dropdown">
                    <div class="el-dropdown-trigger">
                        <el-icon size="16px">
                            <User />
                        </el-icon>
                        <span v-if="!isCollapse" style="color:black;">{{ user ? user.user_name : "无昵称" }}</span>
                    </div>
                </el-dropdown>
            </el-container>


        </el-aside>
        <!-- 内容区域 -->
        <el-main>
            <router-view />
            <PlayerBar/>
            <QueueDrawer/>
            <SongDetail/>
        </el-main>
    </el-container>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { Expand, Fold, House, List, Star, User } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
const userStore = useUserStore()
const { user } = storeToRefs(userStore)
const isCollapse = ref<boolean>(false)


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

:deep(.el-menu--collapse){
    width: 100%;
    box-sizing: border-box;
}

.sidebar-menu-item{
    padding: 0;
    margin: 0px auto 0px -10px;
    width: 100%;
}

.user-dropdown{
    width: 100%;
    height: 20px;
    display: block
}
.el-dropdown-trigger {
    width:100%;
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
}
</style>