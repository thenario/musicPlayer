<template>
    <!-- 1. 父容器设为 h-full flex flex-col，确保高度填满父级 -->
    <div class="h-full flex flex-col bg-gray-50 p-4 sm:p-6 lg:p-8">

        <!-- 2. 顶部导航：设为 shrink-0，防止被压缩 -->
        <div class="flex items-center justify-between mb-8 shrink-0">
            <div class="flex items-center gap-4">
                <el-button @click="router.push('/userProfile')" circle>
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
                    <div v-for="song in songs" :key="song.song_url" class="song-item-card group">
                        <div class="flex items-center p-4">
                            <!-- 歌曲封面 -->
                            <el-image :src="getCoverUrl(song.song_cover_url)"
                                class="w-16 h-16 rounded-lg shadow-sm shrink-0" fit="cover">
                                <template #error>
                                    <div
                                        class="w-full h-full bg-gray-100 flex items-center justify-center text-gray-400">
                                        <el-icon :size="24">
                                            <Mic />
                                        </el-icon>
                                    </div>
                                </template>
                            </el-image>

                            <!-- 歌曲详情 -->
                            <div class="ml-4 grow overflow-hidden">
                                <h3
                                    class="text-lg font-semibold text-gray-800 truncate group-hover:text-indigo-600 transition-colors">
                                    {{ song.song_title }}
                                </h3>
                                <p class="text-sm text-gray-500 truncate">{{ song.artist || '未知艺术家' }}</p>
                                <div class="flex items-center mt-1 text-xs text-gray-400">
                                    <el-icon class="mr-1">
                                        <Calendar />
                                    </el-icon>
                                    {{ formatDate(song.date_added) }}
                                </div>
                            </div>

                            <!-- 操作按钮 -->
                            <div class="flex gap-2 ml-4">
                                <el-tooltip content="编辑详情" placement="top">
                                    <el-button circle @click="goToSongEdit(song)"
                                        class="hover:bg-indigo-50 hover:text-indigo-600 border-none">
                                        <el-icon>
                                            <EditPen />
                                        </el-icon>
                                    </el-button>
                                </el-tooltip>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <el-empty v-else-if="!loading" description="你还没有上传过任何歌曲" :image-size="200">
                <el-button type="primary" plain @click="router.push('/upload')">立即去上传</el-button>
            </el-empty>
        </div>

        <!-- 4. 分页组件：设为 shrink-0，固定在底部 -->
        <div class="flex justify-center py-6 shrink-0">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next" :total="total" @size-change="handleSizeChange"
                @current-change="handleCurrentChange" background />
        </div>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import { songApi } from '../../axios/songApi';
import { ElMessage } from 'element-plus';
import { useSongStore } from '@/stores/song';
import { Back, Upload, Mic, EditPen, Calendar } from '@element-plus/icons-vue';

const router = useRouter();
const songStore = useSongStore();

// 环境变量获取
const API_BASE_URL = import.meta.env.VITE_API_URL;

const songs = ref<any[]>([]);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

const getCoverUrl = (url: string) => {
    if (!url) return '/default-cover.png';
    if (url.startsWith('http')) return url;

    // 自动处理斜杠连接
    const separator = url.startsWith('/') ? '' : '/';
    return `${API_BASE_URL}${separator}${url}`;
};

const fetchSongs = async () => {
    try {
        loading.value = true;
        const res = await songApi.getUserUploadSongs(currentPage.value, pageSize.value);
        songs.value = res.songs;
        total.value = res.total;
    } catch (err) {
        console.error(err);
        ElMessage.error("加载列表失败");
    } finally {
        loading.value = false;
    }
};

const handleSizeChange = (val: number) => {
    pageSize.value = val;
    currentPage.value = 1;
    fetchSongs();
};

const handleCurrentChange = (val: number) => {
    currentPage.value = val;
    fetchSongs();
};

const formatDate = (val: any) => {
    if (!val) return '未知时间';
    const date = new Date(val);
    return date.toLocaleDateString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit'
    });
};

const goToSongEdit = (song: any) => {
    songStore.setEditingSong(song);
    router.push(`/EditUserUploadSong/${song.song_id || song.id}`);
};

onMounted(() => {
    fetchSongs();
});
</script>

<style scoped>
/* 确保页面填满高度 */
:deep(.h-full) {
    height: 100%;
}

/* 歌曲卡片样式 */
.song-item-card {
    background: white;
    border-radius: 16px;
    border: 1px solid #f3f4f6;
    transition: all 0.3s ease;
}

.song-item-card:hover {
    transform: translateX(4px);
    box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.05);
    border-color: #e5e7eb;
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