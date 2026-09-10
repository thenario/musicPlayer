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
.queue-preview-list__header {
  display: flex; align-items: center; justify-content: space-between; padding: 8px 4px 16px; margin-bottom: 8px; border-bottom: 1px solid #e8eaef;
}

.queue-preview-list__title {
  margin: 0; color: #20232d; font-size: 18px; font-weight: 700;
}

.queue-preview-list__count {
  color: #8b91a0; font-size: 12px;
}

.queue-preview-list__table {
  width: 100%; border-spacing: 0 4px; border-collapse: separate;
}

.queue-preview-list__row {
  cursor: pointer; transition: background-color .2s ease;
}

.queue-preview-list__row:hover {
  background: #f4f3ff;
}

.queue-preview-list__icon-col {
  width: 40px; text-align: center;
}

.queue-preview-list__play-icon {
  color: #969cab;
}

.queue-preview-list__row:hover .queue-preview-list__play-icon {
  color: #6256c5;
}

.queue-preview-list__info-col {
  padding: 12px 8px;
}

.queue-preview-list__song {
  color: #353a46; font-size: 14px; font-weight: 600;
}

.queue-preview-list__row:hover .queue-preview-list__song {
  color: #6256c5;
}

.queue-preview-list__artist {
  color: #969cab; font-size: 11px;
}
</style>
