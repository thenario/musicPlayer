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
  color: #303133;
  background: #f6f8fb;
}

.detail-header {
  min-height: 82px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  flex-shrink: 0;
  background: rgb(255 255 255 / 90%);
  border-bottom: 1px solid #e5e7eb;
}

.song-meta-center {
  text-align: center;
}

.main-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
}

.meta-sub {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
}

.sub-artist {
  color: #409eff;
}

.detail-content {
  flex: 1;
  display: flex;
  gap: 32px;
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
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgb(15 23 42 / 6%);
}

.detail-footer {
  min-height: 118px;
  display: flex;
  justify-content: center;
  padding: 14px 10%;
  flex-shrink: 0;
  background: #fff;
  border-top: 1px solid #e5e7eb;
}

.player-control-container :deep(.ctrl-btn) {
  color: #606266;
}

.player-control-container :deep(.ctrl-btn:hover) {
  color: #409eff;
}

.player-control-container :deep(.player-control__play-btn) {
  color: #fff;
  background: #409eff;
  box-shadow: 0 5px 12px rgb(64 158 255 / 25%);
}

.player-control-container :deep(.progress-bar__track) {
  background: #dcdfe6;
}

.player-control-container :deep(.progress-bar__buffer) {
  background: #c0c4cc;
}

.player-control-container :deep(.progress-bar__played) {
  background: #409eff;
}

.player-control-container :deep(.progress-bar__time) {
  color: #909399;
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
  display: flex;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  color: #606266;
  cursor: pointer;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  transition: color .2s ease, background-color .2s ease, transform .2s ease;
}

.action-btn:hover {
  color: #409eff;
  background: #ecf5ff;
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
