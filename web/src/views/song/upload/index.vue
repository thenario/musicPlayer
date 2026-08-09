<template>
  <div class="h-full overflow-y-auto custom-scrollbar p-6 bg-gray-900 text-white">
    <div class="max-w-3xl mx-auto mt-4 pb-24">
      <h1 class="text-3xl font-bold mb-8">上传歌曲</h1>

      <div class="bg-gray-800 rounded-xl p-8 shadow-xl border border-gray-700">
        <!-- 音频文件上传区 -->
        <div class="mb-8">
          <label class="block text-sm font-medium text-gray-400 mb-2">音频文件 (必选)</label>
          <div class="relative border-2 border-dashed rounded-lg p-10 text-center transition-all cursor-pointer" :class="[
            isDragging ? 'border-blue-500 bg-blue-500/10' : 'border-gray-600 hover:border-gray-400 hover:bg-gray-700/50',
            audioFile ? 'border-green-500/50 bg-green-500/5' : ''
          ]" @dragover.prevent="isDragging = true" @dragleave.prevent="isDragging = false"
            @drop.prevent="handleAudioDrop" @click="audioInput?.click()">
            <input type="file" ref="audioInput" class="hidden" accept=".mp3,.flac,.wav,.m4a"
              @change="handleAudioSelect" />

            <div v-if="!audioFile" class="space-y-4">
              <div class="text-5xl opacity-50">🎵</div>
              <div>
                <p class="text-lg text-gray-200">将文件拖到此处，或 <span class="text-blue-400 font-semibold">点击上传</span></p>
                <p class="text-xs text-gray-500 mt-2">支持 MP3, FLAC, WAV, M4A</p>
              </div>
            </div>

            <div v-else class="flex items-center justify-center space-x-3 text-green-400">
              <span class="text-2xl">✅</span>
              <span class="font-medium text-lg truncate max-w-xs">{{ audioFile.name }}</span>
              <button @click.stop="audioFile = null"
                class="ml-4 p-1 hover:bg-gray-600 rounded-full text-gray-400">✕</button>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
          <!-- 封面选择 -->
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-2">封面图片 (必选)</label>
            <div
              class="w-full aspect-square border-2 border-dashed border-gray-600 rounded-lg overflow-hidden cursor-pointer hover:border-gray-400 transition-colors flex items-center justify-center bg-gray-900/50"
              @click="coverInput?.click()">
              <input type="file" ref="coverInput" class="hidden" accept="image/*" @change="handleCoverSelect" />
              <img v-if="coverPreview" :src="coverPreview" class="w-full h-full object-cover" alt="封面" />
              <div v-else class="text-center">
                <span class="text-3xl text-gray-600">+</span>
                <p class="text-xs text-gray-500 mt-1">选择封面</p>
              </div>
            </div>
          </div>

          <div class="flex flex-col space-y-4">
            <input v-model="form.title" placeholder="歌曲标题" class="input-style" />
            <input v-model="form.artist" placeholder="艺术家" class="input-style" />
            <input v-model="form.album" placeholder="专辑名称" class="input-style" />
            <textarea v-model="form.lyrics" placeholder="在这里输入或粘贴歌词/LRC..." rows="5"
              class="input-style resize-none custom-scrollbar text-sm"></textarea>
          </div>
        </div>

        <transition name="el-fade-in">
          <div v-if="uploading" class="mb-8">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs text-blue-400 font-medium">资源同步中...</span>
              <span class="text-xs text-gray-500">{{ uploadProgress }}%</span>
            </div>
            <el-progress :percentage="uploadProgress" :stroke-width="10" :show-text="false" striped striped-flow
              color="#3b82f6" />
          </div>
        </transition>

        <button @click="submitUpload" :disabled="uploading"
          class="w-full py-4 bg-blue-600 hover:bg-blue-500 disabled:bg-gray-700 disabled:cursor-not-allowed text-white font-bold rounded-xl transition-all shadow-lg shadow-blue-900/20">
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