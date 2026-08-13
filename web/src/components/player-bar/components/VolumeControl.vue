<template>
  <div class="volume-control">
    <div class="volume-control__group">
      <button @click="toggleMute" class="volume-control__mute-btn">
        <svg v-if="volumeValue === 0" class="volume-control__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
        </svg>
        <svg v-else class="volume-control__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
        </svg>
      </button>
      <input type="range" v-model="volumeValue" max="100"
        @input="(e: any) => setVolume(Number(e.target.value))" class="volume-range"
        :style="{ '--progress': volumeValue + '%' }" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'VolumeControl' })
import { useVolumeControl } from '../composables/use-volume-control'

const { volumeValue, toggleMute, setVolume } = useVolumeControl()
</script>

<style scoped>
@reference "../../../assets/index.css";

.volume-control {
  @apply w-[30%] flex items-center justify-end gap-4;
}

.volume-control__group {
  @apply flex items-center w-32 gap-2;
}

.volume-control__mute-btn {
  @apply text-gray-400 transition-colors;
}

.volume-control__mute-btn:hover {
  @apply text-white;
}

.volume-control__icon {
  @apply w-5 h-5;
}

.volume-range {
  @apply w-full h-1 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-white;
  background-image: linear-gradient(to right, white var(--progress), transparent var(--progress));
}

.volume-range::-webkit-slider-thumb {
  @apply appearance-none w-2.5 h-2.5 bg-white rounded-full;
}
</style>
