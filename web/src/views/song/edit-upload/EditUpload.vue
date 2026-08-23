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
import { getImageUrl, sameId } from '@/utils/format';
import { songApi } from '@/api/song-api';
import { useEditUpload } from './composables/use-edit-upload';

const route = useRoute();
const router = useRouter();
const songStore = useSongStore();

const song_id = route.params.id as string;

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
    if (cached && sameId(cached.song_id, song_id)) {
        formData.song_name = cached.song_title || '';
        previewUrl.value = getCoverUrl(cached.song_cover_url || '');
    } else {
        try {
            const { song } = await songApi.getUserUploadSong(song_id)
            formData.song_name = song.song_title || ''
            previewUrl.value = getCoverUrl(song.song_cover_url || '')
        } catch {
            router.replace('/user-uploads')
            return
        }
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
.page {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
    background: #f6f8fb;
}

.page__scroll {
    flex: 1;
    min-height: 0;
    padding: 24px;
    overflow-y: auto;
}

@media (min-width: 640px) {
    .page__scroll {
        padding: 32px;
    }
}

.edit-card {
    width: 100%;
    max-width: 960px;
    margin: 0 auto 40px;
    overflow: hidden;
    border: 1px solid #e5e7eb;
    border-radius: 16px;
    box-shadow: 0 10px 30px rgb(15 23 42 / 7%);
}

.edit-card__header {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    min-height: 88px;
    padding: 0 28px;
    background: linear-gradient(120deg, #409eff, #6a8df5);
}

.edit-card__back {
    transition: transform .2s ease;
}

.edit-card__back:hover {
    transform: scale(1.08);
}

.edit-card__title {
    margin: 0 0 0 16px;
    color: #fff;
    font-size: 20px;
    font-weight: 700;
}

.edit-card__body {
    padding: 28px;
}

.form__grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 28px;
}

@media (min-width: 768px) {
    .form__grid {
        grid-template-columns: 200px minmax(0, 1fr) minmax(0, 1fr);
    }
}

.cover-field {
    display: flex;
    justify-content: center;
    margin-bottom: 16px;
}

.cover-field__box {
    position: relative;
    width: 192px;
    height: 192px;
    overflow: hidden;
    cursor: pointer;
    border: 4px solid #fff;
    border-radius: 14px;
    box-shadow: 0 8px 20px rgb(15 23 42 / 16%);
}

.cover-field__image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.cover-field__placeholder {
    display: flex;
    width: 100%;
    height: 100%;
    align-items: center;
    justify-content: center;
    color: #c0c4cc;
    background: #f8fafc;
}

.cover-field__overlay {
    position: absolute;
    inset: 0;
    z-index: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #fff;
    opacity: 0;
    background: rgb(15 23 42 / 58%);
    transition: opacity .25s ease;
}

.cover-field__box:hover .cover-field__overlay {
    opacity: 1;
}

.cover-field__camera {
    margin-bottom: 8px;
    transition: transform .2s ease;
}

.cover-field__box:hover .cover-field__camera {
    transform: scale(1.08);
}

.cover-field__hint {
    font-size: 12px;
    font-weight: 700;
    letter-spacing: .06em;
}

.cover-field__badge {
    margin-top: 8px;
    padding: 2px 8px;
    font-size: 10px;
    background: #67c23a;
    border-radius: 4px;
}

.cover-field__input {
    position: absolute;
    inset: 0;
    z-index: 2;
    width: 100%;
    height: 100%;
    cursor: pointer;
    opacity: 0;
}

@media (min-width: 768px) {
    .form__main {
        grid-column: span 2;
    }
}

.form__notice {
    margin-top: 16px;
    padding: 14px;
    background: #ecf5ff;
    border: 1px solid #d9ecff;
    border-radius: 10px;
}

.form__notice-text {
    margin: 0;
    color: #337ecc;
    font-size: 12px;
}

.lyrics-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 20px;
    margin-top: 28px;
}

@media (min-width: 768px) {
    .lyrics-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

.form__actions {
    display: flex;
    gap: 12px;
    margin-top: 32px;
}

.form__save {
    flex: 1;
}

.form__cancel {
    padding-right: 28px;
    padding-left: 28px;
    border-radius: 10px;
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
