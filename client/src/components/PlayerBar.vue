<template>
  <div v-if="playerStore.currentSong"
    class="w-full shrink-0 h-24 bg-gray-900/90 backdrop-blur-lg border-t border-white/5 px-6 flex items-center justify-between z-50 select-none">

    <div class="w-[30%] flex items-center gap-4 min-w-0">
      <div class="relative group cursor-pointer shrink-0" @click="playerStore.toggleSongDetail">
        <el-image :src="currentSongCover"
          class="w-16 h-16 rounded-lg shadow-2xl transition-transform group-hover:scale-105" fit="cover">
          <template #error>
            <div class="w-full h-full bg-gray-800 flex items-center justify-center">
              <el-icon :size="24" class="text-gray-600">
                <Music />
              </el-icon>
            </div>
          </template>
        </el-image>
        <div
          class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center rounded-lg">
          <el-icon color="white">
            <ArrowUpBold />
          </el-icon>
        </div>
      </div>

      <div class="min-w-0 flex-1">
        <h4 class="text-white text-base font-semibold truncate">{{ playerStore.currentSong.song_title }}</h4>
        <p class="text-gray-400 text-xs truncate mt-1">{{ playerStore.currentSong.artist }}</p>
      </div>
    </div>

    <div class="w-[40%] flex flex-col items-center gap-2">
      <div class="flex items-center gap-6">
        <el-tooltip :content="playModeTitle" placement="top">
          <button @click="togglePlayMode" class="ctrl-btn">
            <el-icon :size="20">
              <RefreshRight v-if="playerStore.playMode === 'repeat_all'" />
              <Refresh v-else-if="playerStore.playMode === 'repeat_one'" />
              <Sort v-else class="rotate-90" />
            </el-icon>
          </button>
        </el-tooltip>

        <button @click="playerStore.previousSong" :disabled="!playerStore.hasPrevious" class="ctrl-btn scale-125">
          <el-icon :size="22">
            <DArrowLeft />
          </el-icon>
        </button>

        <button @click="togglePlay"
          class="w-12 h-12 flex items-center justify-center bg-white text-black rounded-full hover:scale-110 active:scale-95 transition-all shadow-[0_0_20px_rgba(255,255,255,0.1)]">
          <el-icon :size="24">
            <VideoPause v-if="playerStore.isPlaying" />
            <VideoPlay v-else class="ml-1" />
          </el-icon>
        </button>

        <button @click="() => playerStore.nextSong" :disabled="!playerStore.hasNext" class="ctrl-btn scale-125">
          <el-icon :size="22">
            <DArrowRight />
          </el-icon>
        </button>

        <el-tooltip content="播放队列" placement="top">
          <button @click="playerStore.toggleQueueVisibility"
            :class="['ctrl-btn', { 'text-green-400': playerStore.isQueueVisible }]">
            <el-icon :size="20">
              <Expand />
            </el-icon>
          </button>
        </el-tooltip>
      </div>

      <div class="w-full flex items-center gap-3 group">
        <span class="text-[10px] font-mono text-gray-500 w-10 text-right">{{ formatTime(currentTimeDisplay) }}</span>
        <el-slider v-model="sliderValue" :max="safeDuration" :step="0.1" :show-tooltip="false" @input="handleSeekInput"
          @change="handleSeekChange" class="custom-slider" />
        <span class="text-[10px] font-mono text-gray-500 w-10">{{ formatTime(safeDuration) }}</span>
      </div>
    </div>

    <div class="w-[30%] flex items-center justify-end gap-4">
      <div class="flex items-center w-32 gap-2 group">
        <button @click="toggleMute" class="text-gray-400 hover:text-white transition-colors">
          <el-icon :size="18">
            <Mute v-if="playerStore.volume === 0" />
            <Microphone v-else />
          </el-icon>
        </button>
        <el-slider v-model="volumeValue" :max="100" @input="(val: number) => playerStore.setVolume(val)"
          class="volume-slider" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { usePlayerStore } from '../stores/player'
import { useMagicKeys, useActiveElement } from '@vueuse/core' // 引入工具库
import {
  VideoPlay, VideoPause, DArrowLeft, DArrowRight,
  RefreshRight, Refresh, Sort, Expand, Mute, Microphone, Mic, ArrowUpBold
} from '@element-plus/icons-vue'

const playerStore = usePlayerStore()
const API_BASE_URL = 'http://127.0.0.1:5000'

const isDragging = ref(false)
const sliderValue = ref(0)

watch(() => playerStore.currentTime, (val) => {
  if (!isDragging.value) sliderValue.value = val || 0
})

const currentTimeDisplay = computed(() => sliderValue.value)
const safeDuration = computed(() => Number(playerStore.duration) || 0)

const handleSeekInput = () => { isDragging.value = true }
const handleSeekChange = (val: number) => {
  playerStore.seek(val)
  isDragging.value = false
}

const volumeValue = ref(playerStore.volume)
watch(() => playerStore.volume, (val) => { volumeValue.value = val })

const { space } = useMagicKeys()
const activeElement = useActiveElement()
watch(space, (v) => {
  const isTyping = ['INPUT', 'TEXTAREA'].includes(activeElement.value?.tagName || '')
  if (v && !isTyping) {
    togglePlay()
  }
})

const togglePlay = () => playerStore.togglePlay()
const togglePlayMode = () => {
  const modes = ['repeat_all', 'repeat_one', 'shuffle']
  const currentIdx = modes.indexOf(playerStore.playMode === 'sequential' ? 'repeat_all' : playerStore.playMode)
  playerStore.setPlayMode(modes[(currentIdx + 1) % modes.length])
}

const playModeTitle = computed(() => ({
  repeat_all: '列表循环',
  sequential: '顺序播放',
  repeat_one: '单曲循环',
  shuffle: '随机播放'
}[playerStore.playMode] || '未知'))

const currentSongCover = computed(() => {
  const url = playerStore.currentSong?.song_cover_url
  if (!url) return ''
  return url.startsWith('http') ? url : `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
})

const formatTime = (s: number) => {
  const mins = Math.floor(s / 60)
  const secs = Math.floor(s % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const prevVol = ref(80)
const toggleMute = () => {
  if (playerStore.volume > 0) {
    prevVol.value = playerStore.volume
    playerStore.setVolume(0)
  } else {
    playerStore.setVolume(prevVol.value)
  }
}
</script>

<style scoped>
.ctrl-btn {
  @apply text-gray-400 hover:text-white transition-all active:scale-90 disabled:opacity-20;
}

:deep(.custom-slider .el-slider__runway) {
  height: 4px;
  background-color: theme('colors.gray.700');
}

:deep(.custom-slider .el-slider__bar) {
  height: 4px;
  background-color: theme('colors.green.500');
}

:deep(.custom-slider .el-slider__button) {
  width: 12px;
  height: 12px;
  border: none;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
  @apply opacity-0 transition-opacity;
}

.group:hover :deep(.el-slider__button) {
  @apply opacity-100;
}

:deep(.volume-slider .el-slider__runway) {
  height: 4px;
}

:deep(.volume-slider .el-slider__bar) {
  background-color: white;
}

:deep(.volume-slider .el-slider__button) {
  width: 10px;
  height: 10px;
}
</style>