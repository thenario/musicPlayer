import { ref, watch } from 'vue'
import type { LyricLine } from '@/types'
import { songApi } from '@/api/song-api'
import { parseLyrics } from '@/utils/lrc-parser'

/** Loads lyrics for the current song without allowing an old request to overwrite a newer song. */
export function createLyricsLoader(getCurrentSongId: () => number | string | undefined) {
  const lyrics = ref<LyricLine[]>()
  const isLoadingLyrics = ref(false)
  let latestRequest = 0

  watch(getCurrentSongId, async (id) => {
    const requestId = ++latestRequest
    if (!id) {
      lyrics.value = []
      isLoadingLyrics.value = false
      return
    }

    try {
      isLoadingLyrics.value = true
      const res = await songApi.getLyrics(id)
      if (requestId !== latestRequest) return
      lyrics.value = res.success
        ? parseLyrics(res.lyrics || '', res.t_lyrics || '')
        : [{ time: 0, content: '未找到歌词' }]
    } catch (error) {
      if (requestId === latestRequest) {
        console.error('获取歌词发生硬错误:', error)
        lyrics.value = [{ time: 0, content: '歌词加载失败' }]
      }
    } finally {
      if (requestId === latestRequest) isLoadingLyrics.value = false
    }
  })

  return { lyrics, isLoadingLyrics }
}