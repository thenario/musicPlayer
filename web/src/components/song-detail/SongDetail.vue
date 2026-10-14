<template>
  <Teleport to="body">
    <Transition name="slide-up">
      <div v-if="playerStore.isSongDetailVisible" class="song-detail-overlay">

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
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: var(--music-text);
  background: radial-gradient(ellipse at 20% 45%, #30261d, transparent 60%), var(--music-canvas);
}

.detail-header {
  min-height: 82px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  flex-shrink: 0;
  background: rgb(25 23 20 / 90%);
  border-bottom: 1px solid var(--music-border);
  backdrop-filter: blur(16px);
}

.song-meta-center {
  text-align: center;
  min-width: 0;
  padding-inline: 12px;
}

.main-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-sub {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  color: var(--music-muted);
}

.sub-artist {
  color: var(--music-primary);
}

.detail-content {
  flex: 1;
  display: flex;
  gap: 64px;
  max-width: 1120px;
  width: calc(100% - 64px);
  margin: 0 auto;
  padding: 28px 0;
  align-items: center;
  min-height: 0;
}

.content-left {
  flex: .9;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 0;
}

.content-right {
  flex: 1;
  align-self: stretch;
  min-height: 0;
  padding: 12px 28px;
  background: transparent;
  border-left: 1px solid var(--music-border);
}

.detail-footer {
  min-height: 118px;
  display: flex;
  justify-content: center;
  padding: 14px 10%;
  flex-shrink: 0;
  background: var(--music-surface);
  border-top: 1px solid var(--music-border);
}

.player-control-container :deep(.ctrl-btn) {
  color: var(--music-secondary);
}

.player-control-container :deep(.ctrl-btn:hover) {
  color: var(--music-primary);
}

.player-control-container :deep(.player-control__play-btn) {
  color: var(--music-on-primary);
  background: var(--music-primary);
  box-shadow: 0 5px 12px rgb(var(--music-primary-rgb) / 25%);
}

.player-control-container :deep(.progress-bar__track) {
  background: var(--music-border);
}

.player-control-container :deep(.progress-bar__buffer) {
  background: var(--music-muted);
}

.player-control-container :deep(.progress-bar__played) {
  background: var(--music-primary);
}

.player-control-container :deep(.progress-bar__time) {
  color: var(--music-muted);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.25s cubic-bezier(0.23, 1, 0.32, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}

.action-btn {
  display: flex;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  color: var(--music-secondary);
  cursor: pointer;
  background: var(--music-canvas);
  border: 1px solid var(--music-border);
  border-radius: 10px;
  transition: color .2s ease, background-color .2s ease, transform .2s ease;
}

.action-btn:hover {
  color: var(--music-primary);
  background: var(--music-primary-soft);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .detail-header {
    padding: 0 18px;
  }

  .detail-content {
    flex-direction: column;
    width: calc(100% - 32px);
    overflow-y: auto;
  }

  .content-left {
    flex: initial;
  }

  .content-right {
    flex: initial;
    width: 100%;
    min-height: 320px;
    box-sizing: border-box;
  }
}
</style>
