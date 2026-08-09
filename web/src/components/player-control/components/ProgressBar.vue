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
import { useSeekBar } from '../composables/useSeekBar'

const { sliderValue, safeDuration, bufferPercent, handleSeekInput, handleSeekChange } = useSeekBar()
</script>

<style scoped>
@reference "../../../assets/index.css";

.progress-bar {
  @apply w-full flex items-center gap-3;
}

.progress-bar__time {
  @apply text-[10px] font-mono text-gray-500 w-10;
}

.progress-bar__time--current {
  @apply text-right;
}

.progress-bar__track-wrap {
  @apply relative flex-1 h-6 flex items-center;
}

.progress-bar__track {
  @apply absolute w-full h-1 bg-gray-700 rounded-full;
}

.progress-bar__buffer {
  @apply absolute h-1 bg-gray-500 rounded-full transition-all duration-300;
}

.progress-bar__played {
  @apply absolute h-1 bg-green-500 rounded-full;
}

.custom-range {
  appearance: none;
  background: transparent;
  margin: 0;
  outline: none;
  @apply absolute w-full h-full cursor-pointer z-10;
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
  background: #ffffff;
  cursor: pointer;
  margin-top: -4px;
  box-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.2s;
}

.progress-bar:hover .custom-range::-webkit-slider-thumb {
  opacity: 1;
}
</style>
