import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSongStore = defineStore('song', () => {
  const currentEditingSong = ref<any>(null)

  const setEditingSong = (song: any) => {
    currentEditingSong.value = song
  }

  return { currentEditingSong, setEditingSong }
})
