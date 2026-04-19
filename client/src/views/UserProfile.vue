<template>
    <div class="min-h-[80vh] flex items-center justify-center p-4">
        <el-card class="w-full max-w-md border-none overflow-hidden relative profile-card"
            :body-style="{ padding: '0px' }">
            <div class="h-32 bg-linear-to-r from-indigo-500 via-purple-500 to-pink-500"></div>

            <div class="relative px-6 pb-10 flex flex-col items-center">
                <div class="relative -mt-16 mb-4">
                    <div class="p-1 bg-white rounded-full shadow-xl">
                        <el-avatar :size="110" :src="userCoverUrl" class="bg-gray-100 text-3xl font-bold hover-scale">
                            {{ user?.user_name?.charAt(0).toUpperCase() || 'U' }}
                        </el-avatar>
                    </div>
                </div>

                <!-- 用户信息 -->
                <div class="text-center mb-8">
                    <h1 class="text-2xl font-extrabold text-gray-800 tracking-tight">
                        {{ user?.user_name || '未登录用户' }}
                    </h1>
                    <p class="text-gray-400 text-sm mt-1 font-medium">Music Enthusiast</p>
                </div>

                <div class="flex flex-col gap-3 w-full px-4">
                    <el-button type="primary" size="large" class="main-btn group" @click="goToMyUploadSongs">
                        <el-icon class="mr-2 group-hover:rotate-12 transition-transform">
                            <Collection />
                        </el-icon>
                        我上传的音乐
                    </el-button>

                    <el-button size="large" plain class="edit-btn" @click="goToEdit">
                        <el-icon class="mr-2">
                            <Setting />
                        </el-icon>
                        账号设置
                    </el-button>
                </div>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import { Setting, Collection } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();

const { user, userCoverUrl } = storeToRefs(userStore);

const goToEdit = () => {
    router.push({ path: '/editUserProfile' });
};

const goToMyUploadSongs = () => {
    router.push({ path: '/userUploadSongs' });
};
</script>

<style scoped>
.profile-card {
    border-radius: 24px;
    transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
    box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.1);
}

.profile-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.15);
}

.hover-scale {
    transition: transform 0.3s ease;
    cursor: pointer;
}

.hover-scale:hover {
    transform: scale(1.05);
}

/* 主按钮样式定制 */
.main-btn {
    height: 50px;
    border-radius: 14px;
    background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
    border: none;
    font-weight: 600;
    font-size: 1rem;
}

.main-btn:hover {
    opacity: 0.9;
    transform: scale(1.02);
}

.edit-btn {
    height: 50px;
    border-radius: 14px;
    font-weight: 500;
    border-color: #e5e7eb;
    color: #6b7280;
}

.edit-btn:hover {
    background-color: #f9fafb;
    color: #4f46e5;
    border-color: #4f46e5;
}

@media (max-width: 640px) {
    .profile-card {
        margin: 10px;
    }
}
</style>