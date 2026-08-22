import type { Ref } from 'vue'
import type { ISong } from '@/types'
import { getImageUrl } from '@/utils/format'

export interface MediaSessionContext {
  currentSong: Ref<ISong | null>
  onPlay: () => void
  onPause: () => void
  onPrevious: () => void
  onNext: () => void
}

/** Keeps browser and headset media controls in sync with the current song. */
export function createMediaSession(ctx: MediaSessionContext) {
  const update = () => {
    if (!('mediaSession' in navigator) || !ctx.currentSong.value) return

    const song = ctx.currentSong.value
    const coverUrl = getImageUrl(song.song_cover_url)

    navigator.mediaSession.metadata = new MediaMetadata({
      title: song.song_title,
      artist: song.artist,
      album: song.album,
      artwork: coverUrl ? [{ src: coverUrl, sizes: '512x512', type: 'image/jpeg' }] : [],
    })

    navigator.mediaSession.setActionHandler('play', ctx.onPlay)
    navigator.mediaSession.setActionHandler('pause', ctx.onPause)
    navigator.mediaSession.setActionHandler('previoustrack', ctx.onPrevious)
    navigator.mediaSession.setActionHandler('nexttrack', ctx.onNext)
  }

  return { update }
}
