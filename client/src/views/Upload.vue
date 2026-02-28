<template>
  <!-- 
    修改点 1: 添加 h-full overflow-y-auto custom-scrollbar
    让这个页面容器占满高度，并且内容溢出时允许滚动
  -->
  <div class="h-full overflow-y-auto custom-scrollbar p-6">
    
    <!-- 
      修改点 2: 给内容加一个底部内边距 pb-24
      防止滚动到底部时，按钮贴着屏幕边缘或者被遮挡
    -->
    <div class="max-w-3xl mx-auto mt-4 pb-24">
      
      <h1 class="text-3xl font-bold mb-8 text-white">上传歌曲</h1>

      <div class="bg-gray-800 rounded-xl p-8 shadow-xl border border-gray-700">
        
        <!-- 1. 音频文件选择 -->
        <div class="mb-8">
          <label class="block text-sm font-medium text-gray-400 mb-2">音频文件 (必选)</label>
          <div 
            @click="triggerAudioInput"
            class="border-2 border-dashed border-gray-600 rounded-lg p-8 text-center cursor-pointer hover:border-blue-500 hover:bg-gray-700/50 transition-all group"
          >
            <div v-if="!audioFile">
              <svg class="w-10 h-10 text-gray-500 mx-auto mb-2 group-hover:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3"></path></svg>
              <span class="text-gray-300">点击选择 MP3 / FLAC 文件</span>
            </div>
            <div v-else class="flex flex-col items-center justify-center gap-2">
              <div class="text-blue-400 font-bold text-lg break-all">{{ audioFile.name }}</div>
              <span class="text-xs text-gray-500">({{ (audioFile.size / 1024 / 1024).toFixed(2) }} MB)</span>
              <!-- 添加一个重新选择的提示 -->
              <span class="text-xs text-gray-400 mt-1 hover:text-white">点击可重新选择</span>
            </div>
          </div>
          <input type="file" ref="audioInputRef" class="hidden" accept="audio/*" @change="handleAudioChange">
        </div>

        <!-- 2. 信息填写 (两列布局) -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          
          <!-- 左侧：封面上传 -->
          <div>
             <label class="block text-sm font-medium text-gray-400 mb-2">封面图片 (可选)</label>
             <div 
               @click="triggerCoverInput"
               class="w-full aspect-square bg-gray-700/50 rounded-lg border-2 border-dashed border-gray-600 flex flex-col items-center justify-center cursor-pointer hover:border-blue-500 overflow-hidden relative group"
             >
                <img v-if="coverPreview" :src="coverPreview" class="w-full h-full object-cover" />
                <div v-else class="text-center p-4">
                  <svg class="w-8 h-8 text-gray-500 mx-auto mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                  <span class="text-xs text-gray-500 block">点击上传</span>
                  <span class="text-xs text-gray-600 block mt-1">留空则自动提取</span>
                </div>
                
                <!-- 移除按钮 -->
                <button v-if="coverFile" @click.stop="removeCover" class="absolute top-2 right-2 p-1 bg-red-600 rounded-full text-white opacity-0 group-hover:opacity-100 transition-opacity">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </button>
             </div>
             <input type="file" ref="coverInputRef" class="hidden" accept="image/*" @change="handleCoverChange">
          </div>

          <!-- 右侧：文本信息 -->
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-400 mb-1">歌曲标题</label>
              <input v-model="form.title" type="text" class="w-full bg-gray-900 border border-gray-600 rounded px-3 py-2 text-white focus:border-blue-500 outline-none" placeholder="留空则自动识别">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-400 mb-1">艺术家</label>
              <input v-model="form.artist" type="text" class="w-full bg-gray-900 border border-gray-600 rounded px-3 py-2 text-white focus:border-blue-500 outline-none" placeholder="留空则自动识别">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-400 mb-1">专辑</label>
              <input v-model="form.album" type="text" class="w-full bg-gray-900 border border-gray-600 rounded px-3 py-2 text-white focus:border-blue-500 outline-none" placeholder="留空则自动识别">
            </div>
          </div>
        </div>

        <!-- 3. 上传按钮 -->
        <button 
          @click="upload" 
          :disabled="!audioFile || uploading"
          class="w-full py-3 rounded-lg font-bold transition-all duration-200 flex items-center justify-center gap-2"
          :class="[
            !audioFile || uploading 
              ? 'bg-gray-700 text-gray-500 cursor-not-allowed border border-gray-600' 
              : 'bg-blue-600 hover:bg-blue-500 text-white shadow-lg hover:shadow-blue-500/30'
          ]"
        >
          <svg v-if="uploading" class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <svg v-else-if="!audioFile" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
             <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
          </svg>
          <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
             <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path>
          </svg>

          <span>
            {{ uploading ? '正在上传并处理...' : (audioFile ? '开始上传' : '请先选择音频文件') }}
          </span>
        </button>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '../utils/api'
import { useRouter } from 'vue-router'

const router = useRouter()
const audioInputRef = ref(null)
const coverInputRef = ref(null)

const audioFile = ref(null)
const coverFile = ref(null)
const coverPreview = ref(null)
const uploading = ref(false)

const form = ref({
  title: '',
  artist: '',
  album: ''
})

// 音频选择
const triggerAudioInput = () => audioInputRef.value.click()
const handleAudioChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    audioFile.value = file
    // 自动填充标题建议 (去掉后缀)
    if (!form.value.title) {
      form.value.title = file.name.replace(/\.[^/.]+$/, "")
    }
  }
}

// 封面选择
const triggerCoverInput = () => coverInputRef.value.click()
const handleCoverChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    coverFile.value = file
    coverPreview.value = URL.createObjectURL(file)
  }
}
const removeCover = () => {
  coverFile.value = null
  coverPreview.value = null
  if (coverInputRef.value) coverInputRef.value.value = ''
}

// 上传逻辑
const upload = async () => {
  if (!audioFile.value) return
  uploading.value = true

  try {
    const formData = new FormData()
    formData.append('file', audioFile.value)

    // 如果有填，就加进去；没填就不加，后端会处理空值
    if (form.value.title) formData.append('title', form.value.title)
    if (form.value.artist) formData.append('artist', form.value.artist)
    if (form.value.album) formData.append('album', form.value.album)

    if (coverFile.value) {
      formData.append('cover_image', coverFile.value)
    }

    // 发送请求
    // 注意：Axios 自动处理 multipart/form-data，不需要手动设置 header
    const res = await api.post('/api/songs/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    alert('上传成功！')

    // 清空表单
    audioFile.value = null
    coverFile.value = null
    coverPreview.value = null
    form.value = { title: '', artist: '', album: '' }
    if (audioInputRef.value) audioInputRef.value.value = ''
    if (coverInputRef.value) coverInputRef.value.value = ''

    // 可选：跳回歌曲列表
    // router.push('/songs')

  } catch (error) {
    console.error(error)
    alert('上传失败: ' + (error.response?.data?.error || error.message))
  } finally {
    uploading.value = false
  }
}
</script>