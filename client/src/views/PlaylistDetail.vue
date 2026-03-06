<template>
  <div class="h-full flex flex-col bg-gray-950 text-white">
    <div v-if="loading" v-loading="true" element-loading-background="transparent" class="flex-1"></div>

    <div v-else-if="playlist" class="h-full flex flex-col overflow-hidden">
      <div
        class="shrink-0 p-8 bg-linear-to-b from-blue-900/40 to-gray-950 flex items-end gap-8 border-b border-white/5">
        <div class="w-56 h-56 rounded-xl shadow-2xl overflow-hidden shrink-0 group relative">
          <img v-if="playlist.playlist_cover_url" :src="getImageUrl(playlist.playlist_cover_url)"
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
              {{ playlist.user.user_name.charAt(0).toUpperCase() }}
            </el-avatar>
            <span class="font-bold text-white">{{ playlist.user.user_name }}</span>
            <span class="opacity-50">•</span>
            <span>{{ playlist.song_count }} 首歌曲</span>
            <span class="opacity-50">•</span>
            <span>{{ playlist.song_count }} 次播放</span>
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

          <el-tooltip :content="playlist.is_liked ? '取消点赞' : '点赞'">
            <el-icon :size="32" class="cursor-pointer transition-colors"
              :class="playlist.is_liked ? 'text-red-500' : 'text-gray-400 hover:text-white'" @click="toggleLike">
              <StarFilled v-if="playlist.is_liked" />
              <Star v-else />
            </el-icon>
          </el-tooltip>

          <el-dropdown v-if="isOwner" trigger="click">
            <el-icon :size="28" class="text-gray-400 hover:text-white cursor-pointer">
              <MoreFilled />
            </el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="showAddSongModal = true" :icon="Plus">添加歌曲</el-dropdown-item>
                <el-dropdown-item @click="confirmDeletePlaylist" :icon="Delete" divided
                  class="text-red-500">删除歌单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="flex-1 overflow-hidden px-6">
        <el-table :data="playlist.songs" style="width: 100%" row-class-name="song-row" @row-dblclick="playSong"
          class="playlist-table">
          <el-table-column type="index" width="50" label="#" />
          <el-table-column label="标题" min-width="200">
            <template #default="{ row }">
              <div class="flex items-center gap-3">
                <span :class="{ 'text-green-500 font-bold': currentSong?.song_id === row.id }">{{ row.title }}</span>
                <el-icon v-if="currentSong?.song_id === row.id && isPlaying" class="text-green-500 animate-bounce">
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
          <el-table-column label="" width="100" align="center">
            <template #default="{ row }">
              <div class="flex gap-2 justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                <el-icon class="cursor-pointer hover:text-white" @click="addToQueue(row)">
                  <CirclePlus />
                </el-icon>
                <el-icon v-if="isOwner" class="cursor-pointer hover:text-red-500" @click="removeSong(row.id)">
                  <Delete />
                </el-icon>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="showAddSongModal" title="向歌单添加歌曲" width="500px" destroy-on-close>
      <el-input v-model="songSearchQuery" placeholder="搜索喜欢的歌曲..." :prefix-icon="Search" clearable
        @input="debouncedSearch" />
      <div class="mt-4 max-h-[400px] overflow-y-auto">
        <div v-for="song in searchResults" :key="song.song_id"
          class="flex justify-between items-center p-3 hover:bg-white/5 rounded-lg transition-colors group">
          <div class="min-w-0 pr-4">
            <div class="text-sm font-medium">{{ song.song_title }}</div>
            <div class="text-xs text-gray-500">{{ song.artist }}</div>
          </div>
          <el-button type="primary" size="small" plain @click="addSongToPlaylist(song.song_id)">添加</el-button>
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
import { Plus, Delete, Picture, CaretRight, Star, StarFilled, MoreFilled, Search, VideoPlay, CirclePlus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { debounce } from 'lodash-es'
import { IPlaylist, ISong } from '../../type'
import { playlistApi } from '../../api/playlistApi'
import { songApi } from '../../api/songApi'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()
const { currentSong, isPlaying } = storeToRefs(playerStore)

const playlist = ref<IPlaylist>()
const loading = ref(true)
const showAddSongModal = ref(false)
const songSearchQuery = ref('')
const searchResults = ref<ISong[]>([])

const isOwner = computed(() => {
  if (!userStore.isAuthenticated || !userStore.user || !playlist.value) {
    return false
  }

  // 2. 获取双方 ID
  const currentUserId = userStore.user.user_id
  const playlistCreatorId = playlist.value.creator_id

  // 3. 打印调试日志（按 F12 看控制台，修好后可删除）
  console.log(`[权限检查] 当前用户ID: ${currentUserId} (${typeof currentUserId})`)
  console.log(`[权限检查] 歌单作者ID: ${playlistCreatorId} (${typeof playlistCreatorId})`)

  // 4. 【核心修复】使用 String() 转为字符串后再比较，或者使用 == (双等号)
  // 推荐转字符串比较，最稳健
  return String(currentUserId) === String(playlistCreatorId)
})

// 处理图片路径
const getImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const API_BASE = 'http://127.0.0.1:5000'
  return `${API_BASE}${url.startsWith('/') ? '' : '/'}${url}`
}

// 加载歌单详情
const loadPlaylist = async () => {
  loading.value = true
  try {
    const playlistId = route.params.id[0];
    const res = await playlistApi.getPlaylistById(parseInt(playlistId))
    if (!res.success) {
      ElMessage.error("获取列表时出错")
      router.push('./playlist')
      return
    }
    playlist.value = res.playlist
  } catch (error) {
    console.error('Failed to load playlist:', error)
    router.push('/playlists') // 出错跳回列表
  } finally {
    loading.value = false
  }
}

const playAll = async () => {
  if (!playlist.value?.songs?.length) {
    ElMessage.error("歌单是空的")
    return
  }

  await playerStore.playPlaylist(playlist.value.playlist_id)
}

// 播放单曲：不改变队列，只插入播放
const playSong = (song: ISong) => {
  playerStore.playSong(song)
}

// 添加到队列
const addToQueue = async (song: ISong) => {
  console.log('点击添加:', song.song_title)
  const res = await playerStore.addToQueue(song, true)
  if (res.success) ElMessage.success("添加成功")
}

// 点赞/取消
const toggleLike = async () => {
  if (!userStore.isAuthenticated) {
    router.push('/login')
    return
  }

  if (playlist.value && playlist.value.is_liked) {
    const res = await playlistApi.likePlaylist(playlist.value.playlist_id)
    if (!res.success) {
      ElMessage.error("取消点赞失败")
      return
    }
    playlist.value.is_liked = false
    playlist.value.like_count--
    ElMessage.success("取消点赞成功")
  } else if (playlist.value && !playlist.value.is_liked) {
    const res = await playlistApi.unlikePlaylist(playlist.value.playlist_id)
    if (!res.success) {
      ElMessage.error("点赞失败")
      return
    }
    playlist.value.is_liked = false
    playlist.value.like_count--
    ElMessage.success("点赞成功")
  }
}


// 删除歌单
const confirmDeletePlaylist = () => {
  ElMessageBox.confirm('确定要永久删除这个歌单吗？', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '点错了',
    type: 'warning',
    buttonSize: 'default'
  }).then(() => {
    deletePlaylist()
  })
}

const deletePlaylist = async () => {
  if (!confirm('确定要删除这个歌单吗？此操作不可撤销。')) return
  try {
    const res = await playlistApi.deletePlaylist(playlist.value?.playlist_id || -1)
    if (!res.success) {
      ElMessage.error("删除失败")
    }
    ElMessage.success("删除成功")
    router.push('/playlists')
  } catch (error) {
    console.error('Failed to delete playlist:', error)
  }
}

// 从歌单移除歌曲
const removeSong = async (songId: number) => {
  try {
    const res = await playlistApi.removeSongFromPlaylist(songId, playlist.value?.playlist_id || -1)
    if (!res.success) {
      ElMessage.error("移除失败")
    }
    if (playlist.value) {
      const idx = playlist.value.songs.findIndex(s => s.song_id === songId)
      if (idx !== -1) playlist.value.songs.splice(idx, 1)
      playlist.value.song_count--
    }

  } catch (error) {
    console.error('Failed to remove song:', error)
  }
}

const searchSongs = async () => {
  if (!songSearchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    const res = await songApi.getSongs(20, songSearchQuery.value)
    if (!res.success) {
      ElMessage.error("获取歌曲失败")
      return
    }
    searchResults.value = res.songs || []
  } catch (error) {
    console.error('Failed to search songs:', error)
  }
}

const debouncedSearch = debounce(() => {
  searchSongs()
}, 500)


// 添加歌曲到歌单
const addSongToPlaylist = async (songId: number) => {
  try {
    if (!playlist.value) {
      ElMessage.warning('歌单信息尚未加载完毕')
      return
    }
    const res = await playlistApi.addSongToPlaylist(songId, playlist.value.playlist_id)
    if (!res.success) {
      ElMessage.error("添加失败")
      return
    }
    ElMessage.success("已加入到歌单")
    await loadPlaylist()
  } catch (error: any) {
    alert(error.response?.data?.error || '添加失败')
  }
}

const formatDuration = (seconds: number) => {
  if (!seconds) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

watch(songSearchQuery, searchSongs)

onMounted(loadPlaylist)

onUnmounted(() => {
  debouncedSearch.cancel()
})
</script>

<style scoped>
:deep(.song-row:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  cursor: pointer;
}

:deep(.playlist-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-border-color: rgba(255, 255, 255, 0.05);
  --el-table-text-color: #9ca3af;
  --el-table-header-text-color: #6b7280;
}

.overflow-y-auto {
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.1) transparent;
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