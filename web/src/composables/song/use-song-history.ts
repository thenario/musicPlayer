import { songApi } from '@/api/song-api'
import { ref } from 'vue'
import type { ActionResult, ISong, PlayhistorySong } from '@/types'
import { usePlayerStore } from '@/stores/player'
import { requireSuccess, toActionFailure } from '@/utils/action-result'

export function createSongHistory() {
  const playHistory = ref<PlayhistorySong[]>([])

  const getPlayHistory = async (): Promise<ActionResult<{ history: PlayhistorySong[] }>> => {
    try {
      const res = await songApi.getPlayHistory()
      playHistory.value = [...(res.history ?? [])].sort(
        (a, b) => new Date(b.play_time).getTime() - new Date(a.play_time).getTime(),
      )
      return { success: true, history: playHistory.value }
    } catch (error) {
      return toActionFailure(error, '历史记录加载失败')
    }
  }

  const syncPlayHistory = async (song: ISong): Promise<ActionResult> => {
    try {
      await songApi.syncPlayHistory(song)
      requireSuccess(await getPlayHistory(), '历史记录已保存，但刷新失败')
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '历史记录同步失败')
    }
  }

  const playAllHistory = async (): Promise<ActionResult> => {
    try {
      const playerStore = usePlayerStore()
      await songApi.playAllHistory()
      requireSuccess(await playerStore.fetchCurrentQueue(), '历史播放队列加载失败')
      requireSuccess(await playerStore.fetchUserQueues(), '队列列表刷新失败')
      requireSuccess(await playerStore.playAtIndex(0), '历史歌曲播放失败')
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '播放历史失败')
    }
  }

  return { playHistory, syncPlayHistory, playAllHistory, getPlayHistory }
}
