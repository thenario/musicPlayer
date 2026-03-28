<template>
  <!-- 底部播放栏主体 -->
  <div v-if="playerStore.currentSong"
    class="w-full shrink-0 h-24 bg-gray-900/90 backdrop-blur-lg border-t border-white/5 px-6 flex items-center justify-between z-50 select-none">

    <!-- 左侧：歌曲信息 -->
    <div class="w-[30%] flex items-center gap-4 min-w-0">
      <!-- 🚩 点击封面触发展开详情页 -->
      <div class="relative group cursor-pointer shrink-0" @click="playerStore.toggleSongDetail">
        <div
          class="w-16 h-16 rounded-lg shadow-2xl overflow-hidden transition-transform group-hover:scale-105 bg-gray-800">
          <img v-if="currentSongCover" :src="currentSongCover" alt="歌曲封面" class="w-full h-full object-cover" />
          <div v-else class="w-full h-full flex items-center justify-center">
            <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3" />
            </svg>
          </div>
        </div>
        <!-- 向上箭头的悬停效果 -->
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

    <!-- 中间：播放控制 -->
    <div class="w-[40%] flex flex-col items-center gap-2">
      <div class="flex items-center gap-6">
        <button @click="togglePlayMode" class="ctrl-btn" :title="playModeTitle">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path v-if="playerStore.playMode === 'repeat_one'" stroke-linecap="round" stroke-linejoin="round"
              stroke-width="2"
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
          </svg>
        </button>

        <button @click="playerStore.previousSong" :disabled="!playerStore.hasPrevious" class="ctrl-btn scale-125">
          <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
            <path
              d="M8.445 14.832A1 1 0 0010 14v-2.798l5.445 3.63A1 1 0 0017 14V6a1 1 0 00-1.555-.832L10 8.798V6a1 1 0 00-1.555-.832l-6 4a1 1 0 000 1.664l6 4z" />
          </svg>
        </button>

        <button @click="togglePlay"
          class="w-12 h-12 flex items-center justify-center bg-white text-black rounded-full hover:scale-110 active:scale-95 transition-all shadow-lg">
          <svg v-if="playerStore.isPlaying" class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd"
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zM7 8a1 1 0 012 0v4a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v4a1 1 0 102 0V8a1 1 0 00-1-1z"
              clip-rule="evenodd" />
          </svg>
          <svg v-else class="w-6 h-6 ml-1" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z"
              clip-rule="evenodd" />
          </svg>
        </button>

        <button @click="() => playerStore.nextSong()" :disabled="!playerStore.hasNext" class="ctrl-btn scale-125">
          <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
            <path
              d="M4.555 5.168A1 1 0 003 6v8a1 1 0 001.555.832L10 11.202V14a1 1 0 001.555.832l6-4a1 1 0 000-1.664l-6-4A1 1 0 0010 6v2.798L4.555 5.168z" />
          </svg>
        </button>

        <button @click="playerStore.toggleQueueVisibility"
          :class="['ctrl-btn', { 'text-green-400': playerStore.isQueueVisible }]" title="播放队列">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7" />
          </svg>
        </button>
      </div>

      <div class="w-full flex items-center gap-3 group">
        <span class="text-[10px] font-mono text-gray-500 w-10 text-right">{{ formatTime(currentTimeDisplay) }}</span>
        <div class="relative flex-1 flex items-center">
          <input type="range" v-model="sliderValue" :max="safeDuration" step="0.1" @input="handleSeekInput"
            @change="handleSeekChange" class="custom-range"
            :style="{ '--progress': (sliderValue / (safeDuration || 1)) * 100 + '%' }" />
        </div>
        <span class="text-[10px] font-mono text-gray-500 w-10">{{ formatTime(safeDuration) }}</span>
      </div>
    </div>

    <!-- 右侧：音量控制 -->
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
import { ArrowUpBold } from '@element-plus/icons-vue'
import SongDetail from './songDetail.vue'
import { ElMessage } from 'element-plus'

const playerStore = usePlayerStore()
const API_BASE_URL = import.meta.env.VITE_API_URL;

const isDragging = ref(false)
const sliderValue = ref(0)
watch(() => playerStore.currentTime, (val) => {
  if (!isDragging.value) sliderValue.value = val || 0
})
const currentTimeDisplay = computed(() => sliderValue.value)
const safeDuration = computed(() => Number(playerStore.duration) || 0)
const handleSeekInput = () => { isDragging.value = true }
const handleSeekChange = (e: any) => {
  const val = Number(e.target.value)
  playerStore.seek(val)
  isDragging.value = false
}

const volumeValue = ref(playerStore.volume)
watch(() => playerStore.volume, (val) => { volumeValue.value = val })

const togglePlay = () => playerStore.togglePlay()
const togglePlayMode = async () => {
  const modes = ['repeat_all', 'repeat_one', 'shuffle']
  const currentIdx = modes.indexOf(playerStore.playMode === 'sequential' ? 'repeat_all' : playerStore.playMode)
  const res = await playerStore.setPlayMode(modes[(currentIdx + 1) % modes.length])
  if (!res.success) ElMessage.error("切换失败")
}
const playModeTitle = computed(() => ({
  repeat_all: '列表循环', sequential: '顺序播放', repeat_one: '单曲循环', shuffle: '随机播放'
}[playerStore.playMode] || '未知'))

const currentSongCover = computed(() => {
  const url = playerStore.currentSong?.song_cover_url
  if (!url) return ''
  if (url.startsWith('http')) {
    return url
  }
  const separator = url.startsWith('/') ? '' : '/'
  return `${API_BASE_URL}${separator}${url}`
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

const { space } = useMagicKeys()
const activeElement = useActiveElement()
watch(space, (v) => {
  const isTyping = ['INPUT', 'TEXTAREA'].includes(activeElement.value?.tagName || '')
  if (v && !isTyping) togglePlay()
})
</script>

<style scoped>
@reference "../assets/index.css";

.ctrl-btn {
  @apply text-gray-400 hover:text-white transition-all active:scale-90 disabled:opacity-20;
}

.custom-range,
.volume-range {
  @apply w-full h-1 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-green-500;
  background-image: linear-gradient(to right, var(--tw-accent-color, #10b981) var(--progress), transparent var(--progress));
}

.custom-range::-webkit-slider-thumb {
  @apply appearance-none w-3 h-3 bg-white rounded-full opacity-0 transition-opacity;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.group:hover .custom-range::-webkit-slider-thumb {
  @apply opacity-100;
}

.volume-range {
  @apply accent-white;
}

.volume-range::-webkit-slider-thumb {
  @apply appearance-none w-2.5 h-2.5 bg-white rounded-full;
}
</style>