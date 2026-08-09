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
import PlayerControl from '@/components/player-control/index.vue'
import SongDetail from '@/components/song-detail/index.vue'
import { useKeyboardShortcuts } from './composables/useKeyboardShortcuts'

const playerStore = usePlayerStore()

const currentSongCover = computed(() => getImageUrl(playerStore.currentSong?.song_cover_url))

useKeyboardShortcuts()
</script>

<style scoped>
@reference "../../assets/index.css";

.player-bar {
  @apply w-full shrink-0 h-24 bg-gray-900/90 backdrop-blur-lg border-t border-white/5 px-6 flex items-center justify-between z-50 select-none;
}
</style>
