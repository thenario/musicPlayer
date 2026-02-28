<template>
  <div id="app" class="h-screen bg-gray-900 text-white w-full mx-auto flex flex-col overflow-hidden">
    <NavBar class="shrink-0" />
    <main class="flex-1 overflow-hidden relative flex flex-col">
      <router-view />
    </main>

    <PlayerBar />

    <SongDetail />

    <QueueDrawer />

    <audio ref="audioElement" preload="auto" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { usePlayerStore } from './stores/player'
import NavBar from './components/NavBar.vue'
import PlayerBar from './components/PlayerBar.vue'
import QueueDrawer from './components/QueueDrawer.vue'
import SongDetail from './views/SongDetail.vue'

const audioElement = ref(null)
const playerStore = usePlayerStore()

onMounted(() => {
  playerStore.setAudioElement(audioElement.value)
})
</script>

<style>
/* 全局样式 */
body {
  font-family: 'Inter', sans-serif;
}
</style>