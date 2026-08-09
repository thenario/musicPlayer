import { ref } from 'vue'
import { playlistApi } from '@/api/playlistApi'
import { useAsyncTask } from '@/common'

export interface CreatePlaylistPayload {
  name: string
  description: string
  coverFile: File | null
  creatorId?: number
}

/** 歌单列表：加载 + 新建。 */
export function usePlaylistList() {
  const playlists = ref<any[]>([])
  const loadTask = useAsyncTask()
  const createTask = useAsyncTask()

  const load = async () => {
    await loadTask.run(async () => {
      const res = await playlistApi.getMyPlaylists()
      playlists.value = res.playlists
    })
  }

  const create = async (payload: CreatePlaylistPayload) => {
    await createTask.run(async () => {
      const formData = new FormData()
      formData.append('name', payload.name)
      formData.append('description', payload.description)
      formData.append('creator_id', String(payload.creatorId ?? 0))
      if (payload.coverFile) formData.append('cover_image', payload.coverFile)
      await playlistApi.createPlaylist(formData)
      await load()
    })
  }

  return { playlists, loading: loadTask.loading, creating: createTask.loading, load, create }
}
