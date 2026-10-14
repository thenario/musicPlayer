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
import { onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reportError } from '@/utils/feedback'
import { usePlayerStore } from '@/stores/player'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { playlistApi } from '@/api/playlist-api'
import PlaylistHeader from './components/PlaylistHeader.vue'
import PlaylistSongTable from './components/PlaylistSongTable.vue'
import AddSongDialog from './components/AddSongDialog.vue'
import { usePlaylistDetail } from './composables/use-playlist-detail'
import type { ISong } from '@/types'

const router = useRouter()
const route = useRoute()
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
  loadPlaylist,
  toggleLike: toggleLikeAction,
  handleRemoveSong: removeSongAction,
  addSongToPlaylist: addSongAction,
} = usePlaylistDetail()

const load = async () => {
  const result = await loadPlaylist()
  if (!result.success) reportError(result)
}
onMounted(load)
watch(() => route.params.id, () => { void load() })

const toggleLike = async () => {
  const result = await toggleLikeAction()
  if (!result.success) {
    reportError(result)
    if (result.code === 401) await router.push('/login')
    return
  }
  ElMessage.success(result.message || '操作成功')
}

const handleRemoveSong = async (songId: number | string) => {
  const result = await removeSongAction(songId)
  if (!result.success) return reportError(result)
  ElMessage.success(result.message || '已从歌单移除')
}

const addSongToPlaylist = async (songId: number | string) => {
  const result = await addSongAction(songId)
  if (!result.success) return reportError(result)
  ElMessage.success(result.message || '添加成功')
}

const playAll = async () => {
  if (!songs.value.length || !playlist.value) {
    ElMessage.warning("歌单是空的")
    return
  }
  const res = await playerStore.playPlaylist(playlist.value.playlist_id)
  if (res.success) ElMessage.success("开始播放歌单")
  else reportError(res)
}

const playSong = async (song: ISong) => {
  const result = await playerStore.playSong(song, 'now')
  if (!result.success) reportError(result)
}

const handlePlayNext = async (song: ISong) => {
  const res = await playerStore.addToQueue(song, false)
  if (res.success) ElMessage.success(`《${song.song_title}》已设为下一首播放`)
  else reportError(res)
}

const handleAddToQueue = async (song: ISong) => {
  const res = await playerStore.addToQueue(song, false)
  if (res.success) ElMessage.success("已添加到播放队列")
  else reportError(res)
}

const confirmDeletePlaylist = async () => {
  try {
    await ElMessageBox.confirm('确定要永久删除这个歌单吗？此操作不可撤销。', '严重警告', {
      confirmButtonText: '确定删除', cancelButtonText: '点错了', type: 'warning',
    })
  } catch (error) {
    // 取消和关闭不提示；确认框自身的其他异常仍记录并提示。
    reportError(error)
    return
  }
  await deletePlaylistAction()
}

const deletePlaylistAction = async () => {
  if (!playlist.value) return
  try {
    await playlistApi.deletePlaylist(playlist.value.playlist_id)
    ElMessage.success("歌单已删除")
    router.push('/playlists')
  } catch (error) {
    reportError(error)
  }
}
</script>

<style scoped>
.playlist-detail {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: var(--music-canvas);
  color: var(--music-text);
}

.playlist-detail__loading {
  flex: 1;
}

.playlist-detail__body {
  flex: 1;
  min-height: 0;
  padding: 0 24px 24px;
  overflow: hidden;
}

.playlist-detail__empty {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
}

:deep(.song-row:hover) {
  background-color: var(--music-hover) !important;
  cursor: default;
}

:deep(.playlist-table) {
  --el-table-bg-color: #fff;
  --el-table-tr-bg-color: #fff;
  --el-table-header-bg-color: var(--music-canvas);
  --el-table-border-color: var(--music-border);
  --el-table-text-color: var(--music-text);
  --el-table-header-text-color: var(--music-secondary);
  border: 1px solid var(--music-border);
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table__inner-wrapper::before) {
  display: none;
}
</style>
