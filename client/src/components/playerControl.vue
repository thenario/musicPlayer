<template>
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

            <button @click="playerStore.togglePlay"
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
            <span class="text-[10px] font-mono text-gray-500 w-10 text-right">{{ formatTime(sliderValue) }}</span>
            <div class="relative flex-1 flex items-center">
                <input type="range" v-model="sliderValue" :max="safeDuration" step="0.1" @input="handleSeekInput"
                    @change="handleSeekChange" class="custom-range"
                    :style="{ '--progress': (sliderValue / (safeDuration || 1)) * 100 + '%' }" />
            </div>
            <span class="text-[10px] font-mono text-gray-500 w-10">{{ formatTime(safeDuration) }}</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { usePlayerStore } from '../stores/player'

const playerStore = usePlayerStore()

const isDragging = ref(false)
const sliderValue = ref(0)

watch(() => playerStore.currentTime, (val) => {
    if (!isDragging.value) sliderValue.value = val || 0
})

const safeDuration = computed(() => Number(playerStore.duration) || 0)
const handleSeekInput = () => { isDragging.value = true }
const handleSeekChange = (e: any) => {
    const val = Number(e.target.value)
    playerStore.seek(val)
    isDragging.value = false
}

const togglePlayMode = async () => {
    const modes = ['repeat_all', 'repeat_one', 'shuffle']
    const currentIdx = modes.indexOf(playerStore.playMode === 'sequential' ? 'repeat_all' : playerStore.playMode)
    const res = await playerStore.setPlayMode(modes[(currentIdx + 1) % modes.length]!)
    if (!res.success) console.error("切换失败")
}

const playModeTitle = computed(() => ({
    repeat_all: '列表循环', sequential: '顺序播放', repeat_one: '单曲循环', shuffle: '随机播放'
}[playerStore.playMode] || '未知'))

const formatTime = (s: number) => {
    const mins = Math.floor(s / 60)
    const secs = Math.floor(s % 60)
    return `${mins}:${secs.toString().padStart(2, '0')}`
}
</script>

<style scoped>
@reference "../assets/index.css";

.ctrl-btn {
    @apply text-gray-400 hover:text-white transition-all active:scale-90 disabled:opacity-20;
}

.custom-range {
    @apply w-full h-1 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-green-500;
    background-image: linear-gradient(to right, #10b981 var(--progress), transparent var(--progress));
}

.custom-range::-webkit-slider-thumb {
    @apply appearance-none w-3 h-3 bg-white rounded-full opacity-0 transition-opacity;
}

.group:hover .custom-range::-webkit-slider-thumb {
    @apply opacity-100;
}
</style>