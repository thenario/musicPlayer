import type { Ref } from 'vue'
import type { IQueue, IQueueItem, ISong } from '@/types'
import { queueApi } from '@/api/queue-api'
import { sameId } from '@/utils/format'

interface QueueItemAddition {
  queue_id: number | string
  queue_item_id: number | string
  queue_item_position: number
}

export interface QueueMutationsContext {
  currentQueue: Ref<IQueueItem[]>
  currentQueueId: Ref<number | string | null>
  userQueues: Ref<IQueue[]>
  currentIndex: Ref<number>
  currentSong: Ref<ISong | null>
  /** 从队列移除当前播放歌曲后，切换到下一首 / 停止 */
  playAtIndex: (index: number) => Promise<unknown>
  stopPlayback: () => void
}

/**
 * 队列变更：乐观增删（tempId + 回滚）、排序、乱序、删除计数。
 * 通过 context 读写共享队列状态；删除当前歌曲时回调 playAtIndex/stopPlayback。
 */
export function createQueueMutations(ctx: QueueMutationsContext) {
  /** 队列项位置使用 1 基连续序号；currentIndex 仍保持 0 基数组下标。 */
  const normalizeQueuePositions = () => {
    ctx.currentQueue.value.forEach((item, index) => {
      item.queue_item_position = index + 1
    })
  }

  const reorderQueueOrder = async () => {
    if (!ctx.currentQueueId.value) return
    else {
      try {
        const songIds = ctx.currentQueue.value.map((item) => item.song.song_id)
        await queueApi.reorderQueue(songIds, ctx.currentQueueId.value)
        return { success: true }
      } catch (err: unknown) {
        console.error(err instanceof Error ? err.message : err)
        return { success: false }
      }
    }
  }

  const updateQueueOrder = async (newQueue: IQueueItem[]) => {
    ctx.currentQueue.value = newQueue
    normalizeQueuePositions()

    if (ctx.currentSong.value) {
      const tempCurrentSong = ctx.currentSong.value
      const newIndex = ctx.currentQueue.value.findIndex(
        (item) => sameId(item.song.song_id, tempCurrentSong.song_id),
      )
      if (newIndex !== -1) {
        ctx.currentIndex.value = newIndex
      }
    }
    const res = await reorderQueueOrder()
    return res?.success ? { success: true } : { success: false }
  }

  const handleDuplicateSong = (songId: number | string) => {
    const idx = ctx.currentQueue.value.findIndex((item) => sameId(item.song.song_id, songId))
    if (idx !== -1) {
      ctx.currentQueue.value.splice(idx, 1)
      if (idx < ctx.currentIndex.value) ctx.currentIndex.value--
      normalizeQueuePositions()
    }
    return idx
  }

  const finalizeQueueItem = (tempId: string, data: QueueItemAddition, isNewAddition: boolean) => {
    const item = ctx.currentQueue.value.find((i) => i.queue_item_id === tempId)
    if (item && data) {
      Object.assign(item, {
        queue_item_id: data.queue_item_id,
        queue_item_position: data.queue_item_position,
        queue_id: data.queue_id,
      })
      ctx.currentQueueId.value = data.queue_id
    }
    normalizeQueuePositions()

    if (ctx.currentQueueId.value && isNewAddition) {
      const target = ctx.userQueues.value.find((q) => sameId(q.queue_id, ctx.currentQueueId.value))
      if (target) target.song_count++
    }
  }

  const syncRemoveToServer = async (id: number | string) => {
    if (!ctx.currentQueueId.value) {
      return { success: false }
    }
    try {
      await queueApi.removeSongFromQueue(ctx.currentQueueId.value, id)
      return { success: true }
    } catch (err: unknown) {
      console.error(err instanceof Error ? err.message : err)
      return { success: false }
    }
  }

  const handleStateAfterRemoval = (idx: number) => {
    const isDeletingCurrent = idx === ctx.currentIndex.value
    ctx.currentQueue.value.splice(idx, 1)
    normalizeQueuePositions()
    if (isDeletingCurrent) {
      if (ctx.currentQueue.value.length > 0) {
        const nextIdx = Math.min(idx, ctx.currentQueue.value.length - 1)
        ctx.playAtIndex(nextIdx)
      } else {
        ctx.stopPlayback()
      }
    } else if (idx < ctx.currentIndex.value) {
      ctx.currentIndex.value--
    }
  }

  /** mode=true 为立即播放；两种模式都会插入到当前歌曲之后。 */
  const addToQueue = async (song: ISong, playImmediately = false) => {
    if (!song?.song_id) return { targetIndex: -1, success: false }

    const previousQueue = [...ctx.currentQueue.value]
    const previousIndex = ctx.currentIndex.value
    const oldIdx = handleDuplicateSong(song.song_id)
    const targetIndex = Math.max(0, ctx.currentIndex.value + 1)

    const tempId = `temp-${Date.now()}`
    const newItem = {
      queue_item_id: tempId,
      queue_item_position: targetIndex + 1,
      queue_id: ctx.currentQueueId.value ?? -1,
      song,
      added_date: new Date(),
    }

    ctx.currentQueue.value.splice(targetIndex, 0, newItem)
    normalizeQueuePositions()
    if (playImmediately) ctx.currentIndex.value = targetIndex

    try {
      const res = await queueApi.addSongToQueue(song.song_id, ctx.currentQueueId.value || 0, playImmediately)
      finalizeQueueItem(tempId, res.data, oldIdx === -1)
      return { targetIndex, success: true }
    } catch (err: unknown) {
      console.error(err instanceof Error ? err.message : err)
      ctx.currentQueue.value = previousQueue
      ctx.currentIndex.value = previousIndex
      normalizeQueuePositions()
      return { targetIndex: -1, success: false }
    }
  }

  const removeQueueItem = async (itemId: number | string) => {
    if (!itemId) return { success: false }

    try {
      const isTempId = typeof itemId === 'string' && itemId.startsWith('temp-')
      if (!isTempId) {
        const res = await syncRemoveToServer(itemId)
        if (!res.success) return { success: false }
      }

      const targetIdx = ctx.currentQueue.value.findIndex((item) => sameId(item.queue_item_id, itemId))
      if (targetIdx !== -1) {
        handleStateAfterRemoval(targetIdx)
      }
      updateQueueCount(-1)
      return { success: true }
    } catch (e) {
      console.error('移除歌曲失败', e)
      return { success: false }
    }
  }

  const updateQueueCount = (delta: number) => {
    if (!ctx.currentQueueId.value) return
    const target = ctx.userQueues.value.find((q) => sameId(q.queue_id, ctx.currentQueueId.value))
    if (target && target.song_count + delta >= 0) {
      target.song_count += delta
    }
  }

  const shuffleQueue = async () => {
    if (ctx.currentQueue.value.length <= 1) return

    const currentId = ctx.currentSong.value?.song_id
    let currentItem = null
    const others = []

    for (const item of ctx.currentQueue.value) {
      if (sameId(item.song.song_id, currentId) && !currentItem) {
        currentItem = item
      } else {
        others.push(item)
      }
    }

    for (let i = others.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[others[i]!, others[j]!] = [others[j]!, others[i]!]
    }

    if (currentItem) {
      const cleanOthers = others.filter((item) => !sameId(item.queue_item_id, currentItem.queue_item_id))
      ctx.currentQueue.value = [currentItem, ...cleanOthers]
      ctx.currentIndex.value = 0
    } else {
      ctx.currentQueue.value = others
      ctx.currentIndex.value = -1
    }
    normalizeQueuePositions()

    const res = await reorderQueueOrder()
    return res?.success ? { success: true } : { success: false }
  }

  return { reorderQueueOrder, updateQueueOrder, shuffleQueue, addToQueue, removeQueueItem }
}
