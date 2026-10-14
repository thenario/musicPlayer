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
                            <img :src="previewImage || '/default-cover.svg'" alt="封面图片"
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
import { useEditPlaylist } from './composables/use-edit-playlist'
import { reportError } from '@/utils/feedback'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { user } = storeToRefs(userStore)

const playlistId = route.params.id as string
const { form, previewImage, submitting, load, handleFileChange: changeFileAction, submit } = useEditPlaylist(playlistId)
const handleFileChange = (file: Parameters<typeof changeFileAction>[0]) => {
    const result = changeFileAction(file)
    if (!result.success) reportError(result)
}

onMounted(async () => {
    const result = await load()
    if (!result.success) reportError(result)
})

const submitForm = async () => {
    const result = await submit()
    if (!result.success) return reportError(result)
    ElMessage.success(result.message || '更新成功')
    await router.push(`/playlists/${playlistId}`)
}
</script>

<style scoped>
.playlist-edit {
  box-sizing: border-box;
  min-height: 100%;
  padding: 32px 24px;
  background: var(--music-canvas);
}

.playlist-edit__layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 28px;
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
}

.playlist-edit__form-card {
  padding: 28px;
  background: var(--music-surface);
  border: 1px solid var(--music-border);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgb(15 23 42 / 6%);
}

.playlist-edit__heading {
  display: flex;
  gap: 8px;
  align-items: center;
  margin: 0 0 24px;
  color: var(--music-text);
  font-size: 22px;
}

.playlist-edit__cover-row {
  display: flex;
  gap: 16px;
  align-items: center;
}

.playlist-edit__preview {
  position: relative;
  width: 128px;
  height: 128px;
  overflow: hidden;
  border: 2px dashed var(--music-border);
  border-radius: 10px;
}

.playlist-edit__preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.playlist-edit__preview-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  background: rgb(15 23 42 / 45%);
  transition: opacity .2s ease;
}

.playlist-edit__preview:hover .playlist-edit__preview-overlay {
  opacity: 1;
}

.playlist-edit__upload-placeholder {
  display: flex;
  flex-direction: column;
  width: 128px;
  height: 128px;
  align-items: center;
  justify-content: center;
  color: var(--music-muted);
  border: 2px dashed var(--music-border);
  border-radius: 10px;
  transition: border-color .2s ease;
}

.playlist-edit__upload-placeholder:hover {
  border-color: var(--music-primary);
}

.playlist-edit__upload-hint {
  margin-top: 8px;
  font-size: 12px;
}

.playlist-edit__cover-hint {
  color: var(--music-muted);
  font-size: 12px;
  line-height: 1.7;
}

.playlist-edit__form-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
}

.playlist-edit__save {
  flex: 1;
}

.playlist-edit__preview-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.playlist-edit__preview-label {
  margin: 0;
  color: var(--music-muted);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: .08em;
}

.playlist-edit__sticky {
  position: sticky;
  top: 24px;
}

.playlist-edit__preview-card {
  overflow: hidden;
  background: var(--music-surface);
  border: 1px solid var(--music-border);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgb(15 23 42 / 8%);
}

.playlist-edit__preview-card-media {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.playlist-edit__preview-card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .45s ease;
}

.playlist-edit__preview-card:hover .playlist-edit__preview-card-img {
  transform: scale(1.04);
}

.playlist-edit__preview-card-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgb(15 23 42 / 80%), transparent 65%);
}

.playlist-edit__preview-card-text {
  position: absolute;
  right: 16px;
  bottom: 16px;
  left: 16px;
  color: #fff;
}

.playlist-edit__preview-card-title {
  margin: 0;
  overflow: hidden;
  font-size: 20px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-edit__preview-card-desc {
  display: -webkit-box;
  margin: 6px 0 0;
  overflow: hidden;
  color: rgb(255 255 255 / 78%);
  font-size: 13px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.playlist-edit__preview-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: var(--music-surface);
}

.playlist-edit__preview-user {
  display: flex;
  gap: 8px;
  align-items: center;
}

.playlist-edit__preview-avatar {
  color: var(--music-on-primary);
  background: var(--music-primary);
}

.playlist-edit__preview-username {
  color: var(--music-secondary);
  font-size: 12px;
}

.playlist-edit__preview-badge {
  color: var(--music-muted);
  font-size: 11px;
}

@media (min-width: 768px) {
  .playlist-edit__layout {
    grid-template-columns: minmax(0, 7fr) minmax(280px, 5fr);
  }
}
</style>
