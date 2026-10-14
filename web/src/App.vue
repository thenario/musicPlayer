<template>
  <div class="app-container">
    <router-view/>
      <audio ref="audioElement" preload="auto" />
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, onMounted } from 'vue'
import { usePlayerStore } from './stores/player'
import { useUserStore } from './stores/user'
import { tokenStorage } from '@/utils/storage'
import { logBackgroundFailure } from '@/utils/action-result'
const audioElement = ref<HTMLAudioElement | null>(null)
const playerStore = usePlayerStore()
const userStore = useUserStore()

onMounted(() => {
  if (audioElement.value) {
    playerStore.setAudioElement(audioElement.value)
  }
  if (tokenStorage.get()) void userStore.auth().then(logBackgroundFailure)
})

onBeforeUnmount(() => playerStore.disposeAudio())
</script>

<style scoped>
.app-container {
  height: 100vh;
}
</style>
