<template>
  <div class="progress-bar">
    <span class="progress-bar__time progress-bar__time--current">{{ formatDuration(sliderValue) }}</span>

    <div class="progress-bar__track-wrap">
      <div class="progress-bar__track"></div>
      <div class="progress-bar__buffer"
        :style="{ width: bufferPercent + '%' }"></div>
      <div class="progress-bar__played"
        :style="{ width: (sliderValue / (safeDuration || 1)) * 100 + '%' }"></div>

      <input type="range" v-model="sliderValue" :max="safeDuration" step="0.1" @input="handleSeekInput"
        @change="handleSeekChange" class="custom-range" />
    </div>

    <span class="progress-bar__time">{{ formatDuration(safeDuration) }}</span>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'ProgressBar' })
import { formatDuration } from '@/utils/format'
import { useSeekBar } from '../composables/use-seek-bar'

const { sliderValue, safeDuration, bufferPercent, handleSeekInput, handleSeekChange } = useSeekBar()
</script>

<style scoped>
.progress-bar {
  display: flex; gap: 12px; align-items: center; width: 100%;
}

.progress-bar__time {
  width: 40px; color: #928ca5; font-family: inherit; font-size: 11px; font-variant-numeric: tabular-nums;
}

.progress-bar__time--current {
  text-align: right;
}

.progress-bar__track-wrap {
  position: relative; display: flex; flex: 1; height: 24px; align-items: center;
}

.progress-bar__track {
  position: absolute; width: 100%; height: 4px; background: #e9e5f2; border-radius: 999px;
}

.progress-bar__buffer {
  position: absolute; height: 4px; background: #cfc6ea; border-radius: 999px; transition: width .3s ease;
}

.progress-bar__played {
  position: absolute; height: 4px; background: var(--music-primary); border-radius: 999px;
}

.custom-range {
  appearance: none;
  background: transparent;
  margin: 0;
  outline: none;
  position: absolute; z-index: 10; width: 100%; height: 100%; cursor: pointer;
}

.custom-range::-webkit-slider-runnable-track {
  background: transparent;
  height: 4px;
}

.custom-range::-webkit-slider-thumb {
  -webkit-appearance: none;
  height: 12px;
  width: 12px;
  border-radius: 50%;
  background: var(--music-primary);
  cursor: pointer;
  margin-top: -4px;
  box-shadow: 0 2px 6px rgb(109 91 208 / 25%);
  opacity: 0;
  transition: opacity 0.2s;
}

.progress-bar:hover .custom-range::-webkit-slider-thumb,
.custom-range:focus-visible::-webkit-slider-thumb {
  opacity: 1;
}

.custom-range::-moz-range-track {
  height: 4px;
  background: transparent;
}

.custom-range::-moz-range-thumb {
  width: 12px;
  height: 12px;
  border: 0;
  border-radius: 50%;
  background: var(--music-primary);
}
</style>
