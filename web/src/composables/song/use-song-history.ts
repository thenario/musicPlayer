import { songApi } from '@/api/song-api'
import type { ISong } from '@/types'
import { usePlayerStore } from '@/stores/player'
const playerStore = usePlayerStore()
export function createSongHistory() {
  const syncPlayHistory = async (song: ISong) => {
    try {
      await songApi.syncPlayHistory(song)
      return true
    } catch (e: unknown) {
        console.log(e)
        return false
    }
  }
  const playAllHistory = async() => {
    // 逻辑是发送播放请求，后端更新播放队列和播放状态，前端重新拿queue等内容进行播放
    try{
        const historyRes = await songApi.playAllHistory()
        if(!historyRes.success){
            return false
        }
        const currentQueueRes = await playerStore.fetchCurrentQueue()
        const queuesRes = await playerStore.fetchUserQueues()
        if(!currentQueueRes || !queuesRes){
            return false
        }
        playerStore.playAtIndex(0)
        return true
    }
    catch(e : unknown){
        console.log(e)
        return false
    }
  }
}
