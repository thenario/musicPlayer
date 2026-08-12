import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { playlistApi } from '@/api/playlistApi'
import { sameId } from '@/utils/format'
import type { IPlaylist, PlaylistSong } from '@/types'

/** 歌单详情：数据加载、点赞、增删歌；返回状态与操作供页面编排。 */
export function usePlaylistDetail() {
  const route = useRoute()
  const router = useRouter()
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

  const loadPlaylist = async () => {
    const idParam = route.params.id as string
    if (!idParam || Number.isNaN(Number(idParam))) {
      loading.value = false
      return
    }

    loading.value = true
    try {
      const res = await playlistApi.getPlaylistById(idParam)
      playlist.value = res.playlist
      songs.value = res.songs
        ? res.songs.sort((a, b) => a.song_playlist_position - b.song_playlist_position)
        : []
      isLiked.value = res.is_liked
    } catch (error) {
      // 错误已由拦截器统一提示，这里只记录日志
      console.error(error)
      router.push('/playlists')
    } finally {
      loading.value = false
    }
  }

  const toggleLike = async () => {
    if (!userStore.isAuthenticated) {
      router.push('/login')
      return
    }
    if (!playlist.value) return

    try {
      const res = isLiked.value
        ? await playlistApi.unlikePlaylist(playlist.value.playlist_id)
        : await playlistApi.likePlaylist(playlist.value.playlist_id)

      if (res.success) {
        isLiked.value = !isLiked.value
        playlist.value.like_count += isLiked.value ? 1 : -1
        ElMessage.success(isLiked.value ? "已点赞" : "取消点赞成功")
      }
    } catch (err) {
      // 错误已由拦截器统一提示，这里只记录日志
      console.log(err)
    }
  }

  const handleRemoveSong = async (songId: number | string) => {
    if (!playlist.value) return
    try {
      await playlistApi.removeSongFromPlaylist(playlist.value.playlist_id, songId)
      ElMessage.success("已从歌单移除")
      songs.value = songs.value.filter((s) => !sameId(s.song_id, songId))
      playlist.value.song_count--
    } catch (error) {
      // 错误已由拦截器统一提示，这里只记录日志
      console.error('Remove error:', error)
    }
  }

  const addSongToPlaylist = async (songId: number | string) => {
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
