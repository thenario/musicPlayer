import { ref } from 'vue'
import type { UploadedSong } from '@/api/song-api'
import type { ActionResult } from '@/types'
import { toActionFailure } from '@/utils/action-result'
import { songApi } from '@/api/song-api'
import { usePagination, useAsyncTask } from '@/common'
import { UPLOAD_PAGE_SIZE } from '../const'

/** My uploads list: pagination, loading state, and stale-response protection. */
export function useMyUploads(defaultPageSize = UPLOAD_PAGE_SIZE) {
  const songs = ref<UploadedSong[]>([])
  const pagination = usePagination(defaultPageSize)
  const task = useAsyncTask()
  let latestRequest = 0

  const load = async (): Promise<ActionResult> => {
    const requestId = ++latestRequest
    const page = pagination.state.current
    const pageSize = pagination.state.pageSize

    return task.run(async () => {
      try {
        const res = await songApi.getUserUploadSongs(page, pageSize)
        if (requestId !== latestRequest) return { success: false, message: '列表请求已被更新', canceled: true }
        songs.value = res.songs
        const total = Number(res.total)
        pagination.setTotal(Number.isFinite(total) ? total : 0)
        return { success: true }
      } catch (err) {
        if (requestId !== latestRequest) return { success: false, message: '列表请求已被更新', canceled: true }
        return toActionFailure(err, '上传列表加载失败')
      }
    })
  }

  const changePage = (page: number, pageSize: number) => {
    pagination.change(page, pageSize)
    return load()
  }

  return { songs, loading: task.loading, pagination, load, changePage }
}
