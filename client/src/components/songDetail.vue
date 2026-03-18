<template>
    <el-dialog v-model="playerStore.isSongDetailVisible" fullscreen :show-close="false" :modal="false"
        class="song-detail-dialog">
        <div class="full-page-player">
            <transition name="fade">
                <div :key="currentSongCover ? currentSongCover : ''"
                    class="absolute inset-0 bg-cover bg-center transition-all duration-1000 blur-[100px] scale-125 opacity-50"
                    :style="{ backgroundImage: `url(${currentSongCover})` }"></div>
            </transition>
            <div class="absolute inset-0 bg-linear-to-b from-black/20 via-black/60 to-black/80"></div>

            <div class="relative z-10 flex justify-between items-center p-8">
                <el-button link class="text-white! opacity-60 hover:opacity-100" @click="playerStore.toggleSongDetail">
                    <el-icon :size="32">
                        <ArrowDownBold />
                    </el-icon>
                </el-button>

                <div class="text-center">
                    <p class="text-xs uppercase tracking-[0.2em] text-gray-400 mb-1">正在播放</p>
                    <p class="text-sm font-medium text-white/80">{{ currentSong?.album || '未知专辑' }}</p>
                </div>

                <el-button link class="text-white! opacity-60"><el-icon :size="24">
                        <Share />
                    </el-icon></el-button>
            </div>

            <el-row class="relative z-10 flex-1 overflow-hidden px-12 items-center">
                <el-col :lg="12" class="hidden lg:flex justify-center items-center">
                    <div class="vinyl-record-container">
                        <div class="vinyl-disk" :class="{ 'is-playing': isPlaying }">
                            <img :src="currentSongCover ? currentSongCover : ''" alt="cover" class="disk-cover" />
                            <div class="disk-center-dot"></div>
                        </div>
                    </div>
                </el-col>

                <el-col :lg="12" :xs="24" class="h-full flex flex-col justify-center max-w-xl">
                    <div class="song-info mb-10">
                        <h1 class="text-5xl font-bold text-white mb-4 tracking-tight leading-tight">
                            {{ currentSong?.song_title }}
                        </h1>
                        <div class="flex items-center gap-4 text-xl text-green-400 font-medium">
                            <span>{{ currentSong?.artist }}</span>
                            <el-tag size="small" effect="dark" type="info" round
                                class="bg-white/10 border-none">Hi-Res</el-tag>
                        </div>
                    </div>

                    <div class="lyric-wrapper relative">
                        <el-scrollbar ref="lyricScroll" class="h-[50vh]">
                            <div class="lyric-content whitespace-pre-wrap">
                                {{ currentSong?.lyrics || '暂无歌词信息' }}
                            </div>
                            <div class="h-40"></div>
                        </el-scrollbar>
                    </div>
                </el-col>
            </el-row>
        </div>
    </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'
import { ArrowDownBold, Share } from '@element-plus/icons-vue'

const playerStore = usePlayerStore()
const { currentSong, isPlaying } = storeToRefs(playerStore)
const API_BASE_URL = 'http://127.0.0.1:3000'

const currentSongCover = computed(() => {
    const url = currentSong.value?.song_cover_url
    if (!url) return null
    if (url.startsWith('http')) return url
    return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
})
</script>

<style scoped>
@reference "../assets/index.css";

:deep(.song-detail-dialog) {
    padding: 0 !important;
    background: #0f172a;
}

.full-page-player {
    @apply relative h-full w-full flex flex-col overflow-hidden text-white;
}

.vinyl-disk {
    @apply relative w-[450px] h-[450px] rounded-full border-3 border-white/5 shadow-2xl;
    background: repeating-radial-gradient(circle, #111, #111 2px, #222 4px, #111 6px);
    animation: spin 20s linear infinite;
    animation-play-state: paused;
}

.vinyl-disk.is-playing {
    animation-play-state: running;
}

.disk-cover {
    @apply absolute inset-[15%] w-[70%] h-[70%] rounded-full object-cover border-4 border-black/50;
}

.disk-center-dot {
    @apply absolute inset-[48%] w-[4%] h-[4%] bg-gray-900 rounded-full border border-white/20 shadow-inner;
}

.lyric-content {
    @apply text-2xl leading-14 text-white/40 transition-all duration-500;
}

@keyframes spin {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}

.lyric-wrapper::before,
.lyric-wrapper::after {
    content: '';
    @apply absolute left-0 right-0 h-20 z-10 pointer-events-none;
}

.lyric-wrapper::before {
    top: 0;
    background: linear-gradient(to bottom, #0f172a, transparent);
}

.lyric-wrapper::after {
    bottom: 0;
    background: linear-gradient(to top, #0f172a, transparent);
}
</style>