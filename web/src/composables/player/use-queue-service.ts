import type { Ref } from 'vue'
import type { ActionResult, IQueue, IQueueItem, IQueueState, ISong } from '@/types'
import { queueApi } from '@/api/queue-api'
import { sameId } from '@/utils/format'
import { requireSuccess, toActionFailure } from '@/utils/action-result'

interface QueueServiceContext {
  currentQueue: Ref<IQueueItem[]>
  currentQueueId: Ref<number | string | null>
  userQueues: Ref<IQueue[]>
  currentIndex: Ref<number>
  currentSong: Ref<ISong | null>
  playMode: Ref<string>
  playAtIndex: (index: number) => Promise<ActionResult>
  pauseSong: () => void
  prepareAudioSource: (songId: number | string, savedProgress: number) => void
}

export function createQueueService(ctx: QueueServiceContext) {
  const updateQueueData = (queue: IQueue) => {
    ctx.currentQueue.value = [...(queue.queue_items ?? [])]
      .sort((a, b) => a.queue_item_position - b.queue_item_position)
    ctx.currentQueueId.value = queue.queue_id
    ctx.currentIndex.value = -1
    ctx.currentSong.value = null
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

  const fetchCurrentQueue = async (): Promise<ActionResult> => {
    try {
      const { queue, queue_state } = await queueApi.getCurrentQueue()
      updateQueueData(queue)
      if (queue_state) syncPlaybackState(queue_state)
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '当前队列加载失败')
    }
  }

  const fetchUserQueues = async (): Promise<ActionResult> => {
    try {
      const { queues } = await queueApi.getMyQueues()
      const sortedQueues = [...(queues ?? [])].sort(
        (a, b) => new Date(b.updated_date).getTime() - new Date(a.updated_date).getTime(),
      )
      sortedQueues.forEach((queue) => {
        if (Array.isArray(queue.queue_items)) {
          queue.queue_items.sort((a, b) => a.queue_item_position - b.queue_item_position)
        }
      })
      ctx.userQueues.value = sortedQueues
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '队列列表加载失败')
    }
  }

  const fetchQueueDetails = async (queueId: number | string): Promise<ActionResult<{ queue: IQueue }>> => {
    try {
      const { queue } = await queueApi.getQueueById(queueId)
      return { success: true, queue }
    } catch (error) {
      return toActionFailure(error, '队列详情加载失败')
    }
  }

  const switchQueueOrThrow = async (queueId: number | string) => {
    await queueApi.alterQueueToCurrent(queueId)
    ctx.pauseSong()
    // 后端已切换队列，先清掉旧播放上下文，失败时不能继续播放旧队列。
    ctx.currentQueueId.value = queueId
    ctx.currentQueue.value = []
    ctx.currentSong.value = null
    ctx.currentIndex.value = -1
    requireSuccess(await fetchCurrentQueue(), '切换后的队列加载失败')
    requireSuccess(await fetchUserQueues(), '队列列表刷新失败')
  }

  const switchQueue = async (queueId: number | string): Promise<ActionResult> => {
    try {
      await switchQueueOrThrow(queueId)
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '切换队列失败')
    }
  }

  const playSongInQueue = async (queueId: number | string, index: number): Promise<ActionResult> => {
    try {
      if (!sameId(ctx.currentQueueId.value, queueId)) await switchQueueOrThrow(queueId)
      requireSuccess(await ctx.playAtIndex(index), '播放失败')
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '切换播放失败')
    }
  }

  const deleteQueue = async (queueId: number | string): Promise<ActionResult> => {
    try {
      const { data } = await queueApi.deleteQueue(queueId)
      if (data.was_active) {
        ctx.pauseSong()
        ctx.currentSong.value = null
        ctx.currentQueue.value = []
        ctx.currentIndex.value = -1
        ctx.currentQueueId.value = null
        if (data.new_queue_id) await switchQueueOrThrow(data.new_queue_id)
      }
      requireSuccess(await fetchUserQueues(), '队列已删除，但列表刷新失败')
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '删除队列失败')
    }
  }

  const clearQueue = async (queueId: number | string): Promise<ActionResult> => {
    try {
      await queueApi.clearQueue(queueId)
      if (sameId(ctx.currentQueueId.value, queueId)) {
        ctx.pauseSong()
        ctx.currentQueue.value = []
        ctx.currentSong.value = null
        ctx.currentIndex.value = -1
      }
      requireSuccess(await fetchUserQueues(), '队列已清空，但列表刷新失败')
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '清空队列失败')
    }
  }

  const playPlaylist = async (playlistId: number | string, startSongId: number | string | null = null): Promise<ActionResult> => {
    try {
      await queueApi.createQueueFromPlaylist(playlistId)
      ctx.pauseSong()
      ctx.currentQueue.value = []
      ctx.currentQueueId.value = null
      ctx.currentSong.value = null
      ctx.currentIndex.value = -1
      requireSuccess(await fetchCurrentQueue(), '新队列加载失败')
      requireSuccess(await fetchUserQueues(), '队列列表刷新失败')
      const startIndex = startSongId
        ? ctx.currentQueue.value.findIndex((item) => sameId(item.song.song_id, startSongId)) : 0
      requireSuccess(await ctx.playAtIndex(startIndex === -1 ? 0 : startIndex), '播放失败')
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '播放歌单失败')
    }
  }

  return { fetchCurrentQueue, fetchUserQueues, fetchQueueDetails, switchQueue,
    deleteQueue, clearQueue, playPlaylist, playSongInQueue }
}
