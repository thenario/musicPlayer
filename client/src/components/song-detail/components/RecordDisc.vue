<template>
  <div class="record-box">
    <div class="record-vinyl" :class="{ 'is-playing': isPlaying }">
      <img :src="cover || ''" class="record-img" alt="歌曲封面" />
      <div class="record-center"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'RecordDisc' })

defineProps<{
  cover: string
  isPlaying: boolean
}>()
</script>

<style scoped>
.record-box {
  width: 380px;
  height: 380px;
  position: relative;
}

.record-vinyl {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #111;
  background: radial-gradient(circle, #333 0%, #111 30%, #000 100%);
  padding: 15px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.8);
  animation: rotate-record 25s linear infinite;
  animation-play-state: paused;
  will-change: transform;
}

.record-vinyl.is-playing {
  animation-play-state: running;
}

.record-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.record-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  background: #111;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.1);
}

@keyframes rotate-record {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}
</style>
