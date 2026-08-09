<template>
  <div class="h-full flex flex-col bg-gray-950 text-white">
    <div v-if="loading" v-loading="true" element-loading-background="transparent" class="flex-1"></div>

    <template v-else-if="playlist">
      <PlaylistHeader
        :playlist="playlist"
        :is-owner="isOwner"
        :is-liked="is_liked"
        :user-name="user?.user_name || '未知用户'"
        @play-all="playAll"
        @toggle-like="toggleLike"
        @add-song="showAddSongModal = true"
        @delete="confirmDeletePlaylist"
      />

      <div class="flex-1 overflow-hidden px-6">
        <PlaylistSongTable
          :songs="songs"
          :is-owner="isOwner"
          :current-song-id="currentSong?.song_id"
          :is-playing="isPlaying"
          @play-song="playSong"
          @play-next="handlePlayNext"
          @add-to-queue="handleAddToQueue"
          @remove-song="handleRemoveSong"
        />
      </div>
    </template>

    <div v-else class="flex-1 flex items-center justify-center">
      <el-empty description="暂无歌单详情" />
    </div>

    <AddSongDialog v-model:open="showAddSongModal" @add="addSongToPlaylist" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePlayerStore } from '@/stores/player'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import type { IPlaylist, ISong } from '@/types'
import { playlistApi } from '@/api/playlistApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import PlaylistHeader from './components/PlaylistHeader.vue'
import PlaylistSongTable from './components/PlaylistSongTable.vue'
import AddSongDialog from './components/AddSongDialog.vue'

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

const isOwner = computed(() => {
  if (!userStore.isAuthenticated || !user.value || !playlist.value) return false
  return String(user.value.user_id) === String(playlist.value.creator_id)
})

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
    songs.value = res.songs ? res.songs.sort((a: any, b: any) => a.song_playlist_position - b.song_playlist_position) : []
    is_liked.value = (res as any).is_liked
  } catch (error) {
    // 错误已由拦截器统一提示，这里只记录日志
    console.error(error)
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
  if (res.success) ElMessage.success("开始播放歌单")
}

const playSong = async (song: ISong) => {
  await playerStore.playSong(song, "now")
}

const handlePlayNext = async (song: ISong) => {
  const res = await playerStore.addToQueue(song, true)
  if (res.success) ElMessage.success(`《${song.song_title}》已设为下一首播放`)
}

const handleAddToQueue = async (song: ISong) => {
  const res = await playerStore.addToQueue(song, false)
  if (res.success) ElMessage.success("已添加到播放队列")
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
    // 错误已由拦截器统一提示，这里只记录日志
    console.log(err)
  }
}

const confirmDeletePlaylist = () => {
  ElMessageBox.confirm('确定要永久删除这个歌单吗？此操作不可撤销。', '严重警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '点错了',
    type: 'warning',
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
    // 错误已由拦截器统一提示，这里只记录日志
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
    // 错误已由拦截器统一提示，这里只记录日志
    console.error('Remove error:', error)
  }
}

const addSongToPlaylist = async (songId: number) => {
  if (!playlist.value) return
  try {
    await playlistApi.addSongToPlaylist(playlist.value.playlist_id, songId)
    ElMessage.success("添加成功")
    await loadPlaylist()
  } catch (error: any) {
    // 错误已由拦截器统一提示，这里只记录日志
    console.log(error)
  }
}

watch(() => route.params.id, (newId) => {
  if (newId) loadPlaylist()
})

onMounted(loadPlaylist)
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
</style>
