import { ref } from 'vue'
import { playlistApi } from '@/api/playlist-api'
import { useAsyncTask } from '@/common'
import type { ActionResult, IPlaylist } from '@/types'
import { requireSuccess, toActionFailure } from '@/utils/action-result'

export interface CreatePlaylistPayload {
  name: string
  description: string
  coverFile: File | null
  creatorId?: number | string
}

/** 歌单列表业务返回结果，由页面决定提示和弹窗状态。 */
export function usePlaylistList() {
  const playlists = ref<IPlaylist[]>([])
  const loadTask = useAsyncTask()
  const createTask = useAsyncTask()

  const load = async (): Promise<ActionResult> => {
    return loadTask.run(async () => {
      try {
        const res = await playlistApi.getMyPlaylists()
        playlists.value = res.playlists
        return { success: true }
      } catch (err) {
        return toActionFailure(err, '歌单列表加载失败')
      }
    })
  }

  /** 创建成功且列表刷新成功后，组件才能关闭弹窗。 */
  const create = async (payload: CreatePlaylistPayload): Promise<ActionResult> => {
    let created = false
    try {
      await createTask.run(async () => {
        const formData = new FormData()
        formData.append('name', payload.name)
        formData.append('description', payload.description)
        formData.append('creator_id', String(payload.creatorId ?? 0))
        if (payload.coverFile) formData.append('cover_image', payload.coverFile)
        await playlistApi.createPlaylist(formData)
        created = true
        requireSuccess(await load(), '歌单已创建，但列表刷新失败，请先刷新列表')
      })
      return { success: true }
    } catch (err) {
      const failure = toActionFailure(err, '创建歌单失败')
      return created ? { ...failure, completed: true } : failure
    }
  }

  return { playlists, loading: loadTask.loading, creating: createTask.loading, load, create }
}
