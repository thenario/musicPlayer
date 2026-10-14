import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { playlistApi } from '@/api/playlist-api'
import { sameId } from '@/utils/format'
import type { ActionResult, IPlaylist, PlaylistSong } from '@/types'
import { requireSuccess, toActionFailure } from '@/utils/action-result'

/** 歌单详情：数据加载、点赞、增删歌；返回状态与操作供页面编排。 */
export function usePlaylistDetail() {
  const route = useRoute()
  const userStore = useUserStore()

  const playlist = ref<IPlaylist | null>(null)
  const songs = ref<PlaylistSong[]>([])
  const isLiked = ref<boolean>(false)
  const loading = ref(true)
  const showAddSongModal = ref(false)

  const isOwner = computed(() => {
    if (!userStore.isAuthenticated || !userStore.user || !playlist.value) return false
    return String(userStore.user.user_id) === String(playlist.value.creator_id)
  })

  const loadPlaylist = async (): Promise<ActionResult> => {
    const idParam = route.params.id as string
    if (!idParam || Number.isNaN(Number(idParam))) {
      loading.value = false
      return { success: false, message: '歌单编号无效' }
    }

    loading.value = true
    try {
      const res = await playlistApi.getPlaylistById(idParam)
      playlist.value = res.playlist
      songs.value = res.songs
        ? res.songs.sort((a, b) => a.song_playlist_position - b.song_playlist_position)
        : []
      isLiked.value = res.is_liked
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '歌单加载失败')
    } finally {
      loading.value = false
    }
  }

  const toggleLike = async (): Promise<ActionResult> => {
    if (!userStore.isAuthenticated) {
      return { success: false, message: '请先登录', code: 401 }
    }
    if (!playlist.value) return { success: false, message: '歌单尚未加载' }

    try {
      const res = isLiked.value
        ? await playlistApi.unlikePlaylist(playlist.value.playlist_id)
        : await playlistApi.likePlaylist(playlist.value.playlist_id)

      if (res.success) {
        isLiked.value = !isLiked.value
        playlist.value.like_count += isLiked.value ? 1 : -1
      }
      return { success: true, message: isLiked.value ? '已点赞' : '取消点赞成功' }
    } catch (err) {
      return toActionFailure(err, '收藏操作失败')
    }
  }

  const handleRemoveSong = async (songId: number | string): Promise<ActionResult> => {
    if (!playlist.value) return { success: false, message: '歌单尚未加载' }
    try {
      await playlistApi.removeSongFromPlaylist(playlist.value.playlist_id, songId)
      songs.value = songs.value.filter((s) => !sameId(s.song_id, songId))
      playlist.value.song_count--
      return { success: true, message: '已从歌单移除' }
    } catch (error) {
      return toActionFailure(error, '移除歌曲失败')
    }
  }

  const addSongToPlaylist = async (songId: number | string): Promise<ActionResult> => {
    if (!playlist.value) return { success: false, message: '歌单尚未加载' }
    try {
      await playlistApi.addSongToPlaylist(playlist.value.playlist_id, songId)
      requireSuccess(await loadPlaylist(), '歌曲已添加，但歌单刷新失败')
      return { success: true, message: '添加成功' }
    } catch (error: unknown) {
      return toActionFailure(error, '添加歌曲失败')
    }
  }

  return {
    playlist,
    songs,
    isLiked,
    loading,
    showAddSongModal,
    isOwner,
    loadPlaylist,
    toggleLike,
    handleRemoveSong,
    addSongToPlaylist,
  }
}
