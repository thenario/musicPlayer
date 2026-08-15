import { ref } from 'vue'
import type { UploadedSong } from '@/api/song-api'
import { songApi } from '@/api/song-api'
import { usePagination, useAsyncTask } from '@/common'
import { UPLOAD_PAGE_SIZE } from '../const'

/** My uploads list: pagination, loading state, and stale-response protection. */
export function useMyUploads(defaultPageSize = UPLOAD_PAGE_SIZE) {
  const songs = ref<UploadedSong[]>([])
  const pagination = usePagination(defaultPageSize)
  const task = useAsyncTask()
  let latestRequest = 0

  const load = async () => {
    const requestId = ++latestRequest
    const page = pagination.state.current
    const pageSize = pagination.state.pageSize

    await task.run(async () => {
      try {
        const res = await songApi.getUserUploadSongs(page, pageSize)
        if (requestId !== latestRequest) return
        songs.value = res.songs
        pagination.setTotal(res.total)
      } catch (err) {
        if (requestId === latestRequest) console.error(err)
      }
    })
  }

  const changePage = (page: number, pageSize: number) => {
    pagination.change(page, pageSize)
    load()
  }

  return { songs, loading: task.loading, pagination, load, changePage }
}