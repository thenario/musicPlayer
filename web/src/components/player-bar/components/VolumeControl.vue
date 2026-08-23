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
  color: #606266; transition: color .2s ease;
}

.volume-control__mute-btn:hover {
  color: #409eff;
}

.volume-control__icon {
  width: 20px; height: 20px;
}

.volume-range {
  width: 100%; height: 4px; cursor: pointer; appearance: none; accent-color: #409eff; background-color: #dcdfe6; border-radius: 999px;
  background-image: linear-gradient(to right, #409eff var(--progress), transparent var(--progress));
}

.volume-range::-webkit-slider-thumb {
  width: 10px; height: 10px; appearance: none; background: #409eff; border-radius: 50%;
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
