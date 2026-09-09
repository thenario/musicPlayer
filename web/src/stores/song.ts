import { defineStore } from 'pinia'
import { createSongEditingState } from '@/composables/song/use-song-editing'
import { createSongHistory } from '@/composables/song/use-song-history'

export type { EditableUploadSong } from '@/composables/song/use-song-editing'

export const useSongStore = defineStore('song', () => {
  const editing = createSongEditingState()
  const history = createSongHistory()
  return{
    ...editing,
    ...history,
  }
})
