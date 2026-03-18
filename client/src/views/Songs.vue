<template>
  <div class="h-full flex flex-col px-6 pt-6 bg-gray-900 text-white font-sans">
    <div class="flex justify-between items-center mb-6 shrink-0">
      <h1 class="text-3xl font-bold tracking-tight">歌曲库</h1>
      <div class="relative">
        <span class="absolute inset-y-0 left-3 flex items-center text-gray-400">
          <SearchIcon :size="18" />
        </span>
        <input v-model="searchKeyword" type="text" placeholder="搜索歌名或歌手..."
          class="pl-10 pr-4 py-2 bg-gray-800 border border-gray-700 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all w-64">
      </div>
    </div>

    <div class="flex-1 overflow-y-auto border border-gray-800 rounded-xl bg-gray-900/50 backdrop-blur-sm">
      <table class="w-full text-left border-collapse">
        <thead class="bg-gray-800/80 sticky top-0 z-10 backdrop-blur-md">
          <tr>
            <th class="p-4 border-b border-gray-700 text-gray-400 font-medium">歌名</th>
            <th class="p-4 border-b border-gray-700 text-gray-400 font-medium">歌手</th>
            <th class="p-4 border-b border-gray-700 text-gray-400 font-medium text-right">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="song in songs" :key="song.song_id" class="group hover:bg-white/5 transition-colors">
            <td class="p-4 border-b border-gray-800 font-medium">{{ song.song_title }}</td>
            <td class="p-4 border-b border-gray-800 text-gray-400">{{ song.artist }}</td>
            <td class="p-4 border-b border-gray-800 text-right">
              <div class="flex justify-end items-center gap-2">
                <button @click="handlePlay(song)"
                  class="p-2.5 text-blue-400 hover:bg-blue-500/20 rounded-full transition-all active:scale-90"
                  title="立即播放">
                  <PlayIcon :size="20" fill="currentColor" />
                </button>

                <button @click="handleAddToQueue(song)"
                  class="p-2.5 rounded-full transition-all active:scale-90 flex items-center justify-center"
                  :class="addedSongId === song.song_id ? 'text-green-500 bg-green-500/10' : 'text-gray-400 hover:bg-white/10 hover:text-white'"
                  title="加入队列">
                  <CheckIcon v-if="addedSongId === song.song_id" :size="20" stroke-width="3" />
                  <ListPlusIcon v-else :size="20" />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!songs?.length">
            <td colspan="3" class="p-20 text-center">
              <div class="flex flex-col items-center text-gray-600">
                <MusicIcon :size="48" class="mb-2 opacity-20" />
                <p>未找到相关歌曲</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="py-6 flex justify-between items-center shrink-0">
      <button @click="prevPage" :disabled="currentPage === 1"
        class="flex items-center gap-2 px-4 py-2 bg-gray-800 rounded-lg disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-700 transition-colors">
        <ChevronLeftIcon :size="18" /> 上一页
      </button>

      <div class="px-4 py-1.5 bg-gray-800/50 rounded-full border border-gray-700 text-sm">
        <span class="text-white font-bold">{{ currentPage }}</span>
        <span class="mx-2 text-gray-600">/</span>
        <span class="text-gray-400">{{ totalPages }}</span>
      </div>

      <button @click="nextPage" :disabled="currentPage >= totalPages"
        class="flex items-center gap-2 px-4 py-2 bg-gray-800 rounded-lg disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-700 transition-colors">
        下一页
        <ChevronRightIcon :size="18" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { usePlayerStore } from '../stores/player'
import { songApi } from '../../axios/songApi'
import { ISong } from '../../type'
// 导入图标
import {
  Play as PlayIcon,
  ListPlus as ListPlusIcon,
  Check as CheckIcon,
  Search as SearchIcon,
  Music as MusicIcon,
  ChevronLeft as ChevronLeftIcon,
  ChevronRight as ChevronRightIcon
} from 'lucide-vue-next'

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
  const timer = globalThis.setTimeout(() => {
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
  debounceTimer = globalThis.setTimeout(loadSongs, 500)
})

watch(currentPage, loadSongs)
onMounted(loadSongs)
onUnmounted(() => {
  if (debounceTimer) clearTimeout(debounceTimer)
  timers.forEach(t => clearTimeout(t))
})
</script>

<style scoped>
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background-color: #374151;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}
</style>