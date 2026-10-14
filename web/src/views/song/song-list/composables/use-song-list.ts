import { ref } from 'vue'
import type { ActionResult, ISong } from '@/types'
import { toActionFailure } from '@/utils/action-result'
import { songApi } from '@/api/song-api'
import { usePagination, useAsyncTask } from '@/common'
import { SONG_PAGE_SIZE } from '../const'

/** Song library list: search, pagination, and stale-response protection. */
export function useSongList(defaultPageSize = SONG_PAGE_SIZE) {
  const songs = ref<ISong[]>([])
  const searchKeyword = ref('')
  const pagination = usePagination(defaultPageSize)
  const task = useAsyncTask()
  let latestRequest = 0

  const load = async (): Promise<ActionResult> => {
    const requestId = ++latestRequest
    const page = pagination.state.current
    const pageSize = pagination.state.pageSize
    const keyword = searchKeyword.value

    return task.run(async () => {
      try {
        const res = await songApi.getSongs(page, keyword, pageSize)
        if (requestId !== latestRequest) return { success: false, message: '搜索已被更新', canceled: true }
        songs.value = res.songs || []
        pagination.setTotal(res.pagination?.total_items || songs.value.length)
        return { success: true }
      } catch (err) {
        if (requestId !== latestRequest) return { success: false, message: '搜索已被更新', canceled: true }
        return toActionFailure(err, '歌曲列表加载失败')
      }
    })
  }

  const changePage = (page: number, pageSize: number) => {
    pagination.change(page, pageSize)
    return load()
  }

  /** Search/reset returns to the first page. */
  const search = () => {
    pagination.change(1)
    return load()
  }

  return { songs, searchKeyword, loading: task.loading, pagination, load, changePage, search }
}
