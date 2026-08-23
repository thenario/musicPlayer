<template>
  <div class="player-control" :style="{ width: 'var(--width-control, 40%)' }">
    <div class="player-control__buttons">
      <button @click="togglePlayMode" class="ctrl-btn" :title="playModeTitle">
        <svg class="player-control__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path v-if="playerStore.playMode === 'repeat_one'" stroke-linecap="round" stroke-linejoin="round"
            stroke-width="2"
            d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
        </svg>
      </button>

      <button @click="playerStore.previousSong" :disabled="!playerStore.hasPrevious" class="ctrl-btn ctrl-btn--skip">
        <svg class="player-control__icon--lg" fill="currentColor" viewBox="0 0 20 20">
          <path
            d="M8.445 14.832A1 1 0 0010 14v-2.798l5.445 3.63A1 1 0 0017 14V6a1 1 0 00-1.555-.832L10 8.798V6a1 1 0 00-1.555-.832l-6 4a1 1 0 000 1.664l6 4z" />
        </svg>
      </button>

      <button @click="playerStore.togglePlay"
        class="player-control__play-btn">
        <svg v-if="playerStore.isPlaying" class="player-control__icon--lg" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd"
            d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zM7 8a1 1 0 012 0v4a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v4a1 1 0 102 0V8a1 1 0 00-1-1z"
            clip-rule="evenodd" />
        </svg>
        <svg v-else class="player-control__icon--lg-spaced" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z"
            clip-rule="evenodd" />
        </svg>
      </button>

      <button @click="() => playerStore.nextSong()" :disabled="!playerStore.hasNext" class="ctrl-btn ctrl-btn--skip">
        <svg class="player-control__icon--lg" fill="currentColor" viewBox="0 0 20 20">
          <path
            d="M4.555 5.168A1 1 0 003 6v8a1 1 0 001.555.832L10 11.202V14a1 1 0 001.555.832l6-4a1 1 0 000-1.664l-6-4A1 1 0 0010 6v2.798L4.555 5.168z" />
        </svg>
      </button>

      <button @click="playerStore.toggleQueueVisibility"
        :class="['ctrl-btn', { 'is-active': playerStore.isQueueVisible }]" title="播放队列">
        <svg class="player-control__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7" />
        </svg>
      </button>
    </div>

    <ProgressBar />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'PlayerControl' })
import { usePlayerStore } from '@/stores/player'
import ProgressBar from './components/ProgressBar.vue'
import { usePlayMode } from './composables/use-play-mode'

const playerStore = usePlayerStore()
const { playModeTitle, togglePlayMode } = usePlayMode()
</script>

<style scoped>
.player-control {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.player-control__buttons {
  display: flex;
  gap: 24px;
  align-items: center;
}

.player-control__icon {
  width: 20px;
  height: 20px;
}

.player-control__icon--lg {
  width: 24px;
  height: 24px;
}

.player-control__icon--lg-spaced {
  width: 24px;
  height: 24px;
  margin-left: 4px;
}

.player-control__play-btn {
  display: flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #409eff;
  border-radius: 50%;
  box-shadow: 0 5px 12px rgb(64 158 255 / 25%);
  transition: transform .2s ease;
}

.player-control__play-btn:hover {
  transform: scale(1.1);
}

.player-control__play-btn:active {
  transform: scale(.95);
}

.ctrl-btn--skip {
  transform: scale(1.25);
}

.ctrl-btn.is-active {
  color: #409eff;
}

.ctrl-btn {
  padding: 4px;
  color: #606266;
  cursor: pointer;
  background: transparent;
  border: 0;
  transition: color .2s ease, transform .2s ease;
  display: flex;
  align-items: center;
}

.ctrl-btn:hover {
  color: #409eff;
}

.ctrl-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .player-control {
    width: auto !important;
  }

  .player-control__buttons {
    gap: 12px;
  }

  .player-control__play-btn {
    width: 42px;
    height: 42px;
  }

  .player-control :deep(.progress-bar) {
    display: none;
  }
}
</style>
