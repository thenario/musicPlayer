<template>
    <transition name="slide-up">
        <!-- 全屏容器，z-index 要很高以覆盖播放条 -->
        <div v-if="playerStore.isSongDetailVisible"
            class="fixed inset-0 z-60 bg-gray-900 flex flex-col overflow-hidden">
            <!-- 1. 背景层：使用当前封面进行高斯模糊 -->
            <div class="absolute inset-0 bg-cover bg-center transition-all duration-700 opacity-40 blur-[80px] scale-110"
                :style="`background-image: url(${currentSongCover})`"></div>
            <!-- 遮罩层，让文字更清晰 -->
            <div class="absolute inset-0 bg-black/40"></div>

            <!-- 2. 顶部栏：关闭按钮 -->
            <div class="relative z-10 h-20 flex items-center justify-between px-8 shrink-0">
                <button @click="playerStore.toggleSongDetail"
                    class="p-2 text-gray-400 hover:text-white rounded-full hover:bg-white/10 transition-colors"
                    title="收起详情页">
                    <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                    </svg>
                </button>
            </div>

            <!-- 3. 主内容区：左图右文 -->
            <div class="relative z-10 flex-1 flex items-center justify-center gap-24 px-20 pb-20 min-h-0">

                <!-- 左侧：唱片动画 -->
                <div class="w-[45vh] h-[45vh] max-w-[500px] max-h-[500px] shrink-0 relative hidden lg:block">
                    <div class="w-full h-full rounded-full bg-black border-8 border-gray-800/50 shadow-2xl flex items-center justify-center overflow-hidden animate-spin-slow"
                        :style="{ animationPlayState: isPlaying ? 'running' : 'paused' }">
                        <!-- 封面图 -->
                        <img v-if="currentSongCover" :src="currentSongCover"
                            class="w-[70%] h-[70%] rounded-full object-cover">
                        <!-- 没有封面时的占位 -->
                        <div v-else
                            class="w-[70%] h-[70%] bg-gray-700 rounded-full flex items-center justify-center text-gray-500">
                            <span class="text-xs">No Cover</span>
                        </div>
                    </div>
                </div>

                <!-- 右侧：纯文本歌词区域 -->
                <div class="flex-1 h-full max-w-2xl flex flex-col items-center lg:items-start text-center lg:text-left">

                    <!-- 歌曲信息标题 -->
                    <div class="mb-8 w-full">
                        <h1 class="text-3xl font-bold text-white mb-3">{{ currentSong?.song_title }}</h1>
                        <div class="text-lg text-gray-300">
                            <span class="text-gray-400">歌手：</span>{{ currentSong?.artist }}
                            <span class="mx-2 text-gray-600">|</span>
                            <span class="text-gray-400">专辑：</span>{{ currentSong?.album }}
                        </div>
                    </div>

                    <!-- 歌词滚动容器 -->
                    <div class="w-full flex-1 overflow-y-auto custom-scrollbar pr-4">
                        <!-- 
                核心 CSS: whitespace-pre-wrap 
                这会让 \n 换行符生效，并在每一行太长时自动换行 
            -->
                        <div
                            class="text-gray-200 text-lg leading-10 whitespace-pre-wrap font-medium opacity-90 tracking-wide">
                            {{ currentSong?.lyrics || '暂无歌词信息' }}
                        </div>

                        <!-- 底部留白 -->
                        <div class="h-20"></div>
                    </div>

                </div>

            </div>
        </div>
    </transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'

const playerStore = usePlayerStore()
const { currentSong, isPlaying } = storeToRefs(playerStore)
const API_BASE_URL = 'http://127.0.0.1:5000'

// 封面处理 (和 PlayerBar 保持一致)
const currentSongCover = computed(() => {
    const url = currentSong.value?.song_cover_url
    if (!url) return null
    if (url.startsWith('http')) return url
    return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
})
</script>

<style scoped>
/* 进出动画：从下往上滑 */
.slide-up-enter-active,
.slide-up-leave-active {
    transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
    transform: translateY(100%);
}

/* 唱片旋转动画 */
.animate-spin-slow {
    animation: spin 20s linear infinite;
}

@keyframes spin {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}

/* 滚动条美化 */
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}
</style>