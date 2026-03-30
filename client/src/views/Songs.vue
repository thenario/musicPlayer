<template>
  <div class="h-full flex flex-col bg-gray-950 text-white p-6">
    <!-- 1. 顶部搜索栏 -->
    <div class="flex justify-between items-center mb-6 shrink-0">
      <h1 class="text-3xl font-black tracking-tight">歌曲库</h1>
      <div class="relative group">
        <el-input v-model="searchKeyword" placeholder="搜索歌名、歌手或专辑..." :prefix-icon="Search" clearable
          class="custom-search-input" @input="debouncedSearch" />
      </div>
    </div>

    <div class="flex-1 overflow-hidden">
      <el-table v-loading="loading" element-loading-background="rgba(0, 0, 0, 0.5)" :data="songs" style="width: 100%"
        height="100%" row-class-name="song-row group" @row-dblclick="handlePlayNow" class="all-songs-table">
        <!-- ... 列定义保持不变 ... -->
        <el-table-column label="标题" min-width="250">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded overflow-hidden shrink-0 bg-gray-800">
                <img v-if="row.song_cover_url" :src="getImageUrl(row.song_cover_url)"
                  class="w-full h-full object-cover" />
                <el-icon v-else class="w-full h-full flex items-center justify-center text-gray-600">
                  <Headset />
                </el-icon>
              </div>
              <div class="truncate">
                <div
                  :class="['font-medium truncate', currentSong?.song_id === row.song_id ? 'text-green-500' : 'text-white']">
                  {{ row.song_title }}
                </div>
                <div class="text-xs text-gray-500 truncate">{{ row.artist }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="album" label="专辑" min-width="150" class-name="text-gray-400" />

        <el-table-column label="时长" width="100" align="right">
          <template #default="{ row }">
            <span class="text-gray-500 text-sm">{{ formatDuration(row.duration) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" align="right">
          <template #default="{ row }">
            <div class="flex justify-end items-center gap-3 opacity-0 group-hover:opacity-100 transition-opacity pr-2">
              <el-tooltip content="立即播放" placement="top">
                <el-icon class="action-icon text-blue-400 hover:text-blue-300" :size="20" @click="handlePlayNow(row)">
                  <VideoPlay />
                </el-icon>
              </el-tooltip>
              <el-tooltip content="下一首播放" placement="top">
                <el-icon class="action-icon text-gray-400 hover:text-white" :size="20" @click="handlePlayNext(row)">
                  <List />
                </el-icon>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="py-6 flex justify-center shrink-0">
      <el-pagination v-model:current-page="currentPage" :total="totalSongs" :page-size="pageSize"
        layout="prev, pager, next" background @current-change="loadSongs" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { usePlayerStore } from '../stores/player'
import { songApi } from '../../axios/songApi'
import type { ISong } from '../../type'
import { debounce } from 'lodash-es'
import {
  Search, VideoPlay, List, Headset
} from '@element-plus/icons-vue'

const playerStore = usePlayerStore()
const { currentSong, isPlaying } = storeToRefs(playerStore)

const songs = ref<ISong[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const totalSongs = ref(0)
const pageSize = ref(15)

const loadSongs = async () => {
  loading.value = true
  try {
    const res = await songApi.getSongs(currentPage.value, searchKeyword.value)
    songs.value = res.songs || []
    totalSongs.value = res.pagination?.total_items || (res.songs.length > 0 ? 100 : 0)
  } catch (err) {
    console.error(err)
    ElMessage.error("加载时出错")
  } finally {
    loading.value = false
  }
}

const handlePlayNow = async (song: ISong) => {
  const res = await playerStore.playSong(song, "now")
  if (!res.success) ElMessage.error("播放失败")
}

const handlePlayNext = async (song: ISong) => {
  const res = await playerStore.playSong(song, "next")
  if (res.success) {
    return ElMessage(
      `已将《${song.song_title}》添加到下一首播放`,
    )
  }
  return ElMessage("添加失败")
}

const debouncedSearch = debounce(() => {
  currentPage.value = 1
  loadSongs()
}, 500)

const formatDuration = (seconds: number) => {
  if (!seconds) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

const getImageUrl = (url: string) => {
  if (!url) return ''
  return url.startsWith('http') ? url : `${import.meta.env.VITE_API_URL}${url}`
}

onMounted(loadSongs)

onUnmounted(() => {
  debouncedSearch.cancel()
})
</script>

<style scoped>
:deep(.el-table__inner-wrapper::before) {
  display: none;
}

:deep(.el-scrollbar__wrap) {
  overflow-x: hidden !important;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb) {
  background-color: #374151;
  border-radius: 10px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-track) {
  background-color: transparent;
}

:deep(.all-songs-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-border-color: rgba(255, 255, 255, 0.05);
  --el-table-text-color: #9ca3af;
  --el-table-header-text-color: #6b7280;
}

:deep(.song-row:hover) {
  background-color: rgba(255, 255, 255, 0.05) !important;
}

.action-icon {
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-icon:active {
  transform: scale(0.9);
}

.custom-search-input {
  width: 300px;
}

:deep(.custom-search-input .el-input__wrapper) {
  background-color: #1f2937 !important;
  box-shadow: none !important;
  border: 1px solid #374151 !important;
  border-radius: 99px;
}

:deep(.custom-search-input .el-input__inner) {
  color: white !important;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-active)) {
  background-color: #1f2937;
  color: #9ca3af;
}

:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .btn-prev) {
  background-color: #1f2937;
  color: #9ca3af;
}
</style>