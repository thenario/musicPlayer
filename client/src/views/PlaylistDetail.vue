<template>
  <div class="h-full flex flex-col bg-gray-950 text-white">

    <div v-if="loading" v-loading="true" element-loading-background="transparent" class="flex-1"></div>

    <div v-else-if="playlist" class="h-full flex flex-col overflow-hidden">
      <div
        class="shrink-0 p-8 bg-linear-to-b from-blue-900/40 to-gray-950 flex items-end gap-8 border-b border-white/5">
        <div class="w-56 h-56 rounded-xl shadow-2xl overflow-hidden shrink-0 group relative">
          <img v-if="playlist.playlist_cover_url" :src="getImageUrl(playlist.playlist_cover_url)" alt="歌曲封面"
            class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
          <div v-else class="w-full h-full bg-gray-800 flex flex-col items-center justify-center text-gray-500">
            <el-icon :size="64">
              <Picture />
            </el-icon>
          </div>
        </div>

        <div class="flex-1 min-w-0">
          <p class="text-xs font-bold uppercase tracking-wider mb-2 text-blue-400">歌单</p>
          <h1 class="text-6xl font-black mb-6 truncate">{{ playlist.playlist_name }}</h1>

          <div class="flex items-center gap-2 text-sm text-gray-300">
            <el-avatar :size="24" class="bg-blue-600 font-bold">
              {{ user ? user.user_name.charAt(0).toUpperCase() : "U" }}
            </el-avatar>
            <span class="font-bold text-white">{{ user ? user.user_name : "未知用户" }}</span>
            <span class="opacity-50">•</span>
            <span>{{ playlist.song_count || 0 }} 首歌曲</span>
            <span class="opacity-50">•</span>
            <span>{{ playlist.play_count || 0 }} 次播放</span>
          </div>
        </div>
      </div>

      <div class="p-6 flex items-center justify-between">
        <div class="flex items-center gap-6">
          <button @click="playAll"
            class="w-14 h-14 bg-green-500 rounded-full flex items-center justify-center shadow-lg hover:scale-105 transition-transform active:scale-95 group">
            <el-icon :size="30" color="black" class="ml-1">
              <CaretRight />
            </el-icon>
          </button>

          <el-tooltip :content="is_liked ? '取消点赞' : '点赞'">
            <el-icon :size="32" class="cursor-pointer transition-colors"
              :class="is_liked ? 'text-red-500' : 'text-gray-400 hover:text-white'" @click="toggleLike">
              <StarFilled v-if="is_liked" />
              <Star v-else />
            </el-icon>
          </el-tooltip>

          <el-dropdown v-if="isOwner" trigger="click">
            <el-icon :size="28" class="text-gray-400 hover:text-white cursor-pointer">
              <MoreFilled />
            </el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="showAddSongModal = true" :icon="Plus">添加歌曲到歌单</el-dropdown-item>
                <el-dropdown-item @click="confirmDeletePlaylist" :icon="Delete" divided
                  class="text-red-500">删除歌单</el-dropdown-item>
                <router-link :to="`/playlists/${playlist.playlist_id}/edit`" class="no-underline">
                  <el-dropdown-item :icon="Edit">编辑歌单信息</el-dropdown-item>
                </router-link>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="flex-1 overflow-hidden px-6">
        <el-table :data="songs" style="width: 100%" row-class-name="song-row group" @row-dblclick="playSong"
          class="playlist-table">
          <el-table-column type="index" width="50" label="#" />

          <el-table-column label="标题" min-width="200">
            <template #default="{ row }">
              <div class="flex items-center gap-3">
                <span :class="{ 'text-green-500 font-bold': currentSong?.song_id === row.song_id }">
                  {{ row.song_title }}
                </span>
                <el-icon v-if="currentSong?.song_id === row.song_id && isPlaying" class="text-green-500 animate-bounce">
                  <VideoPlay />
                </el-icon>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="artist" label="歌手" />
          <el-table-column prop="album" label="专辑" />

          <el-table-column label="时长" width="100" align="right">
            <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
          </el-table-column>

          <el-table-column label="操作" width="180" align="right">
            <template #default="{ row }">
              <div
                class="flex justify-end items-center gap-3 opacity-0 group-hover:opacity-100 transition-opacity pr-2">
                <el-tooltip content="立即播放">
                  <el-icon class="cursor-pointer text-blue-400 hover:text-blue-300" :size="20" @click="playSong(row)">
                    <VideoPlay />
                  </el-icon>
                </el-tooltip>

                <el-tooltip content="下一首播放">
                  <el-icon class="cursor-pointer text-gray-400 hover:text-white" :size="20"
                    @click="handlePlayNext(row)">
                    <List />
                  </el-icon>
                </el-tooltip>

                <el-tooltip content="添加到队列">
                  <el-icon class="cursor-pointer text-gray-400 hover:text-white" :size="20"
                    @click="handleAddToQueue(row)">
                    <CirclePlus />
                  </el-icon>
                </el-tooltip>

                <el-tooltip v-if="isOwner" content="从歌单移除">
                  <el-icon class="cursor-pointer text-gray-500 hover:text-red-500" :size="18"
                    @click="handleRemoveSong(row.song_id)">
                    <Delete />
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div v-else class="flex-1 flex items-center justify-center">
      <el-empty description="暂无歌单详情" />
    </div>

    <el-dialog v-model="showAddSongModal" title="向歌单添加歌曲" width="500px" destroy-on-close>
      <el-input v-model="songSearchQuery" placeholder="搜索歌名或歌手..." :prefix-icon="Search" clearable
        @input="debouncedSearch" />
      <div class="mt-4 max-h-[400px] overflow-y-auto custom-scrollbar">
        <div v-for="song in searchResults" :key="song.song_id"
          class="flex justify-between items-center p-3 hover:bg-white/5 rounded-lg transition-colors group">
          <div class="min-w-0 pr-4">
            <div class="text-sm font-medium">{{ song.song_title }}</div>
            <div class="text-xs text-gray-500">{{ song.artist }}</div>
          </div>
          <el-button type="primary" size="small" plain @click="addSongToPlaylist(song.song_id)">添加</el-button>
        </div>
        <div v-if="searchResults.length === 0 && songSearchQuery" class="text-center py-4 text-gray-500">
          未找到相关歌曲
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePlayerStore } from '../stores/player'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'
import { debounce } from 'lodash-es'
import type { IPlaylist, ISong } from '../../type'
import { playlistApi } from '../../axios/playlistApi'
import { songApi } from '../../axios/songApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Delete, Plus, Search, Edit,
  MoreFilled, Star, StarFilled,
  CaretRight, VideoPlay, List, CirclePlus
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()
const { user } = storeToRefs(userStore)
const { currentSong, isPlaying } = storeToRefs(playerStore)

const playlist = ref<IPlaylist | null>(null)
const songs = ref<ISong[]>([])
const is_liked = ref<boolean>(false)
const loading = ref(true)
const showAddSongModal = ref(false)
const songSearchQuery = ref('')
const searchResults = ref<ISong[]>([])

const isOwner = computed(() => {
  if (!userStore.isAuthenticated || !user.value || !playlist.value) return false
  return String(user.value.user_id) === String(playlist.value.creator_id)
})

const getImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `${import.meta.env.VITE_API_URL}${url.startsWith('/') ? '' : '/'}${url}`
}

const loadPlaylist = async () => {
  const idParam = route.params.id as string
  if (!idParam || Number.isNaN(Number(idParam))) {
    loading.value = false
    return
  }

  loading.value = true
  try {
    const res = await playlistApi.getPlaylistById(Number(idParam))
    playlist.value = res.playlist
    songs.value = res.songs || []
    is_liked.value = (res as any).is_liked
  } catch (error) {
    console.error(error)
    ElMessage.error("获取列表失败")
    router.push('/playlists')
  } finally {
    loading.value = false
  }
}

const playAll = async () => {
  if (!songs.value.length || !playlist.value) {
    ElMessage.warning("歌单是空的")
    return
  }
  const res = await playerStore.playPlaylist(playlist.value.playlist_id)
  if (!res.success) ElMessage.error("播放时出错")
}

const playSong = async (song: ISong) => {
  const res = await playerStore.playSong(song, "now")
  if (!res.success) ElMessage.error("播放时出错")
}

const handlePlayNext = async (song: ISong) => {
  const res = await playerStore.addToQueue(song, true)
  if (res.success) ElMessage.success(`《${song.song_title}》已设为下一首播放`)
  else ElMessage.error("设置时出错")
}

const handleAddToQueue = async (song: ISong) => {
  const res = await playerStore.addToQueue(song, false)
  if (res.success) ElMessage.success("已添加到播放队列")
  else ElMessage.error("添加时出错")
}

const toggleLike = async () => {
  if (!userStore.isAuthenticated) {
    router.push('/login')
    return
  }
  if (!playlist.value) return

  try {
    const res = is_liked.value
      ? await playlistApi.unlikePlaylist(playlist.value.playlist_id)
      : await playlistApi.likePlaylist(playlist.value.playlist_id)

    if (res.success) {
      is_liked.value = !is_liked.value
      playlist.value.like_count += is_liked.value ? 1 : -1
      ElMessage.success(is_liked.value ? "已点赞" : "取消点赞成功")
    }
  } catch (err) {
    console.log(err)
    ElMessage.error("操作失败")
  }
}

const confirmDeletePlaylist = () => {
  ElMessageBox.confirm('确定要永久删除这个歌单吗？此操作不可撤销。', '严重警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '点错了',
    type: 'warning'
  }).then(() => {
    deletePlaylistAction()
  })
}

const deletePlaylistAction = async () => {
  if (!playlist.value) return
  try {
    await playlistApi.deletePlaylist(playlist.value.playlist_id)
    ElMessage.success("歌单已删除")
    router.push('/playlists')
  } catch (error) {
    ElMessage.error("删除失败")
    console.error('Delete error:', error)
  }
}

const handleRemoveSong = async (songId: number) => {
  if (!playlist.value) return
  try {
    await playlistApi.removeSongFromPlaylist(playlist.value.playlist_id, songId)
    ElMessage.success("已从歌单移除")
    songs.value = songs.value.filter(s => s.song_id !== songId)
    playlist.value.song_count--
  } catch (error) {
    ElMessage.error("移除失败")
    console.error('Remove error:', error)
  }
}

const searchSongs = async () => {
  if (!songSearchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    const res = await songApi.getSongs(1, songSearchQuery.value)
    searchResults.value = res.songs || []
  } catch (error) {
    console.error('Search error:', error)
  }
}

const debouncedSearch = debounce(searchSongs, 500)

const addSongToPlaylist = async (songId: number) => {
  if (!playlist.value) return
  try {
    await playlistApi.addSongToPlaylist(playlist.value.playlist_id, songId)
    ElMessage.success("添加成功")
    await loadPlaylist()
  } catch (error: any) {
    console.log(error)
    ElMessage.warning("添加失败")
  }
}

const formatDuration = (seconds: number) => {
  if (!seconds) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

watch(() => route.params.id, (newId) => {
  if (newId) loadPlaylist()
})

onMounted(loadPlaylist)
onUnmounted(() => {
  debouncedSearch.cancel()
})
</script>

<style scoped>
:deep(.song-row:hover) {
  background-color: rgba(255, 255, 255, 0.05) !important;
  cursor: default;
}

:deep(.playlist-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-border-color: rgba(255, 255, 255, 0.05);
  --el-table-text-color: #9ca3af;
  --el-table-header-text-color: #6b7280;
}

:deep(.el-table__inner-wrapper::before) {
  display: none;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #374151;
  border-radius: 10px;
}

:deep(.el-dialog) {
  background-color: #111827;
  border-radius: 12px;
}

:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.05) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}
</style>