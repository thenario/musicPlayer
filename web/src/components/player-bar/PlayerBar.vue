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

const playerStore = usePlayerStore()

const currentSongCover = computed(() => getImageUrl(playerStore.currentSong?.song_cover_url))

useKeyboardShortcuts()
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
  padding: 0 32px;
  user-select: none;
  background: rgb(255 255 255 / 94%);
  border-top: 1px solid #e5e7eb;
  box-shadow: 0 -6px 20px rgb(15 23 42 / 6%);
  backdrop-filter: blur(14px);
}

@media (max-width: 768px) {
  .player-bar {
    height: 76px;
    padding: 0 16px;
  }
}
</style>
