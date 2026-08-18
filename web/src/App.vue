<template>
  <div class="app-container">
    <router-view/>
      <audio ref="audioElement" preload="auto" />
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, onMounted } from 'vue'
import { usePlayerStore } from './stores/player'

const audioElement = ref<HTMLAudioElement | null>(null)
const playerStore = usePlayerStore()

onMounted(() => {
  if (audioElement.value) {
    playerStore.setAudioElement(audioElement.value)
  }
})

onBeforeUnmount(() => playerStore.disposeAudio())
</script>

<style scoped>
.app-container {
  height: 100vh;
}
</style>