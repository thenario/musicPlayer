<template>
  <div class="h-full flex flex-col px-6 pt-6">
    <div class="flex justify-between items-center mb-4 shrink-0">
      <h1 class="text-3xl font-bold">歌曲库</h1>
      <div class="flex space-x-4">
        <input v-model="searchKeyword" type="text" placeholder="搜索歌曲..."
          class="px-4 py-2 bg-gray-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">
      </div>
    </div>

    <SongsTable :songs="songs">
      <template #actions="{ song }">
        <div class="flex items-center">
          <button @click="handlePlay(song)" class="text-blue-400 hover:text-white mr-4">播放</button>
          <button @click="handleAddToQueue(song)"
            :class="addedSongId === song.song_id ? 'text-green-500' : 'text-gray-400'">
            {{ addedSongId === song.song_id ? '已添加' : '加入队列' }}
          </button>
        </div>
      </template>
    </SongsTable>

    <div class="py-4 flex justify-between items-center shrink-0">
      <button @click="prevPage" :disabled="currentPage === 1"
        class="px-4 py-2 bg-gray-700 rounded-lg disabled:opacity-50">
        上一页
      </button>
      <span class="text-gray-300">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
      <button @click="nextPage" :disabled="currentPage >= totalPages"
        class="px-4 py-2 bg-gray-700 rounded-lg disabled:opacity-50">
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { usePlayerStore } from '../stores/player'
import { songApi } from '../../api/songApi'
import { ElMessage } from 'element-plus'
import SongsTable from '../components/songsTable.vue'
import { ISong } from '../../type'

const playerStore = usePlayerStore()

const songs = ref<ISong[] | null>(null)
const searchKeyword = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const addedSongId = ref<number | null>()

const loadSongs = async () => {
  const res = await songApi.getSongs(currentPage.value, searchKeyword.value)
  if (res.success) {
    songs.value = res.songs ?? []
    totalPages.value = res.pagination?.total_pages || 1
  } else {
    ElMessage.error('获取列表失败')
  }
}

const handlePlay = (song: ISong) => playerStore.playSong(song)
const handleAddToQueue = async (song: ISong) => {
  await playerStore.addToQueue(song, true)
  addedSongId.value = song.song_id
  setTimeout(() => { if (addedSongId.value === song.song_id) addedSongId.value = null }, 1500)
}

const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }

// 防抖与监听
let debounceTimer: any = null
watch(searchKeyword, () => {
  currentPage.value = 1
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(loadSongs, 500)
})

watch(currentPage, loadSongs)
onMounted(loadSongs)
onUnmounted(() => clearTimeout(debounceTimer))
</script>