import { describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { createPlaybackSync } from '@/composables/player/use-playback-sync'

const updateCurrentQueueState = vi.hoisted(() => vi.fn())
vi.mock('@/api/queue-api', () => ({ queueApi: { updateCurrentQueueState } }))

describe('createPlaybackSync', () => {
  it('does nothing when there is no current song or queue', async () => {
    const sync = createPlaybackSync({
      currentSong: ref(null),
      currentQueueId: ref(null),
      currentIndex: ref(-1),
      currentTime: ref(0),
      isPlaying: ref(false),
      playMode: ref('sequential'),
    })

    await expect(sync.sync()).resolves.toEqual({ success: true })
    expect(updateCurrentQueueState).not.toHaveBeenCalled()
  })

  it('sends the current playback state and reports API errors', async () => {
    const song = { song_id: 9 } as never
    const ctx = {
      currentSong: ref(song),
      currentQueueId: ref(2),
      currentIndex: ref(3),
      currentTime: ref(18),
      isPlaying: ref(true),
      playMode: ref('shuffle'),
    }
    updateCurrentQueueState.mockResolvedValueOnce({ success: true })
    const sync = createPlaybackSync(ctx)

    await expect(sync.sync()).resolves.toEqual({ success: true })
    expect(updateCurrentQueueState).toHaveBeenCalledWith(expect.objectContaining({
      current_song_id: 9,
      current_queue_id: 2,
      current_position: 3,
      current_progress: 18,
      is_playing: true,
      playmode: 'shuffle',
      updated_date: expect.any(Date),
    }))

    updateCurrentQueueState.mockRejectedValueOnce(new Error('offline'))
    await expect(sync.sync()).resolves.toEqual({ success: false })
  })
})
