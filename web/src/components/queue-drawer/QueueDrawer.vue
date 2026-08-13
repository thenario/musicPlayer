<template>
  <Teleport to="body">
    <el-drawer v-model="isQueueVisible" direction="rtl" size="450px" :with-header="false" destroy-on-close
      class="queue-drawer-container" :z-index="4000">
      <div class="queue-drawer">

        <header class="queue-drawer__header">
          <el-segmented v-model="activeTab" :options="QUEUE_TABS" @change="handleTabChange"
            class="custom-segmented" />

          <div class="queue-drawer__header-actions">
            <transition name="el-fade-in" mode="out-in">
              <div v-if="activeTab === 'queue'" class="queue-drawer__header-tools">
                <span class="queue-drawer__count">{{ currentQueue.length }} 首</span>
                <el-button link type="danger" :icon="Delete" @click="confirmClear" size="small"
                  :disabled="currentQueue.length === 0">清空</el-button>
              </div>
              <div v-else-if="previewQueueId !== -1" class="queue-drawer__header-back">
                <el-button link :icon="ArrowLeft" @click="backToQueueList" size="small">返回列表</el-button>
              </div>
            </transition>
          </div>
        </header>

        <main class="queue-drawer__body custom-scrollbar">
          <div v-show="activeTab === 'queue'" class="queue-drawer__content">
            <el-empty v-if="currentQueue.length === 0" description="队列空空如也" :image-size="80" />

            <table v-else class="queue-drawer__table">
              <VueDraggable v-model="dragQueue" target="tbody" handle=".drag-handle" :animation="200"
                ghost-class="drag-ghost">
                <tbody class="queue-drawer__tbody">
                  <QueueItem v-for="(item, index) in currentQueue" :key="item.queue_item_id"
                    :item="item"
                    :is-active="sameId(item.song?.song_id, currentSong?.song_id)"
                    :is-playing="isPlaying"
                    @play="playFromQueue(index)"
                    @remove="removeFromQueue(item.queue_item_id)"
                  />
                </tbody>
              </VueDraggable>
            </table>
          </div>

          <div v-show="activeTab === 'lists'" class="queue-drawer__content">
            <QueuePreviewList v-if="previewQueueId !== -1" :queue="previewData" @play="playFromPreview" />
            <QueueList v-else :queues="userQueues" :current-queue-id="currentQueueId" :loading="previewLoading"
              @switch-queue="handleSwitchQueue" @preview-queue="handlePreviewQueue"
              @delete-queue="confirmDeleteQueue" />
          </div>
        </main>
      </div>
    </el-drawer>
  </Teleport>
</template>

<script setup lang="ts">
defineOptions({ name: 'QueueDrawer' })
import { VueDraggable } from 'vue-draggable-plus'
import { Delete, ArrowLeft } from '@element-plus/icons-vue'
import QueueItem from './components/QueueItem.vue'
import QueueList from './components/QueueList.vue'
import QueuePreviewList from './components/QueuePreviewList.vue'
import { useQueueDrawer } from './composables/use-queue-drawer'
import { sameId } from '@/utils/format'
import { QUEUE_TABS } from './const'

const {
  currentQueue,
  currentSong,
  isPlaying,
  isQueueVisible,
  currentQueueId,
  userQueues,
  activeTab,
  previewQueueId,
  previewData,
  previewLoading,
  dragQueue,
  handleTabChange,
  handlePreviewQueue,
  playFromPreview,
  backToQueueList,
  playFromQueue,
  removeFromQueue,
  confirmClear,
  handleSwitchQueue,
  confirmDeleteQueue,
} = useQueueDrawer()
</script>

<style scoped>
@reference "../../assets/index.css";

.queue-drawer {
  @apply flex flex-col h-full bg-gray-950 text-gray-100 font-sans;
}

.queue-drawer__header {
  @apply h-16 flex items-center justify-between px-4 border-b border-white/5 bg-white/5 shrink-0;
}

.queue-drawer__header-actions {
  @apply flex items-center gap-2;
}

.queue-drawer__header-tools {
  @apply flex items-center gap-3;
}

.queue-drawer__header-back {
  @apply flex items-center;
}

.queue-drawer__count {
  @apply text-[10px] text-gray-500 tracking-tighter;
}

.queue-drawer__body {
  @apply flex-1 overflow-y-auto;
}

.queue-drawer__content {
  @apply p-2;
}

.queue-drawer__table {
  @apply w-full border-separate border-spacing-y-1;
}

.queue-drawer__tbody {
  @apply divide-y divide-transparent;
}

:deep(.queue-drawer-container .el-drawer__body) {
  padding: 0;
  background: transparent;
}

.drag-ghost {
  opacity: 0.5;
  background: #2563eb !important;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #333;
  border-radius: 10px;
}

.custom-segmented {
  --el-segmented-bg-color: rgba(255, 255, 255, 0.05);
  --el-segmented-item-selected-bg-color: #3b82f6;
  --el-segmented-item-selected-color: #fff;
}
</style>
