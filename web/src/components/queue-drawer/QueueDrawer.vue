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
import { computed, onMounted } from 'vue'
import { reportError } from '@/utils/feedback'
import { logBackgroundFailure } from '@/utils/action-result'

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
  updateQueueOrder,
  loadQueues,
  handleTabChange,
  handlePreviewQueue,
  playFromPreview: playPreviewAction,
  backToQueueList,
  playFromQueue: playQueueAction,
  removeFromQueue: removeAction,
  clearCurrentQueue,
  handleSwitchQueue: switchQueueAction,
  deleteQueue,
} = useQueueDrawer()

const dragQueue = computed({
  get: () => currentQueue.value,
  set: (items) => { void updateQueueOrder(items).then((result) => {
    if (!result.success) reportError(result)
  }) },
})
const playFromPreview = async (index: number) => {
  const result = await playPreviewAction(index)
  if (!result.success) reportError(result)
}
const playFromQueue = async (index: number) => {
  const result = await playQueueAction(index)
  if (!result.success) reportError(result)
}
const removeFromQueue = async (itemId: number | string) => {
  const result = await removeAction(itemId)
  if (!result.success) reportError(result)
}
const handleSwitchQueue = async (queueId: number | string) => {
  const result = await switchQueueAction(queueId)
  if (!result.success) reportError(result)
}
const confirmClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前播放队列吗？', '提示', {
      confirmButtonText: '清空', cancelButtonText: '取消', type: 'warning',
    })
  } catch (error) {
    reportError(error)
    return
  }
  const result = await clearCurrentQueue()
  if (!result.success) reportError(result)
}
const confirmDeleteQueue = async (queueId: number | string) => {
  try {
    await ElMessageBox.confirm('确定要永久删除这个播放队列吗？', '警告', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning',
    })
  } catch (error) {
    reportError(error)
    return
  }
  const result = await deleteQueue(queueId)
  if (!result.success) reportError(result)
}
onMounted(() => { void loadQueues().then(logBackgroundFailure) })
</script>

<style scoped>
.queue-drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
  color: #20232d;
  font-family: inherit;
  background: #f7f8fa;
}

.queue-drawer__header {
  display: flex;
  flex-shrink: 0;
  min-height: 72px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 1px solid #e8eaef;
}

.queue-drawer__header-actions {
  display: flex; gap: 8px; align-items: center;
}

.queue-drawer__header-tools {
  display: flex; gap: 12px; align-items: center;
}

.queue-drawer__header-back {
  display: flex; align-items: center;
}

.queue-drawer__count {
  color: #8b91a0;
  font-size: 12px;
  letter-spacing: -.01em;
}

.queue-drawer__body {
  flex: 1; overflow-y: auto;
}

.queue-drawer__content {
  padding: 16px;
}

.queue-drawer__table {
  width: 100%; border-spacing: 0 4px; border-collapse: separate;
}

.queue-drawer__tbody {
  border-color: transparent;
}

:deep(.queue-drawer-container .el-drawer__body) {
  padding: 0;
  background: #f7f8fa;
}

.drag-ghost {
  opacity: 0.5;
  background: #e9e7ff !important;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cfd3dc;
  border-radius: 10px;
}

.custom-segmented {
  --el-segmented-bg-color: #f1f3f7;
  --el-segmented-item-selected-bg-color: #6256c5;
  --el-segmented-item-selected-color: #fff;
  --el-segmented-item-color: #717784;
}

:deep(.queue-drawer-container .el-drawer__header) {
  margin-bottom: 0;
}

:deep(.queue-drawer-container .el-empty__description p) {
  color: #8b91a0;
}
</style>
