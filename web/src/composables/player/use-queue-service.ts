import type { Ref } from 'vue'
import type { IQueue, IQueueItem, IQueueState, ISong } from '@/types'
import { queueApi } from '@/api/queue-api'
import { sameId } from '@/utils/format'

interface QueueServiceContext {
  currentQueue: Ref<IQueueItem[]>
  currentQueueId: Ref<number | string | null>
  userQueues: Ref<IQueue[]>
  currentIndex: Ref<number>
  currentSong: Ref<ISong | null>
  playMode: Ref<string>
  playAtIndex: (index: number) => Promise<{ success: boolean }>
  pauseSong: () => void
  prepareAudioSource: (songId: number | string, savedProgress: number) => void
}

const errorMessage = (error: unknown) => error instanceof Error ? error.message : '请求失败'

/** Owns queue API calls and the state they update; playback remains in the store. */
export function createQueueService(ctx: QueueServiceContext) {
  const updateQueueData = (queue: IQueue) => {
    if (queue.queue_items) {
      queue.queue_items.sort((a, b) => a.queue_item_position - b.queue_item_position)
      ctx.currentQueue.value = [...queue.queue_items]
    }
    ctx.currentQueueId.value = queue.queue_id
  }

  const syncPlaybackState = (state: IQueueState) => {
    ctx.playMode.value = state.playmode || 'sequential'
    if (!state.current_song_id) return

    const index = ctx.currentQueue.value.findIndex((item) => sameId(item.song.song_id, state.current_song_id))
    if (index === -1) return

    ctx.currentIndex.value = index
    ctx.currentSong.value = ctx.currentQueue.value[index]!.song
    ctx.prepareAudioSource(ctx.currentSong.value.song_id, state.current_progress || 0)
  }

  const fetchCurrentQueue = async () => {
    try {
      const { queue, queue_state } = await queueApi.getCurrentQueue()
      updateQueueData(queue)
      if (queue_state) syncPlaybackState(queue_state)
      return { success: true }
    } catch (error: unknown) {
      console.error(errorMessage(error))
      return { success: false, message: errorMessage(error) }
    }
  }

  const fetchUserQueues = async () => {
    try {
      const { queues } = await queueApi.getMyQueues()
      const sortedQueues = [...(queues ?? [])].sort(
        (a, b) => new Date(b.updated_date).getTime() - new Date(a.updated_date).getTime(),
      )

      sortedQueues.forEach((queue) => {
        if (queue.queue_items && Array.isArray(queue.queue_items)) {
          queue.queue_items.sort((a, b) => a.queue_item_position - b.queue_item_position)
        }
      })
      ctx.userQueues.value = sortedQueues
      return { success: true }
    } catch (error: unknown) {
      console.error(errorMessage(error))
      return { success: false, message: errorMessage(error) }
    }
  }

  const fetchQueueDetails = async (queueId: number | string) => {
    try {
      const { queue } = await queueApi.getQueueById(queueId)
      return { success: true, queue }
    } catch (error: unknown) {
      console.error(errorMessage(error))
      return { success: false, message: errorMessage(error) }
    }
  }

  const switchQueue = async (queueId: number | string) => {
    try {
      await queueApi.alterQueueToCurrent(queueId)
      ctx.currentQueueId.value = queueId
      await fetchCurrentQueue()
      await fetchUserQueues()
      ctx.pauseSong()
    } catch (error: unknown) {
      console.error(errorMessage(error))
      throw error
    }
  }

  const playSongInQueue = async (queueId: number | string, index: number) => {
    try {
      if (!sameId(ctx.currentQueueId.value, queueId)) await switchQueue(queueId)
      ctx.playAtIndex(index)
      return { success: true }
    } catch (error: unknown) {
      console.error('切换播放失败', error)
      return { success: false }
    }
  }

  const deleteQueue = async (queueId: number | string) => {
    try {
      const { data } = await queueApi.deleteQueue(queueId)
      const { new_queue_id, was_active } = data
      if (was_active) {
        ctx.currentSong.value = null
        ctx.currentQueue.value = []
        ctx.pauseSong()
        if (new_queue_id) await switchQueue(new_queue_id)
        else ctx.currentQueueId.value = null
      }
      const result = await fetchUserQueues()
      return result.success ? { success: true } : { success: false }
    } catch (error: unknown) {
      console.error(errorMessage(error))
      return { success: false }
    }
  }

  const clearQueue = async (queueId: number | string) => {
    try {
      await queueApi.clearQueue(queueId)
      if (sameId(ctx.currentQueueId.value, queueId)) {
        ctx.currentQueue.value = []
        ctx.currentSong.value = null
        ctx.currentIndex.value = -1
        ctx.pauseSong()
        const result = await fetchUserQueues()
        return result.success ? { success: true } : { success: false }
      }
    } catch (error: unknown) {
      console.error(errorMessage(error))
      return { success: false }
    }
  }

  const playPlaylist = async (playlistId: number | string, startSongId: number | string | null = null) => {
    try {
      await queueApi.createQueueFromPlaylist(playlistId)
      await fetchCurrentQueue()
      await fetchUserQueues()
      const startIndex = startSongId
        ? ctx.currentQueue.value.findIndex((item) => sameId(item.song.song_id, startSongId))
        : 0

      if (ctx.currentQueue.value.length > 0) ctx.playAtIndex(startIndex === -1 ? 0 : startIndex)
      return { success: true }
    } catch (error: unknown) {
      console.error(errorMessage(error))
      return { success: false }
    }
  }

  return {
    fetchCurrentQueue,
    fetchUserQueues,
    fetchQueueDetails,
    switchQueue,
    deleteQueue,
    clearQueue,
    playPlaylist,
    playSongInQueue,
  }
}
