import { ref, watch, nextTick, onBeforeUpdate } from 'vue'
import { storeToRefs } from 'pinia'
import { usePlayerStore } from '@/stores/player'

/** 歌词滚动：根据播放时间高亮当前行并居中滚动。 */
export function useLyricsScroll() {
  const playerStore = usePlayerStore()
  const { lyrics, currentTime } = storeToRefs(playerStore)

  const lyricsContainer = ref<HTMLDivElement | null>(null)
  const lyricRefs = ref<(HTMLDivElement | null)[]>([])
  const currentLineIndex = ref(0)

  onBeforeUpdate(() => {
    lyricRefs.value = []
  })

  const scrollToActiveLine = (index: number) => {
    const activeEl = lyricRefs.value[index]
    const container = lyricsContainer.value
    if (activeEl && container) {
      const targetScrollTop = activeEl.offsetTop - container.clientHeight / 2 + activeEl.clientHeight / 2
      container.scrollTo({
        top: targetScrollTop,
        behavior: 'smooth',
      })
    }
  }

  // 切歌时重置滚动
  watch(() => playerStore.currentSong?.song_id, () => {
    currentLineIndex.value = 0
    lyricRefs.value = []
    nextTick(() => {
      if (lyricsContainer.value) lyricsContainer.value.scrollTop = 0
    })
  }, { immediate: true })

  // 按播放时间推进高亮行
  watch(currentTime, (newTime) => {
    if (!lyrics.value || lyrics.value.length === 0) return

    let index = lyrics.value.findIndex((line, i) => {
      const nextLine = lyrics.value![i + 1]
      return newTime >= line.time && (!nextLine || newTime < nextLine.time)
    })

    if (index === -1 && newTime < lyrics.value[0]!.time) {
      index = 0
    }

    if (index !== -1 && index !== currentLineIndex.value) {
      currentLineIndex.value = index
      scrollToActiveLine(index)
    }
  })

  return { lyrics, lyricsContainer, lyricRefs, currentLineIndex }
}
