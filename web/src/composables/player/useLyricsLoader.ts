import { ref, watch } from 'vue'
import type { LyricLine } from '@/types'
import { songApi } from '@/api/songApi'
import { parseLyrics } from '@/utils/lrcParser'

/**
 * 歌词加载：监听当前歌曲 id 变化，拉取并解析歌词。
 * 失败时提供占位歌词（错误提示由拦截器静默处理，见 songApi.getLyrics 的 silent）。
 */
export function createLyricsLoader(getCurrentSongId: () => number | string | undefined) {
  const lyrics = ref<LyricLine[]>()
  const isLoadingLyrics = ref(false)

  watch(getCurrentSongId, async (id) => {
    if (!id) {
      lyrics.value = []
      return
    }
    try {
      isLoadingLyrics.value = true
      const res = await songApi.getLyrics(id)

      if (res.success) {
        lyrics.value = parseLyrics(res.lyrics || '', res.t_lyrics || '')
      } else {
        lyrics.value = [{ time: 0, content: '未找到歌词' }]
      }
    } catch (error) {
      console.error('获取歌词发生硬错误:', error)
      lyrics.value = [{ time: 0, content: '歌词加载失败' }]
    } finally {
      isLoadingLyrics.value = false
    }
  })

  return { lyrics, isLoadingLyrics }
}
