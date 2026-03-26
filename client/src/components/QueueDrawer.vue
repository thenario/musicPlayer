<template>
  <el-drawer v-model="isQueueVisible" direction="rtl" size="450px" :with-header="false" destroy-on-close
    class="queue-drawer-container">
    <div class="flex flex-col h-full bg-gray-950 text-gray-100 font-sans">

      <header class="h-16 flex items-center justify-between px-4 border-b border-white/5 bg-white/5 shrink-0">
        <el-segmented v-model="activeTab" :options="[
          { label: '当前播放', value: 'queue' },
          { label: '队列列表', value: 'lists' }
        ]" @change="handleTabChange" class="custom-segmented" />

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
                <tr v-for="(item, index) in currentQueue" :key="item.queue_item_id" :id="`song-${item.song?.song_id}`"
                  class="group transition-all hover:bg-white/5 cursor-default"
                  :class="{ 'bg-blue-600/10 active-row': item.song?.song_id === currentSong?.song_id }"
                  @dblclick="playFromQueue(index)">

                  <td class="w-12 py-3 text-center">
                    <div class="relative flex justify-center items-center h-5">
                      <!-- 拖拽手柄 -->
                      <el-icon
                        class="drag-handle opacity-0 group-hover:opacity-100 cursor-grab text-gray-500 hover:text-white transition-opacity">
                        <Rank />
                      </el-icon>
                      <!-- 播放状态动画 -->
                      <div v-if="item.song?.song_id === currentSong?.song_id" class="absolute text-blue-500">
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
                        :class="item.song?.song_id === currentSong?.song_id ? 'text-blue-400' : 'text-gray-200'">
                        {{ item.song?.song_title }}
                      </span>
                      <span class="text-[10px] text-gray-500 truncate">{{ item.song?.artist }}</span>
                    </div>
                  </td>

                  <td class="pr-4 text-right">
                    <el-button link type="info" :icon="Close"
                      class="opacity-0 group-hover:opacity-100 transition-opacity"
                      @click="removeFromQueue(item.queue_item_id)" />
                  </td>
                </tr>
              </tbody>
            </VueDraggable>
          </table>
        </div>

        <div v-show="activeTab === 'lists'" class="p-2">
          <div v-if="previewQueueId !== -1">
            <div class="px-2 py-4 border-b border-white/5 mb-2 flex justify-between items-center">
              <h3 class="text-blue-400 font-bold">{{ previewData?.queue_name || '队列详情' }}</h3>
              <span class="text-[10px] text-gray-500">{{ previewData?.queue_items?.length }} 首歌</span>
            </div>
            <table class="w-full border-separate border-spacing-y-1">
              <tbody>
                <tr v-for="(item, index) in previewData?.queue_items" :key="item.queue_item_id"
                  class="group hover:bg-white/5 transition-all cursor-pointer" @click="playFromPreview(index)">
                  <!-- 这里的 click 触发“从预览列表播放并切换” -->
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

          <div v-else>
            <el-skeleton :loading="previewLoading" animated :rows="5">
              <template #default>
                <div v-for="q in userQueues" :key="q.queue_id"
                  class="flex items-center justify-between p-3 mb-2 rounded-lg bg-white/5 hover:bg-white/10 transition-all cursor-pointer group"
                  @click="handleSwitchQueue(q.queue_id)">
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
                      @click.stop="handlePreviewQueue(q.queue_id)">查看</el-button>
                    <el-button v-if="currentQueueId !== q.queue_id" link type="danger" :icon="Delete"
                      class="opacity-40 group-hover:opacity-100 transition-opacity"
                      @click.stop="confirmDeleteQueue(q.queue_id)" />
                  </div>
                </div>
                <el-empty v-if="userQueues.length === 0" description="没有其他队列" />
              </template>
            </el-skeleton>
          </div>
        </div>
      </main>
    </div>
  </el-drawer>
</template>


<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { usePlayerStore } from '../stores/player'
import { VueDraggable } from 'vue-draggable-plus'
import { Delete, Rank, Close, VideoPause, ArrowLeft, Headset, List, VideoPlay } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { IQueue } from '../../type'

const playerStore = usePlayerStore()
const { currentQueue, currentSong, isPlaying, isQueueVisible, currentQueueId, userQueues } = storeToRefs(playerStore)

const activeTab = ref('queue')
const previewQueueId = ref<number>(-1)
const previewData = ref<IQueue | null>(null)
const previewLoading = ref(false)

const handleTabChange = (tab: string) => {
  activeTab.value = tab
  if (tab === 'queue') {
    previewQueueId.value = -1
    previewData.value = null
  }
}

// 触发位置：列表页面的“查看”按钮
const handlePreviewQueue = (queueId: number) => {
  previewQueueId.value = queueId

  const found = userQueues.value.find(q => q.queue_id === queueId)
  if (found) {
    previewData.value = found
  }
}

const playFromPreview = async (index: number) => {
  if (previewQueueId.value === -1) return
  await playerStore.playSongInQueue(previewQueueId.value, index)
  activeTab.value = 'queue'
  previewQueueId.value = -1
}

const backToQueueList = () => {
  previewQueueId.value = -1
  previewData.value = null
}

const dragQueue = computed({
  get: () => currentQueue.value,
  set: (newVal) => playerStore.updateQueueOrder(newVal)
})

const playFromQueue = (index: number) => playerStore.playAtIndex(index)

const removeFromQueue = (itemId: number | string) => playerStore.removeQueueItem(itemId)

const confirmClear = () => {
  ElMessageBox.confirm('确定要清空当前播放队列吗？', '提示', {
    confirmButtonText: '清空',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    if (currentQueueId.value) {
      await playerStore.clearQueue(currentQueueId.value)
    }
  }).catch(() => { })
}

// 触发位置：点击整个队列卡片
const handleSwitchQueue = async (queueId: number) => {
  if (queueId === currentQueueId.value) {
    activeTab.value = 'queue'
    return
  }
  await playerStore.switchQueue(queueId)
  activeTab.value = 'queue'
}

// 触发位置：队列列表右侧的“垃圾桶”图标
const confirmDeleteQueue = (queueId: number) => {
  ElMessageBox.confirm('确定要永久删除这个播放队列吗？', '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await playerStore.deleteQueue(queueId)
  }).catch(() => { })
}

const scrollToCurrent = async () => {
  if (!isQueueVisible.value || !currentSong.value?.song_id) return
  await nextTick()
  const targetId = `song-${currentSong.value.song_id}`
  const el = document.getElementById(targetId)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

watch([isQueueVisible, () => currentSong.value?.song_id], () => {
  if (isQueueVisible.value) setTimeout(scrollToCurrent, 250)
})

onMounted(() => {
  playerStore.fetchCurrentQueue()
  playerStore.fetchUserQueues()
})
</script>

<style scoped>
:deep(.queue-drawer-container .el-drawer__body) {
  padding: 0;
  background: transparent;
}

.playing-bar-animation {
  display: inline-block;
  width: 12px;
  height: 12px;
  background: #3b82f6;
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Crect width='3' height='15' x='1' y='4.5'%3E%3Canimate attributeName='y' values='4.5;1;4.5' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='15;22;15' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3Crect width='3' height='15' x='10.5' y='4.5'%3E%3Canimate attributeName='y' values='1;4.5;1' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='22;15;22' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3Crect width='3' height='15' x='20' y='4.5'%3E%3Canimate attributeName='y' values='4.5;1;4.5' dur='0.6s' repeatCount='indefinite'/%3E%3Canimate attributeName='height' values='15;22;15' dur='0.6s' repeatCount='indefinite'/%3E%3C/rect%3E%3C/svg%3E");
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