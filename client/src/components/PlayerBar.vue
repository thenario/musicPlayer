<template>
  <div v-if="playerStore.currentSong"
    class="w-full shrink-0 h-24 bg-gray-900/90 backdrop-blur-lg border-t border-white/5 px-6 flex items-center justify-between z-50 select-none">

    <div class="w-[30%] flex items-center gap-4 min-w-0">
      <div class="relative group cursor-pointer shrink-0" @click="playerStore.toggleSongDetail">
        <div
          class="w-16 h-16 rounded-lg shadow-2xl overflow-hidden transition-transform group-hover:scale-105 bg-gray-800">
          <img v-if="currentSongCover" :src="currentSongCover" alt="封面" class="w-full h-full object-cover" />
          <div v-else class="w-full h-full flex items-center justify-center">
            <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3" />
            </svg>
          </div>
        </div>
        <div
          class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center rounded-lg text-white">
          <el-icon :size="20">
            <ArrowUpBold />
          </el-icon>
        </div>
      </div>

      <div class="min-w-0 flex-1">
        <h4 class="text-white text-base font-semibold truncate">{{ playerStore.currentSong.song_title }}</h4>
        <p class="text-gray-400 text-xs truncate mt-1">{{ playerStore.currentSong.artist }}</p>
      </div>
    </div>

    <PlayerControl />

    <div class="w-[30%] flex items-center justify-end gap-4">
      <div class="flex items-center w-32 gap-2 group">
        <button @click="toggleMute" class="text-gray-400 hover:text-white transition-colors">
          <svg v-if="playerStore.volume === 0" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
          </svg>
          <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
          </svg>
        </button>
        <input type="range" v-model="volumeValue" max="100"
          @input="(e: any) => playerStore.setVolume(Number(e.target.value))" class="volume-range"
          :style="{ '--progress': volumeValue + '%' }" />
      </div>
    </div>

    <SongDetail />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { usePlayerStore } from '../stores/player'
import { useMagicKeys, useActiveElement } from '@vueuse/core'
import PlayerControl from './playerControl.vue'
import SongDetail from './songDetail.vue'

const playerStore = usePlayerStore()
const API_BASE_URL = import.meta.env.VITE_API_URL

const currentSongCover = computed(() => {

  const url = playerStore.currentSong?.song_cover_url

  if (!url) return ''

  if (url.startsWith('http')) {

    return url

  }

  const separator = url.startsWith('/') ? '' : '/'

  return `${API_BASE_URL}${separator}${url}`

})

const volumeValue = ref(playerStore.volume)
const prevVol = ref(80)

watch(() => playerStore.volume, (val) => { volumeValue.value = val })

const toggleMute = () => {
  if (playerStore.volume > 0) {
    prevVol.value = playerStore.volume
    playerStore.setVolume(0)
  } else {
    playerStore.setVolume(prevVol.value)
  }
}

const { space } = useMagicKeys()
const activeElement = useActiveElement()
watch(() => space?.value, (v) => {
  const isTyping = ['INPUT', 'TEXTAREA'].includes(activeElement.value?.tagName || '')
  if (v && !isTyping) playerStore.togglePlay()
})
</script>

<style scoped>
@reference "../assets/index.css";

.volume-range {
  @apply w-full h-1 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-white;
  background-image: linear-gradient(to right, white var(--progress), transparent var(--progress));
}

.volume-range::-webkit-slider-thumb {
  @apply appearance-none w-2.5 h-2.5 bg-white rounded-full;
}
</style>