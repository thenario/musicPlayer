<template>
  <div v-if="playerStore.currentSong"
    class="w-full shrink-0 h-20 bg-gray-900 border-t border-gray-800 px-6 flex items-center justify-between z-50 select-none relative">
    <!-- 左侧：歌曲信息 (30%) -->
    <div class="w-[30%] flex items-center gap-4 min-w-0">

      <!-- 封面图容器 - 点击打开歌曲详情 -->
      <div
        class="w-14 h-14 bg-gray-700 rounded shadow-md flex-shrink-0 flex items-center justify-center overflow-hidden relative group cursor-pointer"
        @click.stop="playerStore.toggleSongDetail" title="点击查看歌曲详情">
        <img v-if="currentSongCover && !imageLoadError" :src="currentSongCover" alt="Cover"
          class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
          @error="handleImageError">
        <svg v-else class="w-8 h-8 text-gray-500" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z" />
        </svg>
        <!-- 悬停遮罩与提示图标 -->
        <div v-if="currentSongCover"
          class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center backdrop-blur-[1px] pointer-events-none">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7"></path>
          </svg>
        </div>
      </div>

      <!-- 歌曲文字信息 -->
      <div class="min-w-0 flex-1">
        <h4 class="text-white text-sm font-medium truncate cursor-default" :title="playerStore.currentSong.title">
          {{ playerStore.currentSong.title }}
        </h4>
        <p class="text-gray-400 text-xs truncate mt-0.5 cursor-default" :title="playerStore.currentSong.artist">
          {{ playerStore.currentSong.artist }}
        </p>
      </div>
    </div>

    <!-- 中间：播放控制 (40%) -->
    <div class="w-[40%] flex flex-col items-center justify-center min-w-0">
      <div class="flex items-center space-x-8 mb-2">

        <!-- 播放模式切换按钮 (单按钮循环) -->
        <button @click="togglePlayMode"
          class="text-gray-400 hover:text-white transition-all active:scale-90 relative group" :title="playModeTitle">
          <!-- 列表循环 / 顺序播放 -->
          <svg v-if="playerStore.playMode === 'repeat_all' || playerStore.playMode === 'sequential'" class="w-5 h-5"
            fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15">
            </path>
          </svg>
          <!-- 单曲循环 -->
          <svg v-else-if="playerStore.playMode === 'repeat_one'" class="w-5 h-5" fill="none" stroke="currentColor"
            viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M13 15h-2c-1.105 0-2 .895-2 2s.895 2 2 2h2c1.105 0 2-.895 2-2s-.895-2-2-2zM12 9v4" />
          </svg> <!-- 这是一个更合适的单曲循环图标，带有数字 1 -->
          <!-- 随机播放 -->
          <svg v-else-if="playerStore.playMode === 'shuffle'" class="w-5 h-5" fill="none" stroke="currentColor"
            viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M7 16l-4-4m0 0l4-4m-4 4h14m-4 4l4-4m0 0l-4-4"></path>
          </svg> <!-- 这是一个更合适的随机播放图标 -->

          <!-- Tooltip -->
          <span
            class="absolute -top-8 left-1/2 -translate-x-1/2 bg-black/80 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none">
            {{ playModeTitle }}
          </span>
        </button>

        <!-- 上一首 -->
        <button @click="playerStore.previousSong" :disabled="!playerStore.hasPrevious"
          class="text-gray-400 hover:text-white disabled:opacity-30 active:scale-90 transition-transform" title="上一首">
          <svg class="w-6 h-6 fill-current" viewBox="0 0 24 24">
            <path d="M6 6h2v12H6zm3.5 6l8.5 6V6z" />
          </svg>
        </button>

        <!-- 播放/暂停 (大按钮) -->
        <button @click="togglePlay"
          class="w-10 h-10 flex items-center justify-center bg-white rounded-full hover:scale-105 active:scale-95 transition-all shadow-lg hover:bg-gray-100"
          :title="playerStore.isPlaying ? '暂停' : '播放'">
          <svg v-if="!playerStore.isPlaying" class="w-6 h-6 text-black fill-current ml-0.5" viewBox="0 0 24 24">
            <path d="M8 5v14l11-7z" />
          </svg>
          <svg v-else class="w-6 h-6 text-black fill-current" viewBox="0 0 24 24">
            <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" />
          </svg>
        </button>

        <!-- 下一首 -->
        <button @click="playerStore.nextSong" :disabled="!playerStore.hasNext"
          class="text-gray-400 hover:text-white disabled:opacity-30 active:scale-90 transition-transform" title="下一首">
          <svg class="w-6 h-6 fill-current" viewBox="0 0 24 24">
            <path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z" />
          </svg>
        </button>

        <!-- 空占位 (保持按钮组居中对齐) -->
        <div class="w-5 h-5"></div>
      </div>

      <!-- 进度条 -->
      <div class="w-full flex items-center space-x-3 text-xs font-mono text-gray-400 min-w-0">
        <span class="w-10 text-right">{{ formatTime(currentTimeDisplay) }}</span>
        <input type="range" min="0" :max="safeDuration" step="0.1" :value="currentTimeDisplay" @input="handleSeekInput"
          @change="handleSeekChange" class="flex-1 h-1 rounded-lg cursor-pointer range-slider"
          :style="{ '--progress': progressPercent + '%' }" :disabled="safeDuration <= 0">
        <span class="w-10">{{ formatTime(safeDuration) }}</span>
      </div>
    </div>

    <!-- 右侧：队列 & 音量 (30%) -->
    <div class="w-[30%] flex items-center justify-end gap-6 min-w-0">
      <!-- 队列按钮 -->
      <button @click="playerStore.toggleQueueVisibility"
        class="text-gray-400 hover:text-white transition-all active:scale-90"
        :class="{ 'text-green-500': playerStore.isQueueVisible }" title="打开/关闭播放列表">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
        </svg>
      </button>

      <!-- 音量控制 -->
      <div class="flex items-center w-32 gap-3 group min-w-[100px]">
        <button @click="toggleMute" class="text-gray-400 hover:text-white active:scale-90 transition-transform"
          :title="playerStore.volume === 0 ? '取消静音' : '静音'">
          <svg v-if="playerStore.volume === 0" class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73 4.27 3zM12 4L9.91 6.09 12 8.18V4z" />
          </svg>
          <svg v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
            <path
              d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z" />
          </svg>
        </button>
        <input type="range" min="0" max="100" :value="playerStore.volume"
          @input="e => playerStore.setVolume(Number(e.target.value))"
          class="flex-1 h-1 rounded-lg cursor-pointer range-slider" :style="{ '--progress': playerStore.volume + '%' }">
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { usePlayerStore } from '../stores/player'

const playerStore = usePlayerStore()
const API_BASE_URL = 'http://127.0.0.1:5000'

// ==========================================
// 1. 封面图片逻辑 (Cover Image)
// ==========================================
const imageLoadError = ref(false)

// 智能计算封面链接
const currentSongCover = computed(() => {
  const url = playerStore.currentSong?.cover_url
  if (!url) return null

  // 完整链接直接返回，相对路径进行拼接
  if (url.startsWith('http')) return url
  const cleanPath = url.startsWith('/') ? url : `/${url}`
  return `${API_BASE_URL}${cleanPath}`
})

// 图片加载失败的回调
const handleImageError = () => {
  imageLoadError.value = true
}

// 监听封面变化，重置错误状态
watch(currentSongCover, (newUrl, oldUrl) => {
  if (newUrl !== oldUrl) {
    imageLoadError.value = false
  }
})

// ==========================================
// 2. 播放控制逻辑 (Playback Controls)
// ==========================================
const togglePlay = () => playerStore.togglePlay()

const togglePlayMode = () => {
  const modes = ['repeat_all', 'repeat_one', 'shuffle']
  // 将 sequential 视为 repeat_all 处理
  let currentMode = playerStore.playMode === 'sequential' ? 'repeat_all' : playerStore.playMode

  const currentIdx = modes.indexOf(currentMode)
  const nextIdx = (currentIdx + 1) % modes.length

  playerStore.setPlayMode(modes[nextIdx])
}

const playModeTitle = computed(() => {
  const map: Record<string, string> = {
    'repeat_all': '列表循环',
    'sequential': '列表循环',
    'repeat_one': '单曲循环',
    'shuffle': '随机播放'
  }
  return map[playerStore.playMode] || '未知模式'
})

// ==========================================
// 3. 进度条与拖拽逻辑 (Progress & Seek)
// ==========================================
const isDragging = ref(false)
const dragValue = ref(0)

const safeDuration = computed(() => {
  const d = Number(playerStore.duration)
  return (d && d > 0) ? d : 0
})

// 拖拽时显示拖拽值，否则显示真实时间
const currentTimeDisplay = computed(() => {
  return isDragging.value ? dragValue.value : (playerStore.currentTime || 0)
})

const progressPercent = computed(() => {
  if (safeDuration.value <= 0) return 0
  return Math.min(100, Math.max(0, (currentTimeDisplay.value / safeDuration.value) * 100))
})

const handleSeekInput = (e) => {
  isDragging.value = true
  dragValue.value = Number(e.target.value)
}

const handleSeekChange = (e) => {
  const val = Number(e.target.value)
  playerStore.seek(val)
  // 稍微延迟释放拖拽状态，防止 UI 跳变
  setTimeout(() => {
    isDragging.value = false
  }, 50)
}

// ==========================================
// 4. 音量逻辑 (Volume)
// ==========================================
const prevVolume = ref(80)

const toggleMute = () => {
  if (playerStore.volume > 0) {
    prevVolume.value = playerStore.volume
    playerStore.setVolume(0)
  } else {
    playerStore.setVolume(prevVolume.value || 80)
  }
}

// ==========================================
// 5. 工具函数 (Utils)
// ==========================================
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// ==========================================
// 6. 生命周期与快捷键 (Lifecycle)
// ==========================================
const handleKeyDown = (e) => {
  // 空格键切换播放/暂停 (排除输入框)
  if (e.code === 'Space' && !e.target.matches('input, textarea, button')) {
    e.preventDefault()
    togglePlay()
  }
}

onMounted(() => {
  // 初始化状态
  imageLoadError.value = false

  // 绑定键盘事件
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<style scoped>
.range-slider {
  -webkit-appearance: none;
  appearance: none;
  background: #4B5563;
  background-image: linear-gradient(#10B981, #10B981);
  background-size: var(--progress) 100%;
  background-repeat: no-repeat;
  height: 4px;
}

.range-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: white;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  opacity: 0;
  transition: opacity 0.1s ease, transform 0.1s;
}

.range-slider:hover::-webkit-slider-thumb {
  opacity: 1;
}

.range-slider:hover {
  background-image: linear-gradient(#10B981, #10B981);
}

.range-slider::-moz-range-thumb {
  width: 12px;
  height: 12px;
  border: none;
  border-radius: 50%;
  background: white;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.1s ease;
}

.range-slider:hover::-moz-range-thumb {
  opacity: 1;
}

input[type=range]:focus {
  outline: none;
}
</style>