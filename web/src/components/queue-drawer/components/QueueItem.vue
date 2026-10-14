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
.queue-item {
  cursor: default; transition: background-color .2s ease;
}

.queue-item:hover {
  background: var(--music-hover);
}

.queue-item--active {
  background: var(--music-primary-soft);
}

.queue-item__icon-col {
  width: 48px; padding: 12px 0; text-align: center;
  border-radius: 10px 0 0 10px;
}

.queue-item__icon-wrap {
  position: relative; display: flex; height: 20px; align-items: center; justify-content: center;
}

.queue-item__handle {
  color: var(--music-muted); cursor: grab; opacity: 0; transition: opacity .2s ease;
}

.queue-item:hover .queue-item__handle {
  opacity: 1;
}

.queue-item__handle:hover {
  color: var(--music-primary);
}

.queue-item__status {
  position: absolute; color: var(--music-primary);
}

.queue-item__title-col {
  padding: 0 8px;
}

.queue-item__meta {
  display: flex; flex-direction: column; max-width: 240px; overflow: hidden;
}

.queue-item__title {
  overflow: hidden; font-size: 14px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap;
}

.queue-item__title--inactive {
  color: var(--music-text);
}

.queue-item__title--active {
  color: var(--music-primary);
}

.queue-item__artist {
  overflow: hidden; color: var(--music-muted); font-size: 11px; text-overflow: ellipsis; white-space: nowrap;
}

.queue-item__action-col {
  padding-right: 16px; text-align: right;
  border-radius: 0 10px 10px 0;
}

.queue-item__remove {
  opacity: 0.45; transition: opacity .2s ease;
}

.queue-item:hover .queue-item__remove {
  opacity: 1;
}

@media (hover: none) {
  .queue-item__handle,
  .queue-item__remove {
    opacity: 1;
  }
}

.playing-bar-animation {
  display: inline-block;
  width: 12px;
  height: 12px;
  background: var(--music-primary);
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Crect width='3' height='15' x='1' y='4.5'%3E%3Canimate attributeName='y' values='4.5;1;4.5' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='15;22;15' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3Crect width='3' height='15' x='10.5' y='4.5'%3E%3Canimate attributeName='y' values='1;4.5;1' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='22;15;22' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3Crect width='3' height='15' x='20' y='4.5'%3E%3Canimate attributeName='y' values='4.5;1;4.5' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='15;22;15' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3C/svg%3E");
}

@media (prefers-reduced-motion: reduce) {
  .playing-bar-animation {
    mask: none;
    background: repeating-linear-gradient(to right, var(--music-primary) 0 3px, transparent 3px 5px);
  }
}
</style>
