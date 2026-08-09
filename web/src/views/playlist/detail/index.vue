<template>
  <div class="playlist-detail">
    <div v-if="loading" v-loading="true" element-loading-background="transparent" class="playlist-detail__loading"></div>

    <template v-else-if="playlist">
      <PlaylistHeader
        :playlist="playlist"
        :is-owner="isOwner"
        :is-liked="isLiked"
        :user-name="user?.user_name || '未知用户'"
        @play-all="playAll"
        @toggle-like="toggleLike"
        @add-song="showAddSongModal = true"
        @delete="confirmDeletePlaylist"
      />

      <div class="playlist-detail__body">
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

    <div v-else class="playlist-detail__empty">
      <el-empty description="暂无歌单详情" />
    </div>

    <AddSongDialog v-model:open="showAddSongModal" @add="addSongToPlaylist" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'PlaylistDetailPage' })
import { useRouter } from 'vue-router'
import { usePlayerStore } from '@/stores/player'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { playlistApi } from '@/api/playlistApi'
import PlaylistHeader from './components/PlaylistHeader.vue'
import PlaylistSongTable from './components/PlaylistSongTable.vue'
import AddSongDialog from './components/AddSongDialog.vue'
import { usePlaylistDetail } from './composables/usePlaylistDetail'
import type { ISong } from '@/types'

const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()
const { currentSong, isPlaying } = storeToRefs(playerStore)
const { user } = storeToRefs(userStore)

const {
  playlist,
  songs,
  isLiked,
  loading,
  showAddSongModal,
  isOwner,
  toggleLike,
  handleRemoveSong,
  addSongToPlaylist,
} = usePlaylistDetail()

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
</script>

<style scoped>
@reference "../../../assets/index.css";

.playlist-detail {
  @apply h-full flex flex-col bg-gray-950 text-white;
}

.playlist-detail__loading {
  @apply flex-1;
}

.playlist-detail__body {
  @apply flex-1 overflow-hidden px-6;
}

.playlist-detail__empty {
  @apply flex-1 flex items-center justify-center;
}

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
