import { ref } from 'vue'
import type { ISong } from '@/types'

export type EditableUploadSong = Pick<ISong, 'song_id' | 'song_title' | 'song_cover_url'>

/** Keeps upload-editing state independent from the Pinia facade. */
export function createSongEditingState() {
  const currentEditingSong = ref<EditableUploadSong | null>(null)

  const setEditingSong = (song: EditableUploadSong) => {
    currentEditingSong.value = song
  }

  return { currentEditingSong, setEditingSong }
}
