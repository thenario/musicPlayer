import { defineStore } from 'pinia'
import { createSongEditingState } from '@/composables/song/use-song-editing'

export type { EditableUploadSong } from '@/composables/song/use-song-editing'

export const useSongStore = defineStore('song', () => {
  return createSongEditingState()
})
