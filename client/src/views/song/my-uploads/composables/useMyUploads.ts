import { ref } from 'vue'
import type { ISong } from '@/types'
import { songApi } from '@/api/songApi'
import { usePagination, useAsyncTask } from '@/common'
import { UPLOAD_PAGE_SIZE } from '../const'

/** 我的上传列表：分页 + 加载状态。 */
export function useMyUploads(defaultPageSize = UPLOAD_PAGE_SIZE) {
  const songs = ref<ISong[]>([])
  const pagination = usePagination(defaultPageSize)
  const task = useAsyncTask()

  const load = async () => {
    await task.run(async () => {
      const res = await songApi.getUserUploadSongs(pagination.state.current, pagination.state.pageSize)
      songs.value = res.songs
      pagination.setTotal(res.total)
    })
  }

  const changePage = (page: number, pageSize: number) => {
    pagination.change(page, pageSize)
    load()
  }

  return { songs, loading: task.loading, pagination, load, changePage }
}
