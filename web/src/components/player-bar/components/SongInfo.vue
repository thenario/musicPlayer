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
.song-info {
  display: flex;
  gap: 16px;
  align-items: center;
  width: 30%;
  min-width: 0;
}

.song-info__cover-wrap {
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
}

.song-info__cover {
  width: 64px;
  height: 64px;
  overflow: hidden;
  background: #eef2f6;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgb(15 23 42 / 12%);
  transition: transform .2s ease;
}

.song-info__cover-wrap:hover .song-info__cover {
  transform: scale(1.05);
}

.song-info__cover-img {
  width: 100%; height: 100%; object-fit: cover;
}

.song-info__cover-fallback {
  display: flex; width: 100%; height: 100%; align-items: center; justify-content: center;
}

.song-info__cover-icon {
  width: 24px; height: 24px; color: #909399;
}

.song-info__cover-overlay {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  color: #fff; opacity: 0; background: rgb(0 0 0 / 40%); border-radius: 8px; transition: opacity .2s ease;
}

.song-info__cover-wrap:hover .song-info__cover-overlay {
  opacity: 1;
}

.song-info__text {
  flex: 1; min-width: 0;
}

.song-info__title {
  margin: 0;
  overflow: hidden; color: #303133; font-size: 15px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap;
}

.song-info__artist {
  margin: 4px 0 0; overflow: hidden; color: #909399; font-size: 12px; text-overflow: ellipsis; white-space: nowrap;
}

@media (max-width: 768px) {
  .song-info {
    width: auto;
    flex: 1;
  }

  .song-info__cover {
    width: 48px;
    height: 48px;
  }
}
</style>
