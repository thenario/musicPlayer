<template>
  <tr :id="`song-${item.song?.song_id}`" class="group transition-all hover:bg-white/5 cursor-default"
    :class="{ 'bg-blue-600/10 active-row': isActive }" @dblclick="emit('play')">
    <td class="w-12 py-3 text-center">
      <div class="relative flex justify-center items-center h-5">
        <!-- 拖拽手柄 -->
        <el-icon
          class="drag-handle opacity-0 group-hover:opacity-100 cursor-grab text-gray-500 hover:text-white transition-opacity">
          <Rank />
        </el-icon>
        <!-- 播放状态动画 -->
        <div v-if="isActive" class="absolute text-blue-500">
          <span v-if="isPlaying" class="playing-bar-animation"></span>
          <el-icon v-else>
            <VideoPause />
          </el-icon>
        </div>
      </div>
    </td>

    <td class="px-2">
      <div class="flex flex-col truncate max-w-60">
        <span class="text-sm truncate font-medium"
          :class="isActive ? 'text-blue-400' : 'text-gray-200'">
          {{ item.song?.song_title }}
        </span>
        <span class="text-[10px] text-gray-500 truncate">{{ item.song?.artist }}</span>
      </div>
    </td>

    <td class="pr-4 text-right">
      <el-button link type="info" :icon="Close" class="opacity-0 group-hover:opacity-100 transition-opacity"
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
.playing-bar-animation {
  display: inline-block;
  width: 12px;
  height: 12px;
  background: #3b82f6;
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Crect width='3' height='15' x='1' y='4.5'%3E%3Canimate attributeName='y' values='4.5;1;4.5' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='15;22;15' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3Crect width='3' height='15' x='10.5' y='4.5'%3E%3Canimate attributeName='y' values='1;4.5;1' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='22;15;22' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3Crect width='3' height='15' x='20' y='4.5'%3E%3Canimate attributeName='y' values='4.5;1;4.5' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='15;22;15' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3C/svg%3E");
}
</style>
