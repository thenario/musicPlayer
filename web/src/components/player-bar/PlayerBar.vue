<template>
  <div v-if="playerStore.currentSong"
    class="player-bar">

    <SongInfo
      :cover="currentSongCover"
      :title="playerStore.currentSong.song_title"
      :artist="playerStore.currentSong.artist"
      @expand="playerStore.toggleSongDetail"
    />

    <PlayerControl z-index="2" />

    <VolumeControl />

    <SongDetail />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'PlayerBar' })
import { computed } from 'vue'
import { usePlayerStore } from '@/stores/player'
import { getImageUrl } from '@/utils/format'
import SongInfo from './components/SongInfo.vue'
import VolumeControl from './components/VolumeControl.vue'
import PlayerControl from '@/components/player-control/PlayerControl.vue'
import SongDetail from '@/components/song-detail/SongDetail.vue'
import { useKeyboardShortcuts } from './composables/use-keyboard-shortcuts'
import { reportError } from '@/utils/feedback'

const playerStore = usePlayerStore()

const currentSongCover = computed(() => getImageUrl(playerStore.currentSong?.song_cover_url))

useKeyboardShortcuts(reportError)
</script>

<style scoped>
.player-bar {
  box-sizing: border-box;
  z-index: 50;
  display: flex;
  flex-shrink: 0;
  width: 100%;
  height: 88px;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  user-select: none;
  background: rgb(255 255 255 / 94%);
  border-top: 1px solid var(--music-border);
  box-shadow: 0 -8px 32px rgb(47 38 80 / 5%);
  backdrop-filter: blur(14px);
}

@media (max-width: 768px) {
  .player-bar {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 10px 12px;
    height: auto;
    min-height: 116px;
    padding: 12px 16px;
  }

  .player-bar :deep(.player-control) {
    grid-column: 1 / -1;
    grid-row: 2;
    width: 100%;
  }

  .player-bar :deep(.volume-control) {
    grid-column: 2;
    grid-row: 1;
  }

  .player-bar :deep(.song-info) {
    width: 100%;
  }
}
</style>
