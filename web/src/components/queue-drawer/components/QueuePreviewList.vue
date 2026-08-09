<template>
  <div>
    <div class="queue-preview-list__header">
      <h3 class="queue-preview-list__title">{{ queue?.queue_name || '队列详情' }}</h3>
      <span class="queue-preview-list__count">{{ queue?.queue_items?.length }} 首歌</span>
    </div>
    <table class="queue-preview-list__table">
      <tbody>
        <tr v-for="(item, index) in queue?.queue_items" :key="item.queue_item_id"
          class="queue-preview-list__row" @click="emit('play', index)">
          <td class="queue-preview-list__icon-col">
            <el-icon class="queue-preview-list__play-icon">
              <VideoPlay />
            </el-icon>
          </td>
          <td class="queue-preview-list__info-col">
            <div class="queue-preview-list__song">{{ item.song?.song_title }}</div>
            <div class="queue-preview-list__artist">{{ item.song?.artist }}</div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'QueuePreviewList' })
import { VideoPlay } from '@element-plus/icons-vue'
import type { IQueue } from '@/types'

defineProps<{
  queue: IQueue | null
}>()

const emit = defineEmits<{
  (e: 'play', index: number): void
}>()
</script>

<style scoped>
@reference "../../../assets/index.css";

.queue-preview-list__header {
  @apply px-2 py-4 border-b border-white/5 mb-2 flex justify-between items-center;
}

.queue-preview-list__title {
  @apply text-blue-400 font-bold;
}

.queue-preview-list__count {
  @apply text-[10px] text-gray-500;
}

.queue-preview-list__table {
  @apply w-full border-separate border-spacing-y-1;
}

.queue-preview-list__row {
  @apply transition-all cursor-pointer;
}

.queue-preview-list__row:hover {
  @apply bg-white/5;
}

.queue-preview-list__icon-col {
  @apply w-10 text-center;
}

.queue-preview-list__play-icon {
  @apply text-gray-600;
}

.queue-preview-list__row:hover .queue-preview-list__play-icon {
  @apply text-blue-400;
}

.queue-preview-list__info-col {
  @apply px-2 py-3;
}

.queue-preview-list__song {
  @apply text-sm text-gray-200;
}

.queue-preview-list__row:hover .queue-preview-list__song {
  @apply text-white;
}

.queue-preview-list__artist {
  @apply text-[10px] text-gray-500;
}
</style>
