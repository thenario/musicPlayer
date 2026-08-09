<template>
  <div class="song-item-card">
    <div class="song-item-card__body">
      <!-- 歌曲封面 -->
      <el-image :src="getImageUrl(song.song_cover_url)" class="song-item-card__cover" fit="cover">
        <template #error>
          <div class="song-item-card__cover-fallback">
            <el-icon :size="24">
              <Mic />
            </el-icon>
          </div>
        </template>
      </el-image>

      <!-- 歌曲详情 -->
      <div class="song-item-card__info">
        <h3 class="song-item-card__title">
          {{ song.song_title }}
        </h3>
        <p class="song-item-card__artist">{{ song.artist || '未知艺术家' }}</p>
        <div class="song-item-card__date">
          <el-icon class="song-item-card__date-icon">
            <Calendar />
          </el-icon>
          {{ formatDate(song.date_added) }}
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="song-item-card__actions">
        <el-tooltip content="编辑详情" placement="top">
          <el-button circle @click="emit('edit', song)"
            class="song-item-card__edit">
            <el-icon>
              <EditPen />
            </el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Mic, Calendar, EditPen } from '@element-plus/icons-vue'
import { getImageUrl, formatDate } from '@/utils/format'

defineProps<{ song: any }>()
const emit = defineEmits<{
  (e: 'edit', song: any): void
}>()
</script>

<style scoped>
@reference "../../../../assets/index.css";

.song-item-card__body {
  @apply flex items-center p-4;
}

.song-item-card__cover {
  @apply w-16 h-16 rounded-lg shadow-sm shrink-0;
}

.song-item-card__cover-fallback {
  @apply w-full h-full bg-gray-100 flex items-center justify-center text-gray-400;
}

.song-item-card__info {
  @apply ml-4 grow overflow-hidden;
}

.song-item-card__title {
  @apply text-lg font-semibold text-gray-800 truncate transition-colors;
}

.song-item-card:hover .song-item-card__title {
  @apply text-indigo-600;
}

.song-item-card__artist {
  @apply text-sm text-gray-500 truncate;
}

.song-item-card__date {
  @apply flex items-center mt-1 text-xs text-gray-400;
}

.song-item-card__date-icon {
  @apply mr-1;
}

.song-item-card__actions {
  @apply flex gap-2 ml-4;
}

.song-item-card__edit {
  @apply border-none;
}

.song-item-card__edit:hover {
  @apply bg-indigo-50 text-indigo-600;
}

.song-item-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #f3f4f6;
  transition: all 0.3s ease;
}

.song-item-card:hover {
  transform: translateX(4px);
  box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.05);
  border-color: #e5e7eb;
}
</style>
