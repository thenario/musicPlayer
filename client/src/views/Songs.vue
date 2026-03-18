<template>
  <div class="h-full flex flex-col px-6 pt-6 bg-gray-900 text-white">
    <div class="flex justify-between items-center mb-4 shrink-0">
      <h1 class="text-3xl font-bold">歌曲库</h1>
      <input v-model="searchKeyword" type="text" placeholder="搜索歌曲..."
        class="px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
    </div>

    <div class="flex-1 overflow-y-auto border border-gray-800 rounded-lg">
      <table class="w-full text-left border-collapse">
        <thead class="bg-gray-800 sticky top-0">
          <tr>
            <th class="p-4 border-b border-gray-700">歌名</th>
            <th class="p-4 border-b border-gray-700">歌手</th>
            <th class="p-4 border-b border-gray-700 text-right">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="song in songs" :key="song.song_id" class="hover:bg-gray-800/50 transition-colors">
            <td class="p-4 border-b border-gray-800">{{ song.song_title }}</td>
            <td class="p-4 border-b border-gray-800">{{ song.artist }}</td>
            <td class="p-4 border-b border-gray-800 text-right">
              <button @click="handlePlay(song)" class="text-blue-400 hover:text-blue-300 mr-4">播放</button>
              <button @click="handleAddToQueue(song)"
                :class="addedSongId === song.song_id ? 'text-green-500' : 'text-gray-400 hover:text-white'">
                {{ addedSongId === song.song_id ? '已添加' : '加入队列' }}
              </button>
            </td>
          </tr>
          <tr v-if="!songs?.length">
            <td colspan="3" class="p-10 text-center text-gray-500">暂无歌曲</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="py-6 flex justify-between items-center shrink-0">
      <button @click="prevPage" :disabled="currentPage === 1"
        class="px-4 py-2 bg-gray-800 rounded-lg disabled:opacity-30 hover:bg-gray-700">
        上一页
      </button>
      <span class="text-gray-400">第 {{ currentPage }} / {{ totalPages }} 页</span>
      <button @click="nextPage" :disabled="currentPage >= totalPages"
        class="px-4 py-2 bg-gray-800 rounded-lg disabled:opacity-30 hover:bg-gray-700">
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { usePlayerStore } from '../stores/player'
import { songApi } from '../../axios/songApi'
import { ISong } from '../../type'

const playerStore = usePlayerStore()
const songs = ref<ISong[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const addedSongId = ref<number | null>(null)
let timers: number[] = []

const loadSongs = async () => {
  try {
    const res = await songApi.getSongs(currentPage.value, searchKeyword.value)
    if (res.success) {
      songs.value = res.songs ?? []
      totalPages.value = res.pagination?.total_pages || 1
    }
  } catch (e) {
    console.error('加载失败', e)
  }
}

const handlePlay = (song: ISong) => playerStore.playSong(song)

const handleAddToQueue = async (song: ISong) => {
  await playerStore.addToQueue(song, true)
  addedSongId.value = song.song_id
  const timer = window.setTimeout(() => {
    addedSongId.value = null
  }, 1500)
  timers.push(timer)
}

const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }

let debounceTimer: number | null = null
watch(searchKeyword, () => {
  currentPage.value = 1
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = window.setTimeout(loadSongs, 500)
})

watch(currentPage, loadSongs)
onMounted(loadSongs)
onUnmounted(() => {
  if (debounceTimer) clearTimeout(debounceTimer)
  timers.forEach(t => clearTimeout(t))
})
</script>