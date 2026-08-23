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
  display: flex; align-items: center; justify-content: space-between; padding: 16px 8px; margin-bottom: 8px; border-bottom: 1px solid rgb(255 255 255 / 8%);
}

.queue-preview-list__title {
  color: #409eff; font-weight: 700;
}

.queue-preview-list__count {
  color: #6b7280; font-size: 10px;
}

.queue-preview-list__table {
  width: 100%; border-spacing: 0 4px; border-collapse: separate;
}

.queue-preview-list__row {
  cursor: pointer; transition: background-color .2s ease;
}

.queue-preview-list__row:hover {
  background: rgb(255 255 255 / 5%);
}

.queue-preview-list__icon-col {
  width: 40px; text-align: center;
}

.queue-preview-list__play-icon {
  color: #4b5563;
}

.queue-preview-list__row:hover .queue-preview-list__play-icon {
  color: #409eff;
}

.queue-preview-list__info-col {
  padding: 12px 8px;
}

.queue-preview-list__song {
  color: #e5e7eb; font-size: 14px;
}

.queue-preview-list__row:hover .queue-preview-list__song {
  color: #fff;
}

.queue-preview-list__artist {
  color: #6b7280; font-size: 10px;
}
</style>
