<template>
    <Teleport to="body">
        <Transition name="slide-up">
            <div v-if="playerStore.isSongDetailVisible" class="song-detail-overlay">

                <div class="glass-bg">
                    <div class="blur-image" :style="{ backgroundImage: `url(${currentSongCover})` }"></div>
                    <div class="overlay-dark"></div>
                </div>

                <header class="detail-header">
                    <button class="action-btn exit-btn" @click="playerStore.toggleSongDetail">
                        <el-icon :size="30">
                            <ArrowDownBold />
                        </el-icon>
                    </button>

                    <div class="song-meta-center">
                        <h1 class="main-title">{{ currentSong?.song_title }}</h1>
                        <div class="meta-sub">
                            <span class="sub-artist">{{ currentSong?.artist }}</span>
                            <span class="dot">•</span>
                            <span class="sub-album">{{ currentSong?.album || '未知专辑' }}</span>
                        </div>
                    </div>

                    <button class="action-btn">
                        <el-icon :size="24">
                            <Share />
                        </el-icon>
                    </button>
                </header>

                <main class="detail-content">
                    <!-- 左侧：唱片区域（已缩小） -->
                    <div class="content-left">
                        <div class="record-box">
                            <div class="record-vinyl" :class="{ 'is-playing': isPlaying }">
                                <img :src="currentSongCover || ''" class="record-img" alt="歌曲封面" />
                                <div class="record-center"></div>
                            </div>
                        </div>
                    </div>

                    <div class="content-right">
                        <div class="lyrics-wrapper" ref="lyricsContainer">
                            <div v-if="lyrics && lyrics.length > 0" class="lyrics-scroll">
                                <div class="scroll-spacer"></div>
                                <div v-for="(line, index) in lyrics" :key="index"
                                    :ref="(el) => lyricRefs[index] = el as HTMLDivElement" class="lyric-line"
                                    :class="{ 'active': currentLineIndex === index }">
                                    <p class="text">{{ line.content }}</p>
                                    <p v-if="line.translation" class="translation">{{ line.translation }}</p>
                                </div>
                                <div class="scroll-spacer"></div>
                            </div>

                            <div v-else class="lyrics-empty">
                                <p>暂无歌词内容</p>
                            </div>
                        </div>
                    </div>
                </main>

                <footer class="detail-footer">
                    <PlayerControl style="--width-control: 85%" class="player-control-container" />
                </footer>
            </div>
        </Transition>
    </Teleport>
</template>

<script setup lang="ts">
import { ref, watch, onBeforeUpdate, nextTick, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { usePlayerStore } from '../stores/player'
import { ArrowDownBold, Share } from '@element-plus/icons-vue'
import PlayerControl from './playerControl.vue'

const playerStore = usePlayerStore()
const { currentSong, isPlaying, lyrics, currentTime } = storeToRefs(playerStore)

const lyricsContainer = ref<HTMLDivElement | null>(null)
const lyricRefs = ref<(HTMLDivElement | null)[]>([])
const currentLineIndex = ref(0)

onBeforeUpdate(() => {
    lyricRefs.value = []
})

const scrollToActiveLine = (index: number) => {
    const activeEl = lyricRefs.value[index]
    const container = lyricsContainer.value

    if (activeEl && container) {
        // 居中滚动计算
        const targetScrollTop = activeEl.offsetTop - container.clientHeight / 2 + activeEl.clientHeight / 2

        container.scrollTo({
            top: targetScrollTop,
            behavior: 'smooth'
        })
    }
}

watch(() => playerStore.currentSong?.song_id, () => {
    currentLineIndex.value = 0
    lyricRefs.value = []

    nextTick(() => {
        if (lyricsContainer.value) {
            lyricsContainer.value.scrollTop = 0
        }
    })
}, { immediate: true })

watch(currentTime, (newTime) => {
    if (!lyrics.value || lyrics.value.length === 0) return

    let index = lyrics.value.findIndex((line, i) => {
        const nextLine = lyrics.value![i + 1]
        return newTime >= line.time && (!nextLine || newTime < nextLine.time)
    })

    if (index === -1 && newTime < lyrics.value[0]!.time) {
        index = 0
    }

    if (index !== -1 && index !== currentLineIndex.value) {
        currentLineIndex.value = index
        scrollToActiveLine(index)
    }
})

const API_BASE_URL = import.meta.env.VITE_API_URL
const currentSongCover = computed(() => {
    const url = playerStore.currentSong?.song_cover_url
    if (!url) return ''
    if (url.startsWith('http')) return url
    const separator = url.startsWith('/') ? '' : '/'
    return `${API_BASE_URL}${separator}${url}`
})
</script>

<style scoped>
.lyric-line {
    font-size: 20px;
    font-weight: 500;
    line-height: 1.6;
    color: rgba(255, 255, 255, 0.3);
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    text-align: left;
    padding: 12px 0;
    max-width: 90%;
}

.lyric-line.active {
    color: #fff;
    font-size: 26px;
    font-weight: 700;
    opacity: 1;
    transform: translateX(10px);
}

.translation {
    font-size: 16px;
    margin-top: 4px;
    color: rgba(255, 255, 255, 0.4);
}

.lyrics-empty {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 18px;
    color: rgba(255, 255, 255, 0.2);
}

.song-detail-overlay {
    position: fixed;
    inset: 0;
    z-index: 3000;
    background: #000;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    color: white;
}

.detail-header {
    height: 100px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 40px;
    flex-shrink: 0;
}

.song-meta-center {
    text-align: center;
}

.main-title {
    font-size: 28px;
    font-weight: 900;
    margin-bottom: 2px;
}

.meta-sub {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-size: 16px;
    color: rgba(255, 255, 255, 0.5);
}

.sub-artist {
    color: #1DB954;
}

.detail-content {
    flex: 1;
    display: flex;
    padding: 0 8%;
    align-items: center;
    overflow: hidden;
}

.content-left {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
}

.record-box {
    width: 380px;
    height: 380px;
    position: relative;
}

.record-vinyl {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: #111;
    background: radial-gradient(circle, #333 0%, #111 30%, #000 100%);
    padding: 15px;
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.8);
    animation: rotate-record 25s linear infinite;
    animation-play-state: paused;
    will-change: transform;
}

.record-vinyl.is-playing {
    animation-play-state: running;
}

.record-img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
}

.record-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 40px;
    height: 40px;
    background: #111;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.1);
}

.content-right {
    flex: 1.2;
    height: 80%;
    padding-left: 40px;
}

.lyrics-wrapper {
    height: 100%;
    mask-image: linear-gradient(to bottom, transparent 0%, #000 20%, #000 80%, transparent 100%);
    overflow-y: auto;
    scrollbar-width: none;
}

.lyrics-wrapper::-webkit-scrollbar {
    display: none;
}

.detail-footer {
    height: 140px;
    display: flex;
    justify-content: center;
    padding: 0 10%;
    flex-shrink: 0;
}

.scroll-spacer {
    height: 40%;
}

.glass-bg {
    position: absolute;
    inset: 0;
    z-index: -1;
}

.blur-image {
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    filter: blur(80px) brightness(0.2);
    transform: scale(1.1);
}

.overlay-dark {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.6);
}

@keyframes rotate-record {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}

.slide-up-enter-active,
.slide-up-leave-active {
    transition: transform 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
    transform: translateY(100%);
}

.action-btn {
    background: none;
    border: none;
    color: #fff;
    opacity: 0.5;
    cursor: pointer;
    transition: 0.3s;
}

.action-btn:hover {
    opacity: 1;
    transform: scale(1.1);
}
</style>