<template>
  <el-skeleton :loading="loading" animated :rows="5">
    <template #default>
      <div v-for="q in queues" :key="q.queue_id"
        class="flex items-center justify-between p-3 mb-2 rounded-lg bg-white/5 hover:bg-white/10 transition-all cursor-pointer group"
        @click="emit('switch-queue', q.queue_id)">
        <div class="flex items-center gap-3 overflow-hidden">
          <div class="w-10 h-10 bg-gray-800 rounded flex items-center justify-center shrink-0">
            <el-icon v-if="currentQueueId === q.queue_id" class="text-blue-500">
              <Headset />
            </el-icon>
            <el-icon v-else class="text-gray-500">
              <List />
            </el-icon>
          </div>
          <div class="flex flex-col truncate">
            <span class="text-sm font-medium truncate"
              :class="{ 'text-blue-400': currentQueueId === q.queue_id }">
              {{ q.queue_name }}
            </span>
            <span class="text-[10px] text-gray-500">{{ q.song_count }} 首歌曲</span>
          </div>
        </div>

        <div class="flex items-center gap-2">
          <el-button link type="primary" size="small"
            @click.stop="emit('preview-queue', q.queue_id)">查看</el-button>
          <el-button v-if="currentQueueId !== q.queue_id" link type="danger" :icon="Delete"
            class="opacity-40 group-hover:opacity-100 transition-opacity"
            @click.stop="emit('delete-queue', q.queue_id)" />
        </div>
      </div>
      <el-empty v-if="queues.length === 0" description="没有其他队列" />
    </template>
  </el-skeleton>
</template>

<script setup lang="ts">
defineOptions({ name: 'QueueList' })
import { Headset, List, Delete } from '@element-plus/icons-vue'
import type { IQueue } from '@/types'

defineProps<{
  queues: IQueue[]
  currentQueueId: number | null
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'switch-queue', queueId: number): void
  (e: 'preview-queue', queueId: number): void
  (e: 'delete-queue', queueId: number): void
}>()
</script>
