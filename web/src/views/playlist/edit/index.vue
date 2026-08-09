<template>
    <div class="playlist-edit">
        <div class="playlist-edit__layout">
            <!-- 左侧：表单 -->
            <div class="playlist-edit__form-card">
                <h2 class="playlist-edit__heading">
                    <el-icon>
                        <Edit />
                    </el-icon> 编辑歌单详情
                </h2>

                <el-form :model="form" label-position="top" class="custom-form">
                    <el-form-item label="歌单封面">
                        <div class="playlist-edit__cover-row">
                            <el-upload class="avatar-uploader" action="#" :auto-upload="false" :show-file-list="false"
                                :on-change="handleFileChange">
                                <div v-if="previewImage"
                                    class="playlist-edit__preview">
                                    <img :src="previewImage" class="playlist-edit__preview-img" alt="封面图片" />
                                    <div
                                        class="playlist-edit__preview-overlay">
                                        <el-icon color="white">
                                            <Camera />
                                        </el-icon>
                                    </div>
                                </div>
                                <div v-else
                                    class="playlist-edit__upload-placeholder">
                                    <el-icon :size="24">
                                        <Plus />
                                    </el-icon>
                                    <span class="playlist-edit__upload-hint">上传封面</span>
                                </div>
                            </el-upload>
                            <div class="playlist-edit__cover-hint">
                                支持 JPG, PNG 格式<br>建议尺寸 500x500 px
                            </div>
                        </div>
                    </el-form-item>

                    <el-form-item label="歌单名称">
                        <el-input v-model="form.name" placeholder="请输入歌单名称" maxlength="40" show-word-limit />
                    </el-form-item>

                    <el-form-item label="歌单描述">
                        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="介绍一下你的歌单..."
                            maxlength="200" show-word-limit />
                    </el-form-item>

                    <div class="playlist-edit__form-actions">
                        <el-button type="primary" size="large" class="playlist-edit__save" :loading="submitting" @click="submitForm">
                            保存修改
                        </el-button>
                        <el-button size="large" @click="router.back()">取消</el-button>
                    </div>
                </el-form>
            </div>

            <!-- 右侧：预览 -->
            <div class="playlist-edit__preview-col">
                <p class="playlist-edit__preview-label">实时预览</p>
                <div class="playlist-edit__sticky">
                    <div class="playlist-edit__preview-card">
                        <div class="playlist-edit__preview-card-media">
                            <img :src="previewImage || '/default-cover.png'" alt="封面图片"
                                class="playlist-edit__preview-card-img" />
                            <div class="playlist-edit__preview-card-overlay">
                            </div>
                            <div class="playlist-edit__preview-card-text">
                                <h3 class="playlist-edit__preview-card-title">{{ form.name || '未命名歌单' }}</h3>
                                <p class="playlist-edit__preview-card-desc">{{ form.description || '暂无描述...' }}
                                </p>
                            </div>
                        </div>
                        <div class="playlist-edit__preview-card-footer">
                            <div class="playlist-edit__preview-user">
                                <el-avatar :size="20" class="playlist-edit__preview-avatar">{{ user?.user_name?.[0] || 'U' }}</el-avatar>
                                <span class="playlist-edit__preview-username">{{ user?.user_name || '未知昵称' }}</span>
                            </div>
                            <span class="playlist-edit__preview-badge">预览效果</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'EditPlaylistPage' })
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { Edit, Camera, Plus } from '@element-plus/icons-vue'
import { useEditPlaylist } from './composables/useEditPlaylist'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { user } = storeToRefs(userStore)

const playlistId = route.params.id as string
const { form, previewImage, submitting, load, handleFileChange, submit } = useEditPlaylist(playlistId)

onMounted(load)

const submitForm = async () => {
    if (await submit()) router.push(`/playlists/${playlistId}`)
}
</script>

<style scoped>
@reference "../../../assets/index.css";

.playlist-edit {
  @apply min-h-screen bg-gray-950 p-8 flex justify-center;
}

.playlist-edit__layout {
  @apply max-w-5xl w-full grid grid-cols-1 md:grid-cols-12 gap-8;
}

.playlist-edit__form-card {
  @apply md:col-span-7 bg-gray-900 rounded-2xl p-8 border border-white/5 shadow-xl;
}

.playlist-edit__heading {
  @apply text-2xl font-bold mb-8 flex items-center gap-2;
}

.playlist-edit__cover-row {
  @apply flex items-center gap-4;
}

.playlist-edit__preview {
  @apply relative w-32 h-32 rounded-lg overflow-hidden border-2 border-dashed border-gray-700;
}

.playlist-edit__preview-img {
  @apply w-full h-full object-cover;
}

.playlist-edit__preview-overlay {
  @apply absolute inset-0 bg-black/40 opacity-0 flex items-center justify-center transition-opacity;
}

.playlist-edit__preview:hover .playlist-edit__preview-overlay {
  @apply opacity-100;
}

.playlist-edit__upload-placeholder {
  @apply w-32 h-32 rounded-lg border-2 border-dashed border-gray-700 flex flex-col items-center justify-center text-gray-500 transition-colors;
}

.playlist-edit__upload-placeholder:hover {
  @apply border-blue-500;
}

.playlist-edit__upload-hint {
  @apply text-xs mt-2;
}

.playlist-edit__cover-hint {
  @apply text-xs text-gray-500;
}

.playlist-edit__form-actions {
  @apply flex gap-4 mt-8;
}

.playlist-edit__save {
  @apply flex-1;
}

.playlist-edit__preview-col {
  @apply md:col-span-5 flex flex-col gap-4;
}

.playlist-edit__preview-label {
  @apply text-sm font-bold text-gray-500 uppercase tracking-widest;
}

.playlist-edit__sticky {
  @apply sticky top-8;
}

.playlist-edit__preview-card {
  @apply bg-gray-900 rounded-2xl overflow-hidden shadow-2xl border border-white/5;
}

.playlist-edit__preview-card-media {
  @apply aspect-square relative overflow-hidden;
}

.playlist-edit__preview-card-img {
  @apply w-full h-full object-cover transition-transform duration-500;
}

.playlist-edit__preview-card:hover .playlist-edit__preview-card-img {
  @apply scale-105;
}

.playlist-edit__preview-card-overlay {
  @apply absolute inset-0 bg-linear-to-t from-black/80 via-transparent to-transparent;
}

.playlist-edit__preview-card-text {
  @apply absolute bottom-4 left-4 right-4;
}

.playlist-edit__preview-card-title {
  @apply text-xl font-bold truncate;
}

.playlist-edit__preview-card-desc {
  @apply text-sm text-gray-400 line-clamp-2 mt-1;
}

.playlist-edit__preview-card-footer {
  @apply p-4 bg-gray-800/50 flex justify-between items-center;
}

.playlist-edit__preview-user {
  @apply flex items-center gap-2;
}

.playlist-edit__preview-avatar {
  @apply bg-blue-600;
}

.playlist-edit__preview-username {
  @apply text-xs text-gray-300;
}

.playlist-edit__preview-badge {
  @apply text-[10px] text-gray-500;
}
</style>