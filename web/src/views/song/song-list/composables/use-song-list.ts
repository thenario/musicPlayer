import { ref } from 'vue'
import type { ISong } from '@/types'
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

  const load = async () => {
    const requestId = ++latestRequest
    const page = pagination.state.current
    const keyword = searchKeyword.value

    await task.run(async () => {
      try {
        const res = await songApi.getSongs(page, keyword)
        if (requestId !== latestRequest) return
        songs.value = res.songs || []
        pagination.setTotal(res.pagination?.total_items || songs.value.length)
      } catch (err) {
        if (requestId === latestRequest) console.error(err)
      }
    })
  }

  const changePage = (page: number, pageSize: number) => {
    pagination.change(page, pageSize)
    load()
  }

  /** Search/reset returns to the first page. */
  const search = () => {
    pagination.change(1)
    load()
  }

  return { songs, searchKeyword, loading: task.loading, pagination, load, changePage, search }
}