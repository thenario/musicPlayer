<template>
  <Teleport to="body">
    <el-drawer v-model="isQueueVisible" direction="rtl" size="450px" :with-header="false" destroy-on-close
      class="queue-drawer-container" :z-index="4000">
      <div class="flex flex-col h-full bg-gray-950 text-gray-100 font-sans">

        <header class="h-16 flex items-center justify-between px-4 border-b border-white/5 bg-white/5 shrink-0">
          <el-segmented v-model="activeTab" :options="QUEUE_TABS" @change="handleTabChange"
            class="custom-segmented" />

          <div class="flex items-center gap-2">
            <transition name="el-fade-in" mode="out-in">
              <div v-if="activeTab === 'queue'" class="flex items-center gap-3">
                <span class="text-[10px] text-gray-500 tracking-tighter">{{ currentQueue.length }} 首</span>
                <el-button link type="danger" :icon="Delete" @click="confirmClear" size="small"
                  :disabled="currentQueue.length === 0">清空</el-button>
              </div>
              <div v-else-if="previewQueueId !== -1" class="flex items-center">
                <el-button link :icon="ArrowLeft" @click="backToQueueList" size="small">返回列表</el-button>
              </div>
            </transition>
          </div>
        </header>

        <main class="flex-1 overflow-y-auto custom-scrollbar">
          <div v-show="activeTab === 'queue'" class="p-2">
            <el-empty v-if="currentQueue.length === 0" description="队列空空如也" :image-size="80" />

            <table v-else class="w-full border-separate border-spacing-y-1">
              <VueDraggable v-model="dragQueue" target="tbody" handle=".drag-handle" :animation="200"
                ghost-class="drag-ghost">
                <tbody class="divide-y divide-transparent">
                  <QueueItem v-for="(item, index) in currentQueue" :key="item.queue_item_id"
                    :item="item"
                    :is-active="item.song?.song_id === currentSong?.song_id"
                    :is-playing="isPlaying"
                    @play="playFromQueue(index)"
                    @remove="removeFromQueue(item.queue_item_id)"
                  />
                </tbody>
              </VueDraggable>
            </table>
          </div>

          <div v-show="activeTab === 'lists'" class="p-2">
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
import { useQueueDrawer } from './composables/useQueueDrawer'
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
