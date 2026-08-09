<template>
  <div class="song-info">
    <div class="song-info__cover-wrap" @click="emit('expand')">
      <div
        class="song-info__cover">
        <img v-if="cover" :src="cover" alt="封面" class="song-info__cover-img" />
        <div v-else class="song-info__cover-fallback">
          <svg class="song-info__cover-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3" />
          </svg>
        </div>
      </div>
      <div
        class="song-info__cover-overlay">
        <el-icon :size="20">
          <ArrowUpBold />
        </el-icon>
      </div>
    </div>

    <div class="song-info__text">
      <h4 class="song-info__title">{{ title }}</h4>
      <p class="song-info__artist">{{ artist }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SongInfo' })
import { ArrowUpBold } from '@element-plus/icons-vue'

defineProps<{
  cover: string
  title: string
  artist: string
}>()

const emit = defineEmits<{
  (e: 'expand'): void
}>()
</script>

<style scoped>
@reference "../../../assets/index.css";

.song-info {
  @apply w-[30%] flex items-center gap-4 min-w-0;
}

.song-info__cover-wrap {
  @apply relative cursor-pointer shrink-0;
}

.song-info__cover {
  @apply w-16 h-16 rounded-lg shadow-2xl overflow-hidden transition-transform bg-gray-800;
}

.song-info__cover-wrap:hover .song-info__cover {
  @apply scale-105;
}

.song-info__cover-img {
  @apply w-full h-full object-cover;
}

.song-info__cover-fallback {
  @apply w-full h-full flex items-center justify-center;
}

.song-info__cover-icon {
  @apply w-6 h-6 text-gray-600;
}

.song-info__cover-overlay {
  @apply absolute inset-0 bg-black/40 opacity-0 transition-opacity flex items-center justify-center rounded-lg text-white;
}

.song-info__cover-wrap:hover .song-info__cover-overlay {
  @apply opacity-100;
}

.song-info__text {
  @apply min-w-0 flex-1;
}

.song-info__title {
  @apply text-white text-base font-semibold truncate;
}

.song-info__artist {
  @apply text-gray-400 text-xs truncate mt-1;
}
</style>
