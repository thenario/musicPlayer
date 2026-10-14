import { expect, it, vi } from 'vitest'
import { songApi } from '@/api/song-api'
import { useSongList } from '@/views/song/song-list/composables/use-song-list'

vi.mock('@/api/song-api', () => ({
  songApi: { getSongs: vi.fn() },
}))

it('changing page size resets the page and retains the size when paging or searching', async () => {
  vi.mocked(songApi.getSongs).mockResolvedValue({
    success: true,
    message: 'ok',
    songs: [],
    pagination: { total_items: 80, total_pages: 2, current_page: 1, page_limit: 50 },
  })
  const list = useSongList()
  list.searchKeyword.value = '周杰伦'
  list.pagination.change(3)

  await list.changePage(3, 50)
  expect(songApi.getSongs).toHaveBeenLastCalledWith(1, '周杰伦', 50)

  await list.changePage(2, 50)
  expect(songApi.getSongs).toHaveBeenLastCalledWith(2, '周杰伦', 50)

  await list.search()
  expect(songApi.getSongs).toHaveBeenLastCalledWith(1, '周杰伦', 50)
})
