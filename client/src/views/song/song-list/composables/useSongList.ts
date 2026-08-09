import { ref } from 'vue'
import type { ISong } from '@/types'
import { songApi } from '@/api/songApi'
import { usePagination, useAsyncTask } from '@/common'
import { SONG_PAGE_SIZE } from '../const'

/** 歌曲库列表：搜索 + 分页 + 加载状态。 */
export function useSongList(defaultPageSize = SONG_PAGE_SIZE) {
  const songs = ref<ISong[]>([])
  const searchKeyword = ref('')
  const pagination = usePagination(defaultPageSize)
  const task = useAsyncTask()

  const load = async () => {
    await task.run(async () => {
      const res = await songApi.getSongs(pagination.state.current, searchKeyword.value)
      songs.value = res.songs || []
      pagination.setTotal(res.pagination?.total_items || songs.value.length)
    })
  }

  const changePage = (page: number, pageSize: number) => {
    pagination.change(page, pageSize)
    load()
  }

  /** 搜索/重置时回到第一页。 */
  const search = () => {
    pagination.change(1)
    load()
  }

  return { songs, searchKeyword, loading: task.loading, pagination, load, changePage, search }
}
