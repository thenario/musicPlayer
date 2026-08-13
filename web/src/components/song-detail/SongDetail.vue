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
            <RecordDisc :cover="currentSongCover" :is-playing="isPlaying" />
          </div>

          <div class="content-right">
            <LyricsPanel />
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
defineOptions({ name: 'SongDetail' })
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { usePlayerStore } from '@/stores/player'
import { getImageUrl } from '@/utils/format'
import { ArrowDownBold, Share } from '@element-plus/icons-vue'
import RecordDisc from './components/RecordDisc.vue'
import LyricsPanel from './components/LyricsPanel.vue'
import PlayerControl from '@/components/player-control/PlayerControl.vue'

const playerStore = usePlayerStore()
const { currentSong, isPlaying } = storeToRefs(playerStore)

const currentSongCover = computed(() => getImageUrl(currentSong.value?.song_cover_url))
</script>

<style scoped>
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

.content-right {
  flex: 1.2;
  height: 80%;
  padding-left: 40px;
}

.detail-footer {
  height: 140px;
  display: flex;
  justify-content: center;
  padding: 0 10%;
  flex-shrink: 0;
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
