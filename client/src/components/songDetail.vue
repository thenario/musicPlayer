<!-- SongDetail.vue -->
<template>
    <Teleport to="body">
        <Transition name="slide-up">
            <div v-if="playerStore.isSongDetailVisible" class="song-detail-overlay">

                <!-- 背景层 -->
                <div class="glass-bg">
                    <div class="blur-image" :style="{ backgroundImage: `url(${currentSongCover})` }"></div>
                    <div class="overlay-dark"></div>
                </div>

                <!-- 顶部信息：曲名、Artist、Album -->
                <header class="detail-header">
                    <button class="action-btn exit-btn" @click="playerStore.toggleSongDetail">
                        <el-icon :size="30">
                            <ArrowDownBold />
                        </el-icon>
                    </button>

                    <div class="song-meta-center">
                        <h1 class="main-title">{{ currentSong?.song_title }}</h1>
                        <h2 class="sub-artist">{{ currentSong?.artist }}</h2>
                        <h3 class="sub-album">{{ currentSong?.album || '未知专辑' }}</h3>
                    </div>

                    <button class="action-btn"><el-icon :size="24">
                            <Share />
                        </el-icon></button>
                </header>

                <!-- 中间主体布局 -->
                <main class="detail-content">
                    <!-- 左侧：唱片旋转 -->
                    <div class="content-left">
                        <div class="record-box">
                            <div class="record-vinyl" :class="{ 'is-playing': isPlaying }">
                                <img :src="currentSongCover || ''" class="record-img" alt="歌曲封面" />
                                <div class="record-center"></div>
                            </div>
                        </div>
                    </div>

                    <!-- 右侧：歌词展示 -->
                    <div class="content-right">
                        <div class="lyrics-wrapper">
                            <div class="lyrics-scroll">
                                <div class="lyric-line-active">
                                    {{ currentSong?.lyrics || '暂无歌词信息' }}
                                </div>
                                <div class="footer-spacer"></div>
                            </div>
                        </div>
                    </div>
                </main>

                <footer class="detail-footer">
                    <PlayerControl style="--width-control: 1200px" class="player-control-container" />
                </footer>
            </div>
        </Transition>
    </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'
import { ArrowDownBold, Share } from '@element-plus/icons-vue'
import PlayerControl from './playerControl.vue'

const playerStore = usePlayerStore()
const { currentSong, isPlaying } = storeToRefs(playerStore)
const API_BASE_URL = import.meta.env.VITE_API_URL

const currentSongCover = computed(() => {

    const url = playerStore.currentSong?.song_cover_url

    if (!url) return ''

    if (url.startsWith('http')) {

        return url

    }

    const separator = url.startsWith('/') ? '' : '/'

    return `${API_BASE_URL}${separator}${url}`

})
</script>

<style scoped>
.player-control-container {
    width: var(--width-control, 40%);
    flex-grow: 1;
    max-width: 90vw;
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
    filter: blur(60px) brightness(0.3);
    transform: scale(1.2);
}

.overlay-dark {
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, rgba(0, 0, 0, 0.4) 0%, rgba(0, 0, 0, 0.8) 100%);
}

.detail-header {
    height: 120px;
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
    font-size: 32px;
    font-weight: 900;
    margin-bottom: 4px;
}

.sub-artist {
    font-size: 18px;
    color: #1DB954;
    margin-bottom: 2px;
}

.sub-album {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.4);
}

.detail-content {
    flex: 1;
    display: flex;
    padding: 0 10%;
    align-items: center;
    overflow: hidden;
}

.content-left {
    flex: 1.2;
    display: flex;
    justify-content: center;
    align-items: center;
}

.record-box {
    width: 460px;
    height: 460px;
    position: relative;
}

.record-vinyl {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: #111;
    background: radial-gradient(circle, #333 0%, #111 30%, #000 100%);
    padding: 18px;
    box-shadow: 0 20px 80px rgba(0, 0, 0, 0.8);
    animation: rotate-record 25s linear infinite;
    animation-play-state: paused;
    will-change: transform;
    transform: translateZ(0);
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
    width: 50px;
    height: 50px;
    background: #111;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.1);
}

.content-right {
    flex: 1;
    height: 70%;
    padding-left: 80px;
}

.lyrics-wrapper {
    height: 100%;
    mask-image: linear-gradient(to bottom, transparent, #000 15%, #000 85%, transparent 100%);
    overflow-y: auto;
    scrollbar-width: none;
}

.lyrics-wrapper::-webkit-scrollbar {
    display: none;
}

.lyric-line-active {
    font-size: 28px;
    font-weight: 700;
    line-height: 1.8;
    color: rgba(255, 255, 255, 0.8);
    white-space: pre-wrap;
}

.detail-footer {
    height: 160px;
    display: flex;
    justify-content: center;
    padding: 0 15%;
    flex-shrink: 0;
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

.footer-spacer {
    height: 200px;
}

.action-btn {
    background: none;
    border: none;
    color: #fff;
    opacity: 0.5;
    cursor: pointer;
}

.action-btn:hover {
    opacity: 1;
}
</style>