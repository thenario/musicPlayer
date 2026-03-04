<template>
  <el-drawer v-model="isQueueVisible" direction="rtl" size="450px" :with-header="false" destroy-on-close
    class="queue-drawer-container">
    <div class="flex flex-col h-full bg-gray-950 text-gray-100 font-sans">

      <header class="h-16 flex items-center justify-between px-4 border-b border-white/5 bg-white/5 shrink-0">
        <el-segmented v-model="activeTab" :options="[
          { label: '当前播放', value: 'queue' },
          { label: '播放历史', value: 'lists' }
        ]" @change="handleTabChange" class="custom-segmented" />

        <div class="flex items-center gap-2">
          <transition name="el-fade-in">
            <div v-if="activeTab === 'queue'" class="flex items-center gap-3">
              <span class="text-[10px] text-gray-500 tracking-tighter">{{ currentQueue.length }} 首</span>
              <el-button link type="danger" :icon="Delete" @click="confirmClear" size="small">清空</el-button>
            </div>
            <el-button v-else-if="previewQueueId" link :icon="ArrowLeft" @click="backToQueueList"
              size="small">返回</el-button>
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
                      <el-icon
                        class="drag-handle opacity-0 group-hover:opacity-100 cursor-grab text-gray-500 hover:text-white transition-opacity">
                        <Rank />
                      </el-icon>
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
                    <span class="text-[11px] font-mono text-gray-600 group-hover:hidden">{{
                      formatDuration(item.song?.duration) }}</span>
                    <el-button link type="info" :icon="Close" class="hidden group-hover:inline-flex"
                      @click="removeFromQueue(item.queue_item_id)" />
                  </td>
                </tr>
              </tbody>
            </VueDraggable>
          </table>
        </div>

        <div v-show="activeTab === 'lists'" class="p-4 h-full">
          <el-skeleton :loading="previewLoading" animated>
            <template #template>
              <div v-for="i in 5" :key="i" class="mb-4 h-12 bg-white/5 rounded-lg" />
            </template>
            <template #default>
            </template>
          </el-skeleton>
        </div>
      </main>
    </div>
  </el-drawer>
</template>


<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onBeforeUpdate } from 'vue'
import { storeToRefs } from 'pinia'
import { usePlayerStore } from '../stores/player'
import { VueDraggable } from 'vue-draggable-plus'
import { Delete, Rank, Close, VideoPause, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { IQueue } from '../../type'
import { queueApi } from '../../api/queueApi'
const playerStore = usePlayerStore()
const { currentQueue, currentSong, isPlaying, isQueueVisible, currentQueueId } = storeToRefs(playerStore)



// 状态管理
const activeTab = ref('queue')
const songRows = ref([])
const previewQueueId = ref<number>()
const previewData = ref<IQueue | null>()
const previewLoading = ref(false)

// 切换 Tab 时重置预览状态
const handleTabChange = (tab: string) => {
  activeTab.value = tab
  if (tab === 'queue') {
    previewQueueId.value = -1
  }
}

// 1. 点击队列：进入预览模式
const handlePreviewQueue = async (queueId: number) => {
  // 如果点击的是【当前正在播放】的队列，直接切回当前播放视图
  if (queueId === currentQueueId.value) {
    activeTab.value = 'queue'
    return
  }

  // 否则进入预览
  previewQueueId.value = queueId
  previewLoading.value = true
  previewData.value = null

  const res = await playerStore.fetchQueueDetails(queueId)
  if (!res.success) {
    previewLoading.value = false
    return
  }
  if (res.queue) previewData.value = res.queue

}

// 2. 从预览列表播放：切换队列并播放
const playFromPreview = async (index: number) => {
  if (!previewQueueId.value) return

  // 调用 Store 的切换并播放逻辑
  await playerStore.playSongInQueue(previewQueueId.value, index)

  // 切回"当前播放"视图
  activeTab.value = 'queue'
  previewQueueId.value = -1
}

// 3. 返回队列列表
const backToQueueList = () => {
  previewQueueId.value = -1
  previewData.value = null
}

onBeforeUpdate(() => {
  songRows.value = []
})

// 拖拽数据绑定
const dragQueue = computed({
  get: () => currentQueue.value,
  set: (newVal) => playerStore.updateQueueOrder(newVal)
})


// 1. 播放逻辑
const playFromQueue = (index: number) => {
  playerStore.playAtIndex(index)
}

// 2. 移除单曲逻辑
const removeFromQueue = async (itemId: number | string) => {
  await playerStore.removeQueueItem(itemId)
}

// 3. 清空队列
const clearQueue = async (queueId: number) => {
  if (confirm('确定要清空队列吗？')) {
    const res = await queueApi.clearQueue(queueId)
    if (!res.success) {
      ElMessage.error("清空失败")
      return
    }
    await playerStore.fetchCurrentQueue()
  }
}

const confirmClear = () => {
  ElMessageBox.confirm(
    '确定要清空播放队列吗？', // 第1参数：内容
    '提示',                   // 第2参数：标题
    {                        // 第3参数：配置对象
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      // 确保你的 script setup 里有定义或者 import 了 clearQueue
      clearQueue(currentQueueId.value || -1);
    })
    .catch(() => {
      // 用户点击取消，通常不需要做任何事，但 catch 必须写以防控制台报错 Uncaught (in promise)
      console.log('用户取消了清空');
    });
};
// 4. 切换歌单逻辑
const handleSwitchQueue = async (queueId: number) => {
  if (queueId === currentQueueId.value) {
    activeTab.value = 'queue'
    return
  }
  await playerStore.switchQueue(queueId)
  activeTab.value = 'queue'
}

// 5. 删除歌单逻辑
const handleDeleteQueue = async (queueId: number) => {
  if (confirm('确定要永久删除这个歌单吗？')) {
    await playerStore.deleteQueue(queueId)
  }
}

// 工具函数
const formatDuration = (seconds: number) => {
  if (!seconds) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const formatDate = (dateStr: Date) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 滚动到当前歌曲的核心逻辑
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
  if (isQueueVisible.value) setTimeout(scrollToCurrent, 200)
})

// 监听切歌 (如果抽屉开着，也跟着滚)
watch(() => currentSong.value?.song_id, () => {
  if (isQueueVisible.value && activeTab.value === 'queue') {
    scrollToCurrent()
  }
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
  background: currentColor;
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
</style>