<template>
  <tr :id="`song-${item.song?.song_id}`" class="queue-item"
    :class="{ 'queue-item--active active-row': isActive }" @dblclick="emit('play')">
    <td class="queue-item__icon-col">
      <div class="queue-item__icon-wrap">
        <!-- 拖拽手柄 -->
        <el-icon
          class="drag-handle queue-item__handle">
          <Rank />
        </el-icon>
        <!-- 播放状态动画 -->
        <div v-if="isActive" class="queue-item__status">
          <span v-if="isPlaying" class="playing-bar-animation"></span>
          <el-icon v-else>
            <VideoPause />
          </el-icon>
        </div>
      </div>
    </td>

    <td class="queue-item__title-col">
      <div class="queue-item__meta">
        <span class="queue-item__title"
          :class="isActive ? 'queue-item__title--active' : 'queue-item__title--inactive'">
          {{ item.song?.song_title }}
        </span>
        <span class="queue-item__artist">{{ item.song?.artist }}</span>
      </div>
    </td>

    <td class="queue-item__action-col">
      <el-button link type="info" :icon="Close" class="queue-item__remove"
        @click="emit('remove')" />
    </td>
  </tr>
</template>

<script setup lang="ts">
defineOptions({ name: 'QueueItem' })
import { Rank, Close, VideoPause } from '@element-plus/icons-vue'
import type { IQueueItem } from '@/types'

defineProps<{
  item: IQueueItem
  isActive: boolean
  isPlaying: boolean
}>()

const emit = defineEmits<{
  (e: 'play'): void
  (e: 'remove'): void
}>()
</script>

<style scoped>
@reference "../../../assets/index.css";

.queue-item {
  @apply transition-all cursor-default;
}

.queue-item:hover {
  @apply bg-white/5;
}

.queue-item--active {
  @apply bg-blue-600/10;
}

.queue-item__icon-col {
  @apply w-12 py-3 text-center;
}

.queue-item__icon-wrap {
  @apply relative flex justify-center items-center h-5;
}

.queue-item__handle {
  @apply opacity-0 cursor-grab text-gray-500 transition-opacity;
}

.queue-item:hover .queue-item__handle {
  @apply opacity-100;
}

.queue-item__handle:hover {
  @apply text-white;
}

.queue-item__status {
  @apply absolute text-blue-500;
}

.queue-item__title-col {
  @apply px-2;
}

.queue-item__meta {
  @apply flex flex-col truncate max-w-60;
}

.queue-item__title {
  @apply text-sm truncate font-medium;
}

.queue-item__title--inactive {
  @apply text-gray-200;
}

.queue-item__title--active {
  @apply text-blue-400;
}

.queue-item__artist {
  @apply text-[10px] text-gray-500 truncate;
}

.queue-item__action-col {
  @apply pr-4 text-right;
}

.queue-item__remove {
  @apply opacity-0 transition-opacity;
}

.queue-item:hover .queue-item__remove {
  @apply opacity-100;
}

.playing-bar-animation {
  display: inline-block;
  width: 12px;
  height: 12px;
  background: #3b82f6;
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Crect width='3' height='15' x='1' y='4.5'%3E%3Canimate attributeName='y' values='4.5;1;4.5' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='15;22;15' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3Crect width='3' height='15' x='10.5' y='4.5'%3E%3Canimate attributeName='y' values='1;4.5;1' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='22;15;22' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3Crect width='3' height='15' x='20' y='4.5'%3E%3Canimate attributeName='y' values='4.5;1;4.5' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='15;22;15' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3C/svg%3E");
}
</style>
