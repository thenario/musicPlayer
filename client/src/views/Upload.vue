<template>
  <div class="h-full overflow-y-auto custom-scrollbar p-6 bg-gray-900 text-white">
    <div class="max-w-3xl mx-auto mt-4 pb-24">
      <h1 class="text-3xl font-bold mb-8">上传歌曲</h1>

      <div class="bg-gray-800 rounded-xl p-8 shadow-xl border border-gray-700">
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

            <div v-else class="flex items-center justify-center space-x-3 text-green-400 animate-pulse">
              <span class="text-2xl">✅</span>
              <span class="font-medium text-lg">{{ audioFile.name }}</span>
              <button @click.stop="audioFile = null"
                class="ml-4 p-1 hover:bg-gray-600 rounded-full text-gray-400">✕</button>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-2">封面图片 (必选)</label>
            <div
              class="w-full aspect-square border-2 border-dashed border-gray-600 rounded-lg overflow-hidden cursor-pointer hover:border-gray-400 transition-colors flex items-center justify-center bg-gray-900/50"
              @click="coverInput?.click()">
              <input type="file" ref="coverInput" class="hidden" accept="image/*" @change="handleCoverSelect" />

              <img v-if="coverPreview" :src="coverPreview" class="w-full h-full object-cover" />
              <div v-else class="text-center">
                <span class="text-3xl text-gray-600">+</span>
                <p class="text-xs text-gray-500 mt-1">选择封面</p>
              </div>
            </div>
          </div>

          <div class="flex flex-col space-y-4">
            <input v-model="form.title" placeholder="歌曲标题"
              class="w-full bg-gray-900 border border-gray-700 rounded-lg p-3 focus:border-blue-500 outline-none transition-all" />
            <input v-model="form.artist" placeholder="艺术家"
              class="w-full bg-gray-900 border border-gray-700 rounded-lg p-3 focus:border-blue-500 outline-none transition-all" />
            <input v-model="form.album" placeholder="专辑名称"
              class="w-full bg-gray-900 border border-gray-700 rounded-lg p-3 focus:border-blue-500 outline-none transition-all" />
          </div>
        </div>

        <button @click="submitUpload" :disabled="uploading"
          class="w-full py-4 bg-blue-600 hover:bg-blue-500 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-bold rounded-xl transition-all shadow-lg shadow-blue-900/20">
          {{ uploading ? '正在上传中...' : '开始上传' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { songApi } from '../../axios/songApi'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'

const userstore = useUserStore()
const { user } = storeToRefs(userstore)

const audioInput = ref<HTMLInputElement | null>(null)
const coverInput = ref<HTMLInputElement | null>(null)

const audioFile = ref<File | null>(null)
const coverFile = ref<File | null>(null)
const coverPreview = ref<string>('')
const isDragging = ref(false)
const uploading = ref(false)

const form = ref({
  title: '',
  artist: '',
  album: ''
})

const handleAudioSelect = (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (files && files[0]) validateAndSetAudio(files[0])
}

const handleAudioDrop = (e: DragEvent) => {
  isDragging.value = false
  const files = e.dataTransfer?.files
  if (files && files[0]) validateAndSetAudio(files[0])
}

const validateAndSetAudio = (file: File) => {
  const validTypes = ['.mp3', '.flac', '.wav', '.m4a']
  const isAudio = validTypes.some(ext => file.name.toLowerCase().endsWith(ext))
  if (!isAudio) return ElMessage.error('不支持的文件格式')

  audioFile.value = file
  if (!form.value.title && file.name.includes('_')) {
    const parts = file.name.split('.')[0].split('_')
    form.value.title = parts[0]
    form.value.artist = parts[1] || ''
  } else if (!form.value.title) {
    form.value.title = file.name.split('.')[0]
  }
}

const handleCoverSelect = (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (files && files[0]) {
    coverFile.value = files[0]
    coverPreview.value = URL.createObjectURL(files[0])
  }
}

const submitUpload = async () => {
  if (!audioFile.value) return ElMessage.warning('请选择音频文件')
  if (!coverFile.value) return ElMessage.warning('请选择封面')
  if (!form.value.title) return ElMessage.warning('请输入标题')

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('audiofile', audioFile.value)
    if (coverFile.value) formData.append('coverfile', coverFile.value)

    formData.append('uploader_name', user.value?.user_name || 'Anonymous')
    formData.append('uploader_id', String(user.value?.user_id || 0))
    formData.append('title', form.value.title)
    formData.append('artist', form.value.artist)
    formData.append('album', form.value.album)
    formData.append('added_date', new Date().toISOString())

    const res = await songApi.uploadSong(formData)
    if (res.success) {
      ElMessage.success('上传成功！')
      resetForm()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    console.log(error);
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    uploading.value = false
  }
}

const resetForm = () => {
  audioFile.value = null
  coverFile.value = null
  coverPreview.value = ''
  form.value = { title: '', artist: '', album: '' }
  if (audioInput.value) audioInput.value.value = ''
  if (coverInput.value) coverInput.value.value = ''
}
</script>