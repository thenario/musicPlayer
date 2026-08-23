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
  width: 40px; color: #909399; font-family: monospace; font-size: 10px;
}

.progress-bar__time--current {
  text-align: right;
}

.progress-bar__track-wrap {
  position: relative; display: flex; flex: 1; height: 24px; align-items: center;
}

.progress-bar__track {
  position: absolute; width: 100%; height: 4px; background: #dcdfe6; border-radius: 999px;
}

.progress-bar__buffer {
  position: absolute; height: 4px; background: #c0c4cc; border-radius: 999px; transition: width .3s ease;
}

.progress-bar__played {
  position: absolute; height: 4px; background: #409eff; border-radius: 999px;
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
  background: #409eff;
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
