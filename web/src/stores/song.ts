import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ISong } from '@/types'

export type EditableUploadSong = Pick<ISong, 'song_id' | 'song_title' | 'song_cover_url'>

export const useSongStore = defineStore('song', () => {
  const currentEditingSong = ref<EditableUploadSong | null>(null)

  const setEditingSong = (song: EditableUploadSong) => {
    currentEditingSong.value = song
  }

  return { currentEditingSong, setEditingSong }
})