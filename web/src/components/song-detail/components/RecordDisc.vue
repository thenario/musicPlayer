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
  width: min(360px, 32vw);
  aspect-ratio: 1;
  height: auto;
  position: relative;
}

.record-vinyl {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: repeating-radial-gradient(circle, #151412 0 2px, #343029 3px, #1d1b18 4px 5px);
  padding: 36px;
  border: 1px solid #494034;
  box-shadow: 0 24px 56px rgb(0 0 0 / 38%);
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
  background: #291f17;
  border-radius: 50%;
  border: 2px solid var(--music-primary);
}

@media (prefers-reduced-motion: reduce) {
  .record-vinyl { animation: none; }
}

@media (max-width: 768px) {
  .record-box {
    width: min(260px, 64vw);
  }
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
