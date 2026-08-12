<template>
  <el-skeleton :loading="loading" animated :rows="5">
    <template #default>
      <div v-for="q in queues" :key="q.queue_id"
        class="queue-list__item"
        @click="emit('switch-queue', q.queue_id)">
        <div class="queue-list__info">
          <div class="queue-list__avatar">
            <el-icon v-if="sameId(currentQueueId, q.queue_id)" class="queue-list__avatar-icon queue-list__avatar-icon--active">
              <Headset />
            </el-icon>
            <el-icon v-else class="queue-list__avatar-icon">
              <List />
            </el-icon>
          </div>
          <div class="queue-list__meta">
            <span class="queue-list__name"
              :class="{ 'is-active': sameId(currentQueueId, q.queue_id) }">
              {{ q.queue_name }}
            </span>
            <span class="queue-list__count">{{ q.song_count }} 首歌曲</span>
          </div>
        </div>

        <div class="queue-list__actions">
          <el-button link type="primary" size="small"
            @click.stop="emit('preview-queue', q.queue_id)">查看</el-button>
          <el-button v-if="!sameId(currentQueueId, q.queue_id)" link type="danger" :icon="Delete"
            class="queue-list__delete"
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
import { sameId } from '@/utils/format'

defineProps<{
  queues: IQueue[]
  currentQueueId: number | string | null
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'switch-queue', queueId: number | string): void
  (e: 'preview-queue', queueId: number | string): void
  (e: 'delete-queue', queueId: number | string): void
}>()
</script>

<style scoped>
@reference "../../../assets/index.css";

.queue-list__item {
  @apply flex items-center justify-between p-3 mb-2 rounded-lg bg-white/5 transition-all cursor-pointer;
}

.queue-list__item:hover {
  @apply bg-white/10;
}

.queue-list__info {
  @apply flex items-center gap-3 overflow-hidden;
}

.queue-list__avatar {
  @apply w-10 h-10 bg-gray-800 rounded flex items-center justify-center shrink-0;
}

.queue-list__avatar-icon {
  @apply text-gray-500;
}

.queue-list__avatar-icon--active {
  @apply text-blue-500;
}

.queue-list__meta {
  @apply flex flex-col truncate;
}

.queue-list__name {
  @apply text-sm font-medium truncate;
}

.queue-list__name.is-active {
  @apply text-blue-400;
}

.queue-list__count {
  @apply text-[10px] text-gray-500;
}

.queue-list__actions {
  @apply flex items-center gap-2;
}

.queue-list__delete {
  @apply opacity-40 transition-opacity;
}

.queue-list__item:hover .queue-list__delete {
  @apply opacity-100;
}
</style>
