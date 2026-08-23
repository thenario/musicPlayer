<template>
  <div class="page custom-scrollbar">
    <div class="page__inner">
      <h1 class="page__title">上传歌曲</h1>

      <div class="upload-card">
        <!-- 音频文件上传区 -->
        <div class="upload-card__section">
          <label class="upload-card__label">音频文件 (必选)</label>
          <div class="upload-card__dropzone" :class="[
            isDragging ? 'is-dragging' : 'is-idle',
            audioFile ? 'is-selected' : ''
          ]" @dragover.prevent="isDragging = true" @dragleave.prevent="isDragging = false"
            @drop.prevent="handleAudioDrop" @click="audioInput?.click()">
            <input type="file" ref="audioInput" class="upload-card__audio-input" accept=".mp3,.flac,.wav,.m4a"
              @change="handleAudioSelect" />

            <div v-if="!audioFile" class="upload-card__placeholder">
              <div class="upload-card__emoji">🎵</div>
              <div>
                <p class="upload-card__hint">将文件拖到此处，或 <span class="upload-card__hint-accent">点击上传</span></p>
                <p class="upload-card__sub-hint">支持 MP3, FLAC, WAV, M4A</p>
              </div>
            </div>

            <div v-else class="upload-card__file-info">
              <span class="upload-card__file-check">✅</span>
              <span class="upload-card__file-name">{{ audioFile.name }}</span>
              <button @click.stop="audioFile = null"
                class="upload-card__file-remove">✕</button>
            </div>
          </div>
        </div>

        <div class="form-grid">
          <!-- 封面选择 -->
          <div>
            <label class="form-grid__label">封面图片 (必选)</label>
            <div
              class="cover-field"
              @click="coverInput?.click()">
              <input type="file" ref="coverInput" class="cover-field__input" accept="image/*" @change="handleCoverSelect" />
              <img v-if="coverPreview" :src="coverPreview" class="cover-field__preview" alt="封面" />
              <div v-else class="cover-field__placeholder">
                <span class="cover-field__plus">+</span>
                <p class="cover-field__hint">选择封面</p>
              </div>
            </div>
          </div>

          <div class="form-fields">
            <input v-model="form.title" placeholder="歌曲标题" class="input-style" />
            <input v-model="form.artist" placeholder="艺术家" class="input-style" />
            <input v-model="form.album" placeholder="专辑名称" class="input-style" />
            <textarea v-model="form.lyrics" placeholder="在这里输入或粘贴歌词/LRC..." rows="5"
              class="input-style lyrics-textarea custom-scrollbar"></textarea>
          </div>
        </div>

        <transition name="el-fade-in">
          <div v-if="uploading" class="upload-progress">
            <div class="upload-progress__row">
              <span class="upload-progress__label">资源同步中...</span>
              <span class="upload-progress__percent">{{ uploadProgress }}%</span>
            </div>
            <el-progress :percentage="uploadProgress" :stroke-width="10" :show-text="false" striped striped-flow
              color="#3b82f6" />
          </div>
        </transition>

        <button @click="submitUpload" :disabled="uploading"
          class="submit-btn">
          <span v-if="!uploading">开始上传</span>
          <span v-else>正在上传资源 ({{ uploadProgress }}%)</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'UploadPage' })
import { useSongUpload } from './composables/use-song-upload'

const {
  audioInput,
  coverInput,
  audioFile,  coverPreview,
  isDragging,
  uploading,
  uploadProgress,
  form,
  handleAudioSelect,
  handleAudioDrop,
  handleCoverSelect,
  submit,
} = useSongUpload()

const submitUpload = () => submit()
</script>

<style scoped>
.page {
  box-sizing: border-box;
  height: 100%;
  overflow-y: auto;
  padding: 32px 24px 64px;
  background: #f6f8fb;
  color: #1f2937;
}

.page__inner {
  max-width: 960px;
  margin: 0 auto;
}

.page__title {
  margin: 0 0 24px;
  font-size: 28px;
  line-height: 1.2;
}

.upload-card {
  padding: 28px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgb(15 23 42 / 6%);
}

.upload-card__section {
  margin-bottom: 28px;
}

.upload-card__label,
.form-grid__label {
  display: block;
  margin-bottom: 8px;
  color: #4b5563;
  font-size: 14px;
  font-weight: 600;
}

.upload-card__dropzone {
  position: relative;
  padding: 36px 24px;
  text-align: center;
  cursor: pointer;
  border: 2px dashed #cbd5e1;
  border-radius: 12px;
  transition: border-color .2s ease, background-color .2s ease;
}

.upload-card__dropzone.is-idle {
  border-color: #cbd5e1;
}

.upload-card__dropzone.is-idle:hover {
  border-color: #8aa4c8;
  background: #f8fbff;
}

.upload-card__dropzone.is-dragging {
  border-color: #409eff;
  background: #ecf5ff;
}

.upload-card__dropzone.is-selected {
  border-color: #67c23a;
  background: #f0f9eb;
}

.upload-card__audio-input {
  display: none;
}

.upload-card__placeholder {
  display: grid;
  gap: 12px;
}

.upload-card__emoji {
  font-size: 42px;
  opacity: .7;
}

.upload-card__hint {
  margin: 0;
  font-size: 16px;
}

.upload-card__hint-accent {
  color: #409eff;
  font-weight: 600;
}

.upload-card__sub-hint {
  margin: 6px 0 0;
  color: #9ca3af;
  font-size: 12px;
}

.upload-card__file-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #529b2e;
}

.upload-card__file-check {
  font-size: 22px;
}

.upload-card__file-name {
  max-width: 360px;
  overflow: hidden;
  font-size: 16px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-card__file-remove {
  padding: 4px 7px;
  color: #6b7280;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 999px;
}

.upload-card__file-remove:hover {
  background: #e5e7eb;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 28px;
  margin-bottom: 28px;
}

@media (min-width: 768px) {
  .form-grid {
    grid-template-columns: 220px 1fr;
  }
}

.cover-field {
  display: flex;
  aspect-ratio: 1;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
  background: #f8fafc;
  border: 2px dashed #cbd5e1;
  border-radius: 12px;
  transition: border-color .2s ease;
}

.cover-field:hover {
  border-color: #8aa4c8;
}

.cover-field__input {
  display: none;
}

.cover-field__preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-field__placeholder {
  text-align: center;
}

.cover-field__plus {
  color: #94a3b8;
  font-size: 32px;
}

.cover-field__hint {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 12px;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.lyrics-textarea {
  min-height: 132px;
  resize: vertical;
  font-size: 14px;
}

.upload-progress {
  margin-bottom: 28px;
}

.upload-progress__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.upload-progress__label {
  color: #409eff;
  font-size: 12px;
  font-weight: 600;
}

.upload-progress__percent {
  color: #909399;
  font-size: 12px;
}

.submit-btn {
  width: 100%;
  padding: 13px 20px;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  background: #409eff;
  border: 0;
  border-radius: 10px;
  transition: background-color .2s ease, transform .2s ease;
}

.submit-btn:hover {
  background: #337ecc;
}

.submit-btn:disabled {
  cursor: not-allowed;
  background: #a8abb2;
}

.input-style {
  box-sizing: border-box;
  width: 100%;
  padding: 11px 12px;
  color: #303133;
  font: inherit;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  outline: none;
  transition: border-color .2s ease, box-shadow .2s ease;
}

.input-style::placeholder {
  color: #a8abb2;
}

.input-style:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgb(64 158 255 / 12%);
}

.el-fade-in-enter-active,
.el-fade-in-leave-active {
  transition: opacity 0.3s ease;
}

.el-fade-in-enter-from,
.el-fade-in-leave-to {
  opacity: 0;
}
</style>
