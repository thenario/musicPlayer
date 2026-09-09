import { songApi } from '@/api/song-api'
import { ref } from 'vue'
import type { ISong, PlayhistorySong } from '@/types'
import { usePlayerStore } from '@/stores/player'
export function createSongHistory() {
  const playHistory = ref<PlayhistorySong[]>([])

  const getPlayHistory = async () => {
    try {
      const res = await songApi.getPlayHistory()
      if (!res.success) {
        return { success: false, history: playHistory.value }
      }

      playHistory.value = [...(res.history ?? [])].sort(
        (a, b) => new Date(b.play_time).getTime() - new Date(a.play_time).getTime(),
      )
      return { success: true, history: playHistory.value }
    } catch (e: unknown) {
      console.log(e)
      return { success: false, history: playHistory.value }
    }
  }

  const syncPlayHistory = async (song: ISong) => {
    try {
      await songApi.syncPlayHistory(song)
      await getPlayHistory()
      return true
    } catch (e: unknown) {
      console.log(e)
      return false
    }
  }
  const playAllHistory = async () => {
    try {
      const playerStore = usePlayerStore()
      const historyRes = await songApi.playAllHistory()
      if (!historyRes.success) {
        return { success: false }
      }
      const currentQueueRes = await playerStore.fetchCurrentQueue()
      const queuesRes = await playerStore.fetchUserQueues()
      if (!currentQueueRes.success || !queuesRes.success) {
        return { success: false }
      }
      await playerStore.playAtIndex(0)
      return { success: true }
    } catch (e: unknown) {
      console.log(e)
      return { success: false }
    }
  }

  return { playHistory, syncPlayHistory, playAllHistory, getPlayHistory }
}
