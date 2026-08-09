<template>
    <!-- 1. 这里的 h-full 会占据父级（Layout）的所有高度 -->
    <div class="page">

        <!-- 2. 这里的容器负责滚动 -->
        <div class="page__scroll custom-scrollbar">

            <!-- 3. 卡片居中限制宽度 -->
            <el-card
                class="edit-card animate-fade-in"
                :body-style="{ padding: '0px' }">
                <!-- 顶部装饰栏 -->
                <div class="edit-card__header">
                    <el-button circle @click="router.back()" class="edit-card__back">
                        <el-icon>
                            <Back />
                        </el-icon>
                    </el-button>
                    <h1 class="edit-card__title">编辑歌曲：{{ formData.song_name || '加载中...' }}</h1>
                </div>

                <div class="edit-card__body">
                    <el-form ref="formRef" :model="formData" :rules="rules" label-position="top">

                        <div class="form__grid">
                            <!-- 左侧：封面编辑 -->
                            <div class="cover-field">
                                <div
                                    class="cover-field__box">
                                    <el-image :src="previewUrl" class="cover-field__image">
                                        <template #error>
                                            <div
                                                class="cover-field__placeholder">
                                                <el-icon :size="48">
                                                    <Mic />
                                                </el-icon>
                                            </div>
                                        </template>
                                    </el-image>

                                    <div
                                        class="cover-field__overlay">
                                        <el-icon :size="32"
                                            class="cover-field__camera">
                                            <Camera />
                                        </el-icon>
                                        <span class="cover-field__hint">更换封面</span>
                                        <div v-if="songCoverFile"
                                            class="cover-field__badge">已选择新图片</div>
                                    </div>
                                    <input type="file" accept="image/*"
                                        class="cover-field__input"
                                        @change="handleFileChange" />
                                </div>
                            </div>

                            <!-- 右侧：基础信息 -->
                            <div class="form__main">
                                <el-form-item label="歌曲名称" prop="song_name">
                                    <el-input v-model="formData.song_name" placeholder="请输入歌名" size="large"
                                        class="custom-input" />
                                </el-form-item>
                                <div class="form__notice">
                                    <p class="form__notice-text">提示：修改封面或歌名后，请点击下方保存按钮生效。</p>
                                </div>
                            </div>
                        </div>

                        <!-- 下方：歌词编辑 -->
                        <div class="lyrics-grid">
                            <el-form-item label="LRC 原文歌词" prop="lyrics">
                                <el-input v-model="formData.lyrics" type="textarea" :rows="12"
                                    placeholder="[00:00.00] 歌词内容" class="lyrics-input" v-loading="lyricsLoading" />
                            </el-form-item>
                            <el-form-item label="LRC 翻译歌词" prop="t_lyrics">
                                <el-input v-model="formData.t_lyrics" type="textarea" :rows="12"
                                    placeholder="[00:00.00] 翻译内容" class="lyrics-input" v-loading="lyricsLoading" />
                            </el-form-item>
                        </div>

                        <!-- 操作按钮 -->
                        <div class="form__actions">
                            <el-button type="primary" size="large" class="form__save save-btn" :loading="submitting"
                                @click="submitForm">
                                确认修改并保存
                            </el-button>
                            <el-button size="large" class="form__cancel" @click="router.back()">取消</el-button>
                        </div>
                    </el-form>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'EditUploadPage' })
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useSongStore } from '@/stores/song';
import { Back, Camera, Mic } from '@element-plus/icons-vue';
import { getImageUrl } from '@/utils/format';
import { useEditUpload } from './composables/useEditUpload';

const route = useRoute();
const router = useRouter();
const songStore = useSongStore();

const song_id = Number(route.params.id);

const {
    formRef,
    submitting,
    lyricsLoading,
    songCoverFile,
    previewUrl,
    formData,
    rules,
    fetchLyrics,
    handleFileChange,
    save,
} = useEditUpload(song_id);

const getCoverUrl = (url: string) => getImageUrl(url);

onMounted(async () => {
    const cached = songStore.currentEditingSong;
    if (cached && Number(cached.song_id) === song_id) {
        formData.song_name = cached.song_title || '';
        previewUrl.value = getCoverUrl(cached.song_cover_url || '');
    }

    // 无论是否有缓存，都去获取歌词数据
    if (song_id) {
        fetchLyrics();
    } else {
        ElMessage.error("参数错误：未获取到歌曲ID");
    }
});

const submitForm = async () => {
    if (await save()) router.push('/user-uploads');
};
</script>

<style scoped>
@reference "../../../assets/index.css";

.page {
    @apply h-full flex flex-col bg-gray-50;
}

.page__scroll {
    @apply flex-1 overflow-y-auto p-4;
}

@media (min-width: 640px) {
    .page__scroll {
        @apply p-8;
    }
}

.edit-card {
    @apply w-full max-w-3xl mx-auto border-none shadow-lg rounded-3xl overflow-hidden mb-10;
}

.edit-card__header {
    @apply h-24 bg-linear-to-r from-indigo-600 to-purple-600 flex items-center px-8 shrink-0;
}

.edit-card__back {
    @apply transition-transform;
}

.edit-card__back:hover {
    @apply scale-110;
}

.edit-card__title {
    @apply text-white text-xl font-bold ml-4;
}

.edit-card__body {
    @apply p-8;
}

.form__grid {
    @apply grid grid-cols-1 gap-8;
}

@media (min-width: 768px) {
    .form__grid {
        @apply grid-cols-3;
    }
}

.cover-field {
    @apply flex justify-center mb-4;
}

.cover-field__box {
    @apply relative w-48 h-48 cursor-pointer shadow-xl rounded-2xl overflow-hidden border-4 border-white;
}

.cover-field__image {
    @apply w-full h-full object-cover;
}

.cover-field__placeholder {
    @apply w-full h-full bg-gray-50 flex items-center justify-center text-gray-300;
}

.cover-field__overlay {
    @apply absolute inset-0 bg-black/50 flex flex-col items-center justify-center text-white opacity-0 transition-all duration-300 backdrop-blur-sm;
}

.cover-field__box:hover .cover-field__overlay {
    @apply opacity-100;
}

.cover-field__camera {
    @apply mb-2 transition-transform;
}

.cover-field__box:hover .cover-field__camera {
    @apply scale-110;
}

.cover-field__hint {
    @apply text-xs font-bold tracking-wider;
}

.cover-field__badge {
    @apply mt-2 px-2 py-0.5 bg-green-500 rounded text-[10px];
}

.cover-field__input {
    @apply absolute inset-0 opacity-0 cursor-pointer z-10;
}

@media (min-width: 768px) {
    .form__main {
        @apply col-span-2;
    }
}

.form__notice {
    @apply mt-4 p-4 bg-blue-50 rounded-xl border border-blue-100;
}

.form__notice-text {
    @apply text-xs text-blue-600;
}

.lyrics-grid {
    @apply grid grid-cols-1 gap-6 mt-8;
}

@media (min-width: 768px) {
    .lyrics-grid {
        @apply grid-cols-2;
    }
}

.form__actions {
    @apply flex gap-4 mt-10;
}

.form__save {
    @apply flex-1;
}

.form__cancel {
    @apply px-10 rounded-xl;
}

/* 核心修复：确保容器高度 */
.h-full {
    height: 100%;
}

.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #e5e7eb;
    border-radius: 10px;
}

/* 保持你原有的动画和样式 */
.animate-fade-in {
    animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

:deep(.lyrics-input .el-textarea__inner) {
    border-radius: 12px;
    background-color: #f9fafb;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
    font-size: 13px;
    padding: 12px;
    border-color: #e5e7eb;
    /* 解决部分浏览器无法滚动的关键：允许 textarea 内部滚动 */
    overflow-y: auto;
}

:deep(.custom-input .el-input__wrapper) {
    border-radius: 12px;
    box-shadow: 0 0 0 1px #e5e7eb inset;
}

.save-btn {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    border: none;
    border-radius: 12px;
    font-weight: 600;
}
</style>