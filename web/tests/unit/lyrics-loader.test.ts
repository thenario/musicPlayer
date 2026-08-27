import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick, ref } from 'vue'

const getLyrics = vi.hoisted(() => vi.fn())
vi.mock('@/api/song-api', () => ({ songApi: { getLyrics } }))

import { createLyricsLoader } from '@/composables/player/use-lyrics-loader'

beforeEach(() => vi.resetAllMocks())

describe('createLyricsLoader', () => {
  it('loads and parses lyrics when the current song changes', async () => {
    const songId = ref<number | undefined>()
    getLyrics.mockResolvedValue({ success: true, lyrics: '[00:01.00]Hello', t_lyrics: '[00:01.00]你好' })
    const loader = createLyricsLoader(() => songId.value)

    songId.value = 1
    await nextTick()
    await vi.waitFor(() => expect(loader.lyrics.value).toEqual([
      { time: 1, content: 'Hello', translation: '你好' },
    ]))
    expect(loader.isLoadingLyrics.value).toBe(false)
  })

  it('clears lyrics when there is no current song and reports request errors', async () => {
    const songId = ref<number | undefined>(1)
    const loader = createLyricsLoader(() => songId.value)
    getLyrics.mockRejectedValue(new Error('offline'))
    songId.value = 2
    await nextTick()
    await vi.waitFor(() => expect(loader.lyrics.value).toEqual([{ time: 0, content: '歌词加载失败' }]))

    songId.value = undefined
    await nextTick()
    expect(loader.lyrics.value).toEqual([])
    expect(loader.isLoadingLyrics.value).toBe(false)
  })
})
