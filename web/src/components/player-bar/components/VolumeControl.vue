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
        @input="handleVolumeInput" class="volume-range"
        :style="{ '--progress': volumeValue + '%' }" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'VolumeControl' })
import { useVolumeControl } from '../composables/use-volume-control'

const { volumeValue, toggleMute, setVolume } = useVolumeControl()

const handleVolumeInput = (event: Event) => {
  const input = event.target
  if (input instanceof HTMLInputElement) setVolume(Number(input.value))
}
</script>

<style scoped>
.volume-control {
  display: flex; gap: 16px; align-items: center; justify-content: flex-end; width: 30%;
}

.volume-control__group {
  display: flex; gap: 8px; align-items: center; width: 128px;
}

.volume-control__mute-btn {
  display: grid; width: 32px; height: 32px; flex-shrink: 0; place-items: center;
  border: 0; border-radius: 9px; background: transparent; cursor: pointer;
  color: var(--music-secondary); transition: color .2s ease, background .2s ease;
}

.volume-control__mute-btn:hover {
  color: var(--music-primary);
  background: var(--music-primary-soft);
}

.volume-control__icon {
  width: 20px; height: 20px;
}

.volume-range {
  width: 100%; height: 4px; cursor: pointer; appearance: none; accent-color: var(--music-primary); background-color: var(--music-raised); border-radius: 999px;
  background-image: linear-gradient(to right, var(--music-primary) var(--progress), transparent var(--progress));
}

.volume-range::-webkit-slider-thumb {
  width: 10px; height: 10px; appearance: none; background: var(--music-primary); border-radius: 50%;
  box-shadow: 0 2px 5px rgb(var(--music-primary-rgb) / 20%);
}

.volume-range::-moz-range-thumb {
  width: 10px; height: 10px; border: 0; background: var(--music-primary); border-radius: 50%;
}

@media (max-width: 768px) {
  .volume-control {
    width: auto;
  }

  .volume-control__group {
    width: auto;
  }

  .volume-range {
    display: none;
  }
}
</style>
