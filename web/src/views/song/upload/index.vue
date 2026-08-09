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
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { useSongUpload } from './composables/useSongUpload'

const userStore = useUserStore()
const { user } = storeToRefs(userStore)

const {
  audioInput,
  coverInput,
  audioFile,
  coverFile,
  coverPreview,
  isDragging,
  uploading,
  uploadProgress,
  form,
  handleAudioSelect,
  handleAudioDrop,
  handleCoverSelect,
  submit,
} = useSongUpload()

const submitUpload = () => submit(user.value?.user_id)
</script>

<style scoped>
@reference "../../../assets/index.css";

.page {
  @apply h-full overflow-y-auto p-6 bg-gray-900 text-white;
}

.page__inner {
  @apply max-w-3xl mx-auto mt-4 pb-24;
}

.page__title {
  @apply text-3xl font-bold mb-8;
}

.upload-card {
  @apply bg-gray-800 rounded-xl p-8 shadow-xl border border-gray-700;
}

.upload-card__section {
  @apply mb-8;
}

.upload-card__label {
  @apply block text-sm font-medium text-gray-400 mb-2;
}

.upload-card__dropzone {
  @apply relative border-2 border-dashed rounded-lg p-10 text-center transition-all cursor-pointer;
}

.upload-card__dropzone.is-idle {
  @apply border-gray-600;
}

.upload-card__dropzone.is-idle:hover {
  @apply border-gray-400 bg-gray-700/50;
}

.upload-card__dropzone.is-dragging {
  @apply border-blue-500 bg-blue-500/10;
}

.upload-card__dropzone.is-selected {
  @apply border-green-500/50 bg-green-500/5;
}

.upload-card__audio-input {
  @apply hidden;
}

.upload-card__placeholder {
  @apply space-y-4;
}

.upload-card__emoji {
  @apply text-5xl opacity-50;
}

.upload-card__hint {
  @apply text-lg text-gray-200;
}

.upload-card__hint-accent {
  @apply text-blue-400 font-semibold;
}

.upload-card__sub-hint {
  @apply text-xs text-gray-500 mt-2;
}

.upload-card__file-info {
  @apply flex items-center justify-center space-x-3 text-green-400;
}

.upload-card__file-check {
  @apply text-2xl;
}

.upload-card__file-name {
  @apply font-medium text-lg truncate max-w-xs;
}

.upload-card__file-remove {
  @apply ml-4 p-1 rounded-full text-gray-400;
}

.upload-card__file-remove:hover {
  @apply bg-gray-600;
}

.form-grid {
  @apply grid grid-cols-1 gap-8 mb-8;
}

@media (min-width: 768px) {
  .form-grid {
    @apply grid-cols-2;
  }
}

.form-grid__label {
  @apply block text-sm font-medium text-gray-400 mb-2;
}

.cover-field {
  @apply w-full aspect-square border-2 border-dashed border-gray-600 rounded-lg overflow-hidden cursor-pointer transition-colors flex items-center justify-center bg-gray-900/50;
}

.cover-field:hover {
  @apply border-gray-400;
}

.cover-field__input {
  @apply hidden;
}

.cover-field__preview {
  @apply w-full h-full object-cover;
}

.cover-field__placeholder {
  @apply text-center;
}

.cover-field__plus {
  @apply text-3xl text-gray-600;
}

.cover-field__hint {
  @apply text-xs text-gray-500 mt-1;
}

.form-fields {
  @apply flex flex-col space-y-4;
}

.lyrics-textarea {
  @apply resize-none text-sm;
}

.upload-progress {
  @apply mb-8;
}

.upload-progress__row {
  @apply flex justify-between items-center mb-2;
}

.upload-progress__label {
  @apply text-xs text-blue-400 font-medium;
}

.upload-progress__percent {
  @apply text-xs text-gray-500;
}

.submit-btn {
  @apply w-full py-4 bg-blue-600 text-white font-bold rounded-xl transition-all shadow-lg shadow-blue-900/20;
}

.submit-btn:hover {
  @apply bg-blue-500;
}

.submit-btn:disabled {
  @apply bg-gray-700 cursor-not-allowed;
}

.input-style {
  @apply w-full bg-gray-900 border border-gray-700 rounded-lg p-3 focus:border-blue-500 outline-none transition-all placeholder:text-gray-600;
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