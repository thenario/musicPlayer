<template>
  <!-- 1. 最外层：h-full 占满主区域，flex-col 垂直排列 -->
  <div class="h-full flex flex-col px-6 pt-6">

    <!-- 2. 头部搜索栏：shrink-0 固定高度 -->
    <div class="flex justify-between items-center mb-4 shrink-0">
      <h1 class="text-3xl font-bold">歌曲库</h1>
      <div class="flex space-x-4">
        <input v-model="searchQuery" type="text" placeholder="搜索歌曲..."
          class="px-4 py-2 bg-gray-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">
        <button v-if="userStore.isAuthenticated" @click="refreshSongs" :disabled="refreshing"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
          {{ refreshing ? '刷新中...' : '刷新服务器歌曲' }}
        </button>
      </div>
    </div>

    <!-- 3. 中间列表容器：flex-1 自动撑满剩余空间，min-h-0 防止溢出 -->
    <div class="bg-gray-800 rounded-lg flex-1 overflow-hidden flex flex-col min-h-0 relative">

      <!-- 滚动区域：只有这里面会滚动 -->
      <div class="overflow-y-auto flex-1 custom-scrollbar">
        <table class="w-full">
          <thead class="bg-gray-700 sticky top-0 z-10 shadow-sm">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">歌曲</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">艺术家</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">专辑</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">时长</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-700">
            <tr v-for="song in songs" :key="song.id" class="hover:bg-gray-700 transition-colors group">
              <!-- ... 列表内容保持不变 ... -->
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-white">{{ song.title }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-300">{{ song.artist }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-300">{{ song.album }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-300">
                {{ formatDuration(song.duration) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <div class="flex items-center">
                  <!-- 1. 播放按钮 -->
                  <button @click.stop="handlePlay(song)"
                    class="text-blue-400 hover:text-white hover:bg-blue-500 rounded-full p-1.5 transition-all duration-200 opacity-0 group-hover:opacity-100"
                    title="播放">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 fill-current" viewBox="0 0 20 20"
                      fill="currentColor">
                      <path fill-rule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z"
                        clip-rule="evenodd" />
                    </svg>
                  </button>

                  <!-- 2. 添加到队列按钮 (带反馈) -->
                  <button @click.stop="handleAddToQueue(song)"
                    class="ml-3 rounded-full p-1.5 transition-all duration-200 opacity-0 group-hover:opacity-100"
                    :class="addedSongId === song.id ? 'text-green-500 opacity-100' : 'text-gray-400 hover:text-white hover:bg-gray-600'"
                    :title="addedSongId === song.id ? '已添加' : '加入队列'" :disabled="addedSongId === song.id">
                    <!-- 状态 A: 成功 (对勾) -->
                    <svg v-if="addedSongId === song.id" class="w-5 h-5" fill="none" stroke="currentColor"
                      viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>

                    <!-- 状态 B: 默认 (加号) -->
                    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 4. 底部页码：shrink-0 固定高度，放在 flex-1 容器外面！ -->
    <!-- py-4 给上下留点呼吸空间 -->
    <div class="py-4 flex justify-between items-center shrink-0">
      <button @click="prevPage" :disabled="currentPage === 1"
        class="px-4 py-2 bg-gray-700 rounded-lg hover:bg-gray-600 disabled:opacity-50 transition-colors">
        上一页
      </button>
      <span class="text-gray-300 text-sm">
        第 {{ currentPage }} 页 / 共 {{ totalPages }} 页
      </span>
      <button @click="nextPage" :disabled="currentPage >= totalPages"
        class="px-4 py-2 bg-gray-700 rounded-lg hover:bg-gray-600 disabled:opacity-50 transition-colors">
        下一页
      </button>
    </div>

  </div>
</template>

<script setup>

import { ref, onMounted, watch } from 'vue'
import api from '../utils/api'
import { usePlayerStore } from '../stores/player'
import { useUserStore } from '../stores/user'

const playerStore = usePlayerStore()
const userStore = useUserStore()

const songs = ref([])
const searchQuery = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const refreshing = ref(false)

const addedSongId = ref(null)


const loadSongs = async () => {
  try {
    const response = await api.get('/api/songs', {
      params: {
        page: currentPage.value,
        search: searchQuery.value
      }
    })
    songs.value = response.data.songs
    totalPages.value = response.data.pages
  } catch (error) {
    console.error('Failed to load songs:', error)
  }
}

// 【修改】播放逻辑：直接调用 store，它会自动处理插队
const handlePlay = (song) => {
  playerStore.playSong(song)
}

// 【新增】添加到队列逻辑：带视觉反馈
const handleAddToQueue = async (song) => {
  // 1. 调用 Store 添加到末尾 (false)
  await playerStore.addToQueue(song, true)

  // 2. 触发成功动画
  addedSongId.value = song.id
  setTimeout(() => {
    if (addedSongId.value === song.id) {
      addedSongId.value = null
    }
  }, 1500)
}


const playSong = (song) => {
  playerStore.playSong(song)
}

const addToQueue = async (songId) => {
  await playerStore.addToQueue(songId)
}

const playSongAndAddToQueue = (song, songId) => {
  playSong(song)
  addToQueue(songId)
}

const refreshSongs = async () => {
  refreshing.value = true
  try {
    await api.post('/api/songs/refresh_server_songs')
    await loadSongs()
  } catch (error) {
    console.error('Failed to refresh songs:', error)
  } finally {
    refreshing.value = false
  }
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    loadSongs()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadSongs()
  }
}

const formatDuration = (seconds) => {
  if (!seconds) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 监听搜索查询变化
watch(searchQuery, () => {
  currentPage.value = 1
  loadSongs()
})

onMounted(() => {
  loadSongs()
})
</script>