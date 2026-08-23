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
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: radial-gradient(circle, #64748b 0%, #334155 28%, #1e293b 74%, #0f172a 100%);
  padding: 13px;
  box-shadow: 0 14px 32px rgb(15 23 42 / 20%);
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
  background: #334155;
  border-radius: 50%;
  border: 2px solid rgb(255 255 255 / 30%);
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
