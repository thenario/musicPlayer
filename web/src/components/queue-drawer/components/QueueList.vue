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
.queue-list__item {
  display: flex; align-items: center; justify-content: space-between; padding: 12px; margin-bottom: 8px; cursor: pointer; background: rgb(255 255 255 / 5%); border-radius: 8px; transition: background-color .2s ease;
}

.queue-list__item:hover {
  background: rgb(255 255 255 / 10%);
}

.queue-list__info {
  display: flex; gap: 12px; align-items: center; overflow: hidden;
}

.queue-list__avatar {
  display: flex; flex-shrink: 0; width: 40px; height: 40px; align-items: center; justify-content: center; background: #1f2937; border-radius: 4px;
}

.queue-list__avatar-icon {
  color: #6b7280;
}

.queue-list__avatar-icon--active {
  color: #409eff;
}

.queue-list__meta {
  display: flex; flex-direction: column; overflow: hidden;
}

.queue-list__name {
  overflow: hidden; font-size: 14px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap;
}

.queue-list__name.is-active {
  color: #409eff;
}

.queue-list__count {
  color: #6b7280; font-size: 10px;
}

.queue-list__actions {
  display: flex; gap: 8px; align-items: center;
}

.queue-list__delete {
  opacity: .4; transition: opacity .2s ease;
}

.queue-list__item:hover .queue-list__delete {
  opacity: 1;
}
</style>
