import { ref } from 'vue'
import type { UploadedSong } from '@/api/song-api'
import { songApi } from '@/api/song-api'
import { usePagination, useAsyncTask } from '@/common'
import { UPLOAD_PAGE_SIZE } from '../const'

/** 我的上传列表：分页 + 加载状态。 */
export function useMyUploads(defaultPageSize = UPLOAD_PAGE_SIZE) {
  const songs = ref<UploadedSong[]>([])
  const pagination = usePagination(defaultPageSize)
  const task = useAsyncTask()

  const load = async () => {
    await task.run(async () => {
      try {
        const res = await songApi.getUserUploadSongs(pagination.state.current, pagination.state.pageSize)
        songs.value = res.songs
        pagination.setTotal(res.total)
      } catch (err) {
        console.error(err)
      }
    })
  }

  const changePage = (page: number, pageSize: number) => {
    pagination.change(page, pageSize)
    load()
  }

  return { songs, loading: task.loading, pagination, load, changePage }
}
