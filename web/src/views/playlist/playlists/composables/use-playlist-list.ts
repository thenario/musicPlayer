import { ref } from 'vue'
import { playlistApi } from '@/api/playlist-api'
import { useAsyncTask } from '@/common'
import type { IPlaylist } from '@/types'

export interface CreatePlaylistPayload {
  name: string
  description: string
  coverFile: File | null
  creatorId?: number | string
}

/** 歌单列表：加载 + 新建。错误提示由拦截器统一弹出，这里只吞掉避免未处理拒绝。 */
export function usePlaylistList() {
  const playlists = ref<IPlaylist[]>([])
  const loadTask = useAsyncTask()
  const createTask = useAsyncTask()

  const load = async () => {
    await loadTask.run(async () => {
      try {
        const res = await playlistApi.getMyPlaylists()
        playlists.value = res.playlists
      } catch (err) {
        console.error(err)
      }
    })
  }

  /** 创建成功返回 true，由调用方决定是否关闭弹窗。 */
  const create = async (payload: CreatePlaylistPayload): Promise<boolean> => {
    try {
      await createTask.run(async () => {
        const formData = new FormData()
        formData.append('name', payload.name)
        formData.append('description', payload.description)
        formData.append('creator_id', String(payload.creatorId ?? 0))
        if (payload.coverFile) formData.append('cover_image', payload.coverFile)
        await playlistApi.createPlaylist(formData)
        await load()
      })
      return true
    } catch (err) {
      console.error(err)
      return false
    }
  }

  return { playlists, loading: loadTask.loading, creating: createTask.loading, load, create }
}
