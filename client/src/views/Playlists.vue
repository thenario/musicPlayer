<template>
  <div>
    <!-- 头部：只保留标题 -->
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-white">我的歌单</h1>
      <!-- 原来的按钮已移除 -->
    </div>

    <!-- 歌单网格 -->
    <!-- 修改点1: 增加列数 (lg:3->4, xl:4->6)，减小间距 (gap-6->gap-4) -->
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4">

      <!-- 1. 新建歌单按钮 (作为网格的第一个元素) -->
      <div v-if="userStore.isAuthenticated" @click="showCreateModal = true"
        class="bg-gray-800/50 hover:bg-gray-700 rounded-lg p-3 cursor-pointer transition-all duration-200 border-2 border-dashed border-gray-700 hover:border-blue-500 group flex flex-col items-center justify-center min-h-[240px]">
        <div
          class="w-16 h-16 bg-gray-700 rounded-full flex items-center justify-center group-hover:bg-blue-600 transition-colors mb-3">
          <svg class="w-8 h-8 text-gray-400 group-hover:text-white" fill="none" stroke="currentColor"
            viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
          </svg>
        </div>
        <span class="text-gray-400 font-medium group-hover:text-white">新建歌单</span>
      </div>

      <!-- 2. 歌单列表 -->
      <!-- 修改点2: 减小 padding (p-4 -> p-3) -->
      <div v-for="playlist in playlists" :key="playlist.id"
        class="bg-gray-800 rounded-lg p-3 hover:bg-gray-700 cursor-pointer transition-all duration-300 relative group flex flex-col"
        @click="$router.push(`/playlists/${playlist.id}`)">
        <!-- 删除按钮 (保持不变) -->
        <button v-if="userStore.isAuthenticated" @click.stop="deletePlaylist(playlist.id)"
          class="absolute top-2 right-2 p-1.5 bg-black/60 rounded-full text-gray-400 hover:text-red-500 hover:bg-black z-20 opacity-0 group-hover:opacity-100 transition-all duration-200"
          title="删除歌单">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16">
            </path>
          </svg>
        </button>

        <!-- 封面图 -->
        <!-- 修改点3: 减小高度 (h-48 -> h-36) -->
        <div
          class="w-full h-36 bg-gray-700 rounded-md mb-3 flex-shrink-0 flex items-center justify-center overflow-hidden relative shadow-lg">
          <img v-if="playlist.cover_image" :src="getImageUrl(playlist.cover_image)"
            class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" alt="封面">
          <svg v-else class="w-12 h-12 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd"
              d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
              clip-rule="evenodd" />
          </svg>

          <!-- 悬停播放图标 (装饰) -->
          <div
            class="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
            <div
              class="w-10 h-10 bg-green-500 rounded-full flex items-center justify-center shadow-lg transform translate-y-2 group-hover:translate-y-0 transition-transform">
              <svg class="w-6 h-6 text-black fill-current ml-1" viewBox="0 0 24 24">
                <path d="M8 5v14l11-7z" />
              </svg>
            </div>
          </div>
        </div>

        <!-- 文字信息 -->
        <!-- 修改点4: 调整字体大小 -->
        <div class="flex-1 min-w-0">
          <h3 class="text-sm font-bold text-white mb-1 truncate" :title="playlist.name">{{ playlist.name }}</h3>
          <p class="text-xs text-gray-400 mb-2 truncate h-4">{{ playlist.description || '暂无描述' }}</p>
        </div>

        <!-- 底部统计 -->
        <div class="flex justify-between items-center text-xs text-gray-500 mt-auto pt-2 border-t border-gray-700">
          <span class="flex items-center gap-1">
            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3">
              </path>
            </svg>
            {{ playlist.song_count }}
          </span>
          <span class="flex items-center gap-1">
            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z">
              </path>
            </svg>
            {{ playlist.like_count }}
          </span>
        </div>
      </div>
    </div>

    <!-- 创建歌单模态框 (保持不变) -->
    <div v-if="showCreateModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 backdrop-blur-sm">
      <div class="bg-gray-800 p-6 rounded-lg w-[450px] shadow-2xl border border-gray-700 transform transition-all">
        <h3 class="text-xl font-bold mb-4 text-white">创建新歌单</h3>
        <form @submit.prevent="createPlaylist">
          <!-- 封面上传区域 -->
          <div class="mb-5">
            <label class="block text-gray-400 text-sm mb-2">歌单封面</label>
            <div @click="triggerFileInput"
              class="w-full h-32 bg-gray-700/50 rounded-lg border-2 border-dashed border-gray-600 flex flex-col items-center justify-center cursor-pointer hover:border-blue-500 hover:bg-gray-700 transition-all relative overflow-hidden group">
              <template v-if="previewUrl">
                <img :src="previewUrl" class="w-full h-full object-cover" />
                <button @click.stop="removeImage"
                  class="absolute top-2 right-2 p-1.5 bg-black/60 rounded-full text-white hover:bg-red-600 transition-colors">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12">
                    </path>
                  </svg>
                </button>
              </template>
              <template v-else>
                <div
                  class="w-10 h-10 bg-gray-600 rounded-full flex items-center justify-center mb-2 group-hover:scale-110 transition-transform">
                  <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                  </svg>
                </div>
                <p class="text-xs text-gray-400">点击上传封面</p>
              </template>
            </div>
            <input type="file" ref="fileInputRef" class="hidden" accept="image/*" @change="handleFileChange">
          </div>

          <!-- 输入框 -->
          <div class="mb-4">
            <label class="block text-gray-400 text-sm mb-2">歌单名称</label>
            <input v-model="newPlaylist.name" type="text" required
              class="w-full px-3 py-2 bg-gray-700 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 border border-gray-600 placeholder-gray-500"
              placeholder="给歌单起个名字">
          </div>
          <div class="mb-6">
            <label class="block text-gray-400 text-sm mb-2">描述</label>
            <textarea v-model="newPlaylist.description"
              class="w-full px-3 py-2 bg-gray-700 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 border border-gray-600 placeholder-gray-500"
              rows="3" placeholder="介绍一下这个歌单"></textarea>
          </div>

          <!-- 按钮 -->
          <div class="flex justify-end space-x-3">
            <button type="button" @click="closeModal"
              class="px-4 py-2 text-sm text-gray-300 hover:text-white transition-colors">取消</button>
            <button type="submit"
              class="px-6 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition-colors shadow-lg shadow-blue-900/20">创建</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
// Script 部分保持不变，逻辑完全兼容
import { ref, onMounted } from 'vue'
import api from '../utils/api'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const playlists = ref([])
const showCreateModal = ref(false)
const newPlaylist = ref({ name: '', description: '' })
const fileInputRef = ref(null)
const selectedFile = ref(null)
const previewUrl = ref(null)

const triggerFileInput = () => fileInputRef.value.click()

const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    previewUrl.value = URL.createObjectURL(file)
  }
}

const removeImage = () => {
  selectedFile.value = null
  previewUrl.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
}

const closeModal = () => {
  showCreateModal.value = false
  newPlaylist.value = { name: '', description: '' }
  removeImage()
}

const createPlaylist = async () => {
  try {
    const formData = new FormData()
    formData.append('name', newPlaylist.value.name)
    formData.append('description', newPlaylist.value.description)
    if (selectedFile.value) formData.append('cover_image', selectedFile.value)

    await api.post('/api/playlists', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    closeModal()
    await loadPlaylists()
  } catch (error) {
    console.error('Failed to create playlist:', error)
    alert('创建失败: ' + (error.response?.data?.error || '未知错误'))
  }
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const API_BASE = 'http://127.0.0.1:5000'
  return `${API_BASE}${url.startsWith('/') ? '' : '/'}${url}`
}

const loadPlaylists = async () => {
  try {
    const response = await api.get('/api/playlists/my')
    playlists.value = response.data.playlists || response.data
  } catch (error) {
    console.error('Failed to load playlists:', error)
  }
}

const deletePlaylist = async (id) => {
  if (!confirm('确定要删除这个歌单吗？')) return
  try {
    await api.delete(`/api/playlists/${id}`)
    await loadPlaylists()
  } catch (error) {
    console.error('Failed to delete:', error)
  }
}

onMounted(loadPlaylists)
</script>