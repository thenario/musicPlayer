<template>
  <div class="w-full flex items-center gap-3 group">
    <span class="text-[10px] font-mono text-gray-500 w-10 text-right">{{ formatDuration(sliderValue) }}</span>

    <div class="relative flex-1 h-6 flex items-center">
      <div class="absolute w-full h-1 bg-gray-700 rounded-full"></div>
      <div class="absolute h-1 bg-gray-500 rounded-full transition-all duration-300"
        :style="{ width: bufferPercent + '%' }"></div>
      <div class="absolute h-1 bg-green-500 rounded-full"
        :style="{ width: (sliderValue / (safeDuration || 1)) * 100 + '%' }"></div>

      <input type="range" v-model="sliderValue" :max="safeDuration" step="0.1" @input="handleSeekInput"
        @change="handleSeekChange" class="custom-range absolute w-full h-full cursor-pointer z-10" />
    </div>

    <span class="text-[10px] font-mono text-gray-500 w-10">{{ formatDuration(safeDuration) }}</span>
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

.custom-range {
  appearance: none;
  background: transparent;
  margin: 0;
  outline: none;
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

.group:hover .custom-range::-webkit-slider-thumb {
  opacity: 1;
}
</style>
