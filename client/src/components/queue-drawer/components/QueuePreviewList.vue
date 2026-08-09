<template>
  <div>
    <div class="px-2 py-4 border-b border-white/5 mb-2 flex justify-between items-center">
      <h3 class="text-blue-400 font-bold">{{ queue?.queue_name || '队列详情' }}</h3>
      <span class="text-[10px] text-gray-500">{{ queue?.queue_items?.length }} 首歌</span>
    </div>
    <table class="w-full border-separate border-spacing-y-1">
      <tbody>
        <tr v-for="(item, index) in queue?.queue_items" :key="item.queue_item_id"
          class="group hover:bg-white/5 transition-all cursor-pointer" @click="emit('play', index)">
          <td class="w-10 text-center">
            <el-icon class="text-gray-600 group-hover:text-blue-400">
              <VideoPlay />
            </el-icon>
          </td>
          <td class="px-2 py-3">
            <div class="text-sm text-gray-200 group-hover:text-white">{{ item.song?.song_title }}</div>
            <div class="text-[10px] text-gray-500">{{ item.song?.artist }}</div>
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
