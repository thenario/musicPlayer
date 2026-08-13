import { computed } from 'vue'
import { usePlayerStore } from '@/stores/player'
import { PLAY_MODES, PLAY_MODE_TITLES } from '../const'

/** 播放模式：标题映射 + 循环切换。 */
export function usePlayMode() {
  const playerStore = usePlayerStore()

  const playModeTitle = computed(() => PLAY_MODE_TITLES[playerStore.playMode] || '未知')

  const togglePlayMode = async () => {
    const current = playerStore.playMode === 'sequential' ? 'repeat_all' : playerStore.playMode
    const currentIdx = PLAY_MODES.indexOf(current as (typeof PLAY_MODES)[number])
    const next = PLAY_MODES[(currentIdx + 1) % PLAY_MODES.length] ?? 'repeat_all'
    const res = await playerStore.setPlayMode(next)
    if (!res.success) console.error('切换失败')
  }

  return { playModeTitle, togglePlayMode }
}
