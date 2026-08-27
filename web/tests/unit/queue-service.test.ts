import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import type { IQueue, IQueueItem, ISong } from '@/types'

const queueApi = vi.hoisted(() => ({
  getCurrentQueue: vi.fn(),
  getMyQueues: vi.fn(),
  getQueueById: vi.fn(),
  alterQueueToCurrent: vi.fn(),
  deleteQueue: vi.fn(),
  clearQueue: vi.fn(),
  createQueueFromPlaylist: vi.fn(),
}))
vi.mock('@/api/queue-api', () => ({ queueApi }))

import { createQueueService } from '@/composables/player/use-queue-service'

const item = (songId: number, position: number, id = songId) => ({
  queue_item_id: id,
  queue_item_position: position,
  queue_id: 1,
  song: { song_id: songId, song_title: `song-${songId}` },
}) as unknown as IQueueItem

const queue = (items = [item(1, 2), item(2, 1)]) => ({
  queue_id: 4,
  queue_name: 'queue',
  queue_items: items,
}) as unknown as IQueue

const createContext = () => ({
  currentQueue: ref<IQueueItem[]>([]),
  currentQueueId: ref<number | string | null>(null),
  userQueues: ref<IQueue[]>([]),
  currentIndex: ref(-1),
  currentSong: ref<ISong | null>(null),
  playMode: ref('sequential'),
  playAtIndex: vi.fn().mockResolvedValue({ success: true }),
  pauseSong: vi.fn(),
  prepareAudioSource: vi.fn(),
})

beforeEach(() => vi.resetAllMocks())

describe('createQueueService', () => {
  it('loads current queue, sorts items, and restores playback state', async () => {
    const ctx = createContext()
    queueApi.getCurrentQueue.mockResolvedValue({
      queue: queue(),
      queue_state: { current_song_id: 1, current_progress: 12, playmode: 'shuffle' },
    })
    const service = createQueueService(ctx)

    await expect(service.fetchCurrentQueue()).resolves.toEqual({ success: true })
    expect(ctx.currentQueue.value.map((entry) => entry.queue_item_position)).toEqual([1, 2])
    expect(ctx.currentQueueId.value).toBe(4)
    expect(ctx.currentIndex.value).toBe(1)
    expect(ctx.playMode.value).toBe('shuffle')
    expect(ctx.prepareAudioSource).toHaveBeenCalledWith(1, 12)
  })

  it('sorts user queues and reports API failures', async () => {
    const ctx = createContext()
    queueApi.getMyQueues.mockResolvedValue({ queues: [
      { queue_id: 1, updated_date: '2026-01-01', queue_items: [item(1, 2), item(2, 1)] },
      { queue_id: 2, updated_date: '2026-02-01', queue_items: [] },
    ] })
    const service = createQueueService(ctx)

    await expect(service.fetchUserQueues()).resolves.toEqual({ success: true })
    expect(ctx.userQueues.value.map((entry) => entry.queue_id)).toEqual([2, 1])
    expect(ctx.userQueues.value[1]!.queue_items.map((entry) => entry.queue_item_position)).toEqual([1, 2])

    queueApi.getQueueById.mockRejectedValue(new Error('offline'))
    await expect(service.fetchQueueDetails(2)).resolves.toEqual({ success: false, message: 'offline' })
  })

  it('clears the active queue and refreshes queue list', async () => {
    const ctx = createContext()
    ctx.currentQueueId.value = 2
    ctx.currentQueue.value = [item(1, 1)]
    ctx.currentSong.value = item(1, 1).song
    ctx.currentIndex.value = 0
    queueApi.clearQueue.mockResolvedValue({ success: true })
    queueApi.getMyQueues.mockResolvedValue({ queues: [] })
    const service = createQueueService(ctx)

    await expect(service.clearQueue(2)).resolves.toEqual({ success: true })
    expect(ctx.currentQueue.value).toEqual([])
    expect(ctx.currentSong.value).toBeNull()
    expect(ctx.currentIndex.value).toBe(-1)
    expect(ctx.pauseSong).toHaveBeenCalledOnce()
  })

  it('plays a playlist and starts at the requested song', async () => {
    const ctx = createContext()
    queueApi.createQueueFromPlaylist.mockResolvedValue({ success: true })
    queueApi.getCurrentQueue.mockResolvedValue({ queue: queue([item(10, 1), item(20, 2)]), queue_state: null })
    queueApi.getMyQueues.mockResolvedValue({ queues: [] })
    const service = createQueueService(ctx)

    await expect(service.playPlaylist(7, 20)).resolves.toEqual({ success: true })
    expect(ctx.playAtIndex).toHaveBeenCalledWith(1)
  })
})
