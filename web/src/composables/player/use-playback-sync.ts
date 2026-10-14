import type { Ref } from 'vue'
import type { ActionResult, ISong } from '@/types'
import { toActionFailure } from '@/utils/action-result'
import { queueApi } from '@/api/queue-api'

export interface PlaybackSyncContext {
  currentSong: Ref<ISong | null>
  currentQueueId: Ref<number | string | null>
  currentIndex: Ref<number>
  currentTime: Ref<number>
  isPlaying: Ref<boolean>
  playMode: Ref<string>
}

/** Persists the playback position and mode without owning player state. */
export function createPlaybackSync(ctx: PlaybackSyncContext) {
  const sync = async (): Promise<ActionResult> => {
    if (!ctx.currentSong.value || !ctx.currentQueueId.value) return { success: true }

    try {
      await queueApi.updateCurrentQueueState({
        current_song_id: ctx.currentSong.value.song_id,
        current_position: ctx.currentIndex.value,
        current_progress: ctx.currentTime.value,
        is_playing: ctx.isPlaying.value,
        current_queue_id: ctx.currentQueueId.value,
        playmode: ctx.playMode.value,
        updated_date: new Date(),
      })
      return { success: true }
    } catch (error: unknown) {
      return toActionFailure(error, '播放状态同步失败')
    }
  }

  return { sync }
}
