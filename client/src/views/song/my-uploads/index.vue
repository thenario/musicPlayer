<template>
    <!-- 1. 父容器设为 h-full flex flex-col，确保高度填满父级 -->
    <div class="h-full flex flex-col bg-gray-50 p-4 sm:p-6 lg:p-8">

        <!-- 2. 顶部导航：设为 shrink-0，防止被压缩 -->
        <div class="flex items-center justify-between mb-8 shrink-0">
            <div class="flex items-center gap-4">
                <el-button @click="router.push('/user-profile')" circle>
                    <el-icon>
                        <Back />
                    </el-icon>
                </el-button>
                <h1 class="text-2xl font-bold text-gray-900">我的上传</h1>
            </div>
            <el-button type="primary" class="upload-btn" @click="router.push('/upload')">
                <el-icon class="mr-1">
                    <Upload />
                </el-icon>
                上传新歌曲
            </el-button>
        </div>

        <!-- 3. 歌曲列表区：关键点！设为 flex-1 和 overflow-y-auto -->
        <!-- 这样它会自动占据中间所有空间，多出内容时内部滚动 -->
        <div class="flex-1 overflow-y-auto pr-2 custom-scrollbar" v-loading="loading">
            <div v-if="songs && songs.length > 0">
                <div class="max-w-4xl mx-auto grid gap-4">
                    <UploadedSongCard
                        v-for="song in songs"
                        :key="song.song_url"
                        :song="song"
                        @edit="goToSongEdit"
                    />
                </div>
            </div>

            <el-empty v-else-if="!loading" description="你还没有上传过任何歌曲" :image-size="200">
                <el-button type="primary" plain @click="router.push('/upload')">立即去上传</el-button>
            </el-empty>
        </div>

        <!-- 4. 分页组件：设为 shrink-0，固定在底部 -->
        <div class="flex justify-center py-6 shrink-0">
            <AppPagination
                :current="pagination.state.current"
                :page-size="pagination.state.pageSize"
                :total="pagination.state.total"
                :page-sizes="[10, 20, 50]"
                @page-change="changePage"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { onMounted } from 'vue';
import { useSongStore } from '@/stores/song';
import { AppPagination } from '@/common';
import UploadedSongCard from './components/UploadedSongCard.vue';
import { useMyUploads } from './composables/useMyUploads';
import { Back, Upload } from '@element-plus/icons-vue';

const router = useRouter();
const songStore = useSongStore();

const { songs, loading, pagination, changePage, load } = useMyUploads();

const goToSongEdit = (song: any) => {
    songStore.setEditingSong(song);
    router.push(`/user-uploads/${song.song_id || song.id}/edit`);
};

onMounted(load);
</script>

<style scoped>
/* 确保页面填满高度 */
:deep(.h-full) {
    height: 100%;
}

/* 自定义滚动条样式，让它看起来更现代（可选） */
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #e5e7eb;
    border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background-color: transparent;
}

.upload-btn {
    background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
    border: none;
    border-radius: 10px;
    height: 40px;
    font-weight: 600;
}
</style>