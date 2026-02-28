<template>
  <!-- 遮罩层 (保持不变) -->
  <div v-if="isQueueVisible" class="fixed inset-0 z-40 bg-black/20" @click="playerStore.closeQueue"></div>

  <!-- 抽屉主体 -->
  <transition name="slide-fade">
    <div v-if="isQueueVisible"
      class="fixed top-0 bottom-20 right-0 z-50 w-[450px] bg-gray-900/95 backdrop-blur-md border-l border-gray-800 shadow-2xl flex flex-col"
      @click.stop>
      <!-- 头部：Tab 切换栏 -->
      <div class="h-14 flex items-center justify-between px-2 border-b border-gray-800 shrink-0">
        <!-- Tab 按钮 (保持不变) -->
        <div class="flex bg-gray-800/50 p-1 rounded-lg ml-2">
          <button @click="switchTab('queue')"
            class="px-4 py-1.5 text-sm rounded-md transition-all duration-150 active:scale-90"
            :class="activeTab === 'queue' ? 'bg-blue-600 text-white shadow-md font-bold' : 'text-gray-400 hover:text-white hover:bg-white/10'">
            当前播放
          </button>
          <button @click="switchTab('lists')"
            class="px-4 py-1.5 text-sm rounded-md transition-all duration-150 active:scale-90"
            :class="activeTab === 'lists' ? 'bg-blue-600 text-white shadow-md font-bold' : 'text-gray-400 hover:text-white hover:bg-white/10'">
            我的队列
          </button>
        </div>

        <!-- 头部右侧操作区 -->
        <div v-if="activeTab === 'queue'" class="flex items-center gap-3 mr-4">
          <span class="text-xs text-gray-500 mr-2">{{ currentQueue.length }} 首</span>
          <div class="w-px h-3 bg-gray-700"></div>
          <button @click="clearQueue" class="text-xs text-gray-400 hover:text-red-400">清空</button>
        </div>

        <!-- ★★★ 新增：详情页的返回按钮 ★★★ -->
        <div v-else-if="activeTab === 'lists' && previewQueueId" class="mr-4">
          <button @click="backToQueueList" class="text-xs flex items-center gap-1 text-gray-400 hover:text-white">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
            返回列表
          </button>
        </div>

        <div v-else class="mr-4 text-xs text-gray-500">
          共 {{ userQueues.length }} 个队列
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="flex-1 overflow-y-auto custom-scrollbar p-0">

        <!-- ================= Tab 1: 当前播放队列 ================= -->
        <div v-if="activeTab === 'queue'" class="h-full">
          <!-- 空状态 -->
          <div v-if="currentQueue.length === 0" class="h-full flex flex-col items-center justify-center text-gray-500">
            <svg class="w-12 h-12 mb-3 opacity-20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3">
              </path>
            </svg>
            <p>队列是空的</p>
          </div>

          <!-- 歌曲列表 -->
          <table v-else class="w-full text-left">
            <draggable v-model="dragQueue" tag="tbody" item-key="id" handle=".drag-handle" @end="onDragEnd"
              class="divide-y divide-gray-800/50" ghost-class="bg-gray-700/50">
              <template #item="{ element: item, index }">
                <tr :ref="el => { if (el) songRows[index] = el }"
                  class="group hover:bg-white/5 transition-colors cursor-pointer"
                  :class="{ 'bg-white/10': item.song.id === currentSong?.id }" @dblclick="playFromQueue(index)">
                  <!-- 序号列改为：拖拽手柄 + 序号/播放图标 -->
                  <td class="pl-4 py-3 w-12 text-xs text-gray-500 font-mono flex items-center gap-2">
                    <!-- 拖拽手柄 (Hover时显示) -->
                    <div
                      class="drag-handle cursor-grab active:cursor-grabbing text-gray-600 hover:text-white opacity-0 group-hover:opacity-100 transition-opacity">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8h16M4 16h16">
                        </path>
                      </svg>
                    </div>

                    <!-- 原有的播放状态/序号逻辑 -->
                    <div class="w-6 text-center">
                      <div v-if="item.song.id === currentSong?.id" class="text-green-500">
                        <svg v-if="isPlaying" class="w-3 h-3 animate-pulse" fill="currentColor" viewBox="0 0 24 24">
                          <path d="M8 5v14l11-7z" />
                        </svg>
                        <svg v-else class="w-3 h-3" fill="currentColor" viewBox="0 0 24 24">
                          <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" />
                        </svg>
                      </div>
                      <template v-else>
                        <span class="group-hover:hidden">{{ index + 1 }}</span>
                        <button @click.stop="playFromQueue(index)" class="hidden group-hover:block text-white">
                          <svg class="w-3 h-3 fill-current" viewBox="0 0 24 24">
                            <path d="M8 5v14l11-7z" />
                          </svg>
                        </button>
                      </template>
                    </div>
                  </td>

                  <!-- ...其他列 (歌名、歌手、时长、移除) 保持不变... -->
                  <!-- 歌名 -->
                  <td class="px-2 py-3 max-w-[180px]">
                    <!-- ❌ 原来可能是: {{ item.title }} -->
                    <!-- ✅ 现在必须是: item.song?.title -->
                    <div class="text-sm text-white truncate"
                      :class="{ 'text-green-500': item.song?.id === currentSong?.id }">
                      {{ item.song?.title || '加载中...' }}
                    </div>
                  </td>

                  <!-- 歌手 -->
                  <td class="px-2 py-3 max-w-[100px]">
                    <!-- ✅ item.song?.artist -->
                    <div class="text-xs text-gray-400 truncate">
                      {{ item.song?.artist || '-' }}
                    </div>
                  </td>

                  <!-- 时长 -->
                  <td class="px-2 py-3 w-12 text-right text-xs text-gray-500 font-mono">
                    <!-- ✅ item.song?.duration -->
                    {{ formatDuration(item.song?.duration) }}
                  </td>

                  <!-- 移除按钮 -->
                  <td class="pr-4 py-3 w-8 text-right">
                    <!-- 这里的 item.id 是包装对象的 ID，用于删除，这个是对的 -->
                    <button @click.stop="removeFromQueue(item.id)"
                      class="text-gray-500 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-opacity disabled:cursor-not-allowed">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12">
                        </path>
                      </svg>
                    </button>
                  </td>
                </tr>
              </template>
            </draggable>
          </table>
        </div>

        <!-- ================= Tab 2: 我的播放列表 ================= -->
        <!-- ================= Tab 2: 我的队列 (双视图) ================= -->
        <div v-else class="h-full">

          <!-- 视图 A: 队列列表 (默认显示) -->
          <div v-if="!previewQueueId" class="p-3 space-y-2">
            <div v-for="q in userQueues" :key="q.id" @click="handlePreviewQueue(q.id)"
              class="p-3 rounded-lg cursor-pointer transition-all group relative border border-transparent"
              :class="q.id === currentQueueId ? 'bg-white/10 border-white/20' : 'hover:bg-white/5 border-gray-800'">
              <div class="flex justify-between items-start">
                <div>
                  <div class="font-medium text-white truncate w-64"
                    :class="{ 'text-green-400': q.id === currentQueueId }">
                    {{ q.name }}
                  </div>
                  <div class="text-xs text-gray-500 mt-1 flex items-center gap-2">
                    <span>{{ q.item_count }} 首歌曲</span>
                    <span>•</span>
                    <span>{{ formatDate(q.updated_date) }}</span>
                  </div>
                </div>
                <!-- 正在播放标识 -->
                <div v-if="q.id === currentQueueId" class="text-green-500">
                  <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd"
                      d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                      clip-rule="evenodd" />
                  </svg>
                </div>
              </div>

              <!-- 删除按钮 -->
              <button v-if="q.id !== currentQueueId" @click.stop="handleDeleteQueue(q.id)"
                class="absolute top-1/2 -translate-y-1/2 right-3 p-2 rounded-full text-gray-500 hover:text-white hover:bg-red-500/20 opacity-0 group-hover:opacity-100 transition-all">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16">
                  </path>
                </svg>
              </button>
            </div>

            <div class="text-center py-8 text-xs text-gray-600">
              🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵
            </div>
          </div>

          <!-- 视图 B: 队列详情 (预览模式) -->
          <div v-else class="h-full flex flex-col">
            <div v-if="previewLoading" class="flex-1 flex items-center justify-center text-gray-500">
              加载中...
            </div>
            <div v-else-if="!previewData || previewData.items.length === 0"
              class="flex-1 flex items-center justify-center text-gray-500">
              此队列为空
            </div>

            <!-- 预览歌曲列表 -->
            <table v-else class="w-full text-left">
              <tbody class="divide-y divide-gray-800/50">
                <tr v-for="(item, index) in previewData.items" :key="item.id"
                  class="group hover:bg-white/5 transition-colors cursor-pointer" @dblclick="playFromPreview(index)">
                  <td class="pl-4 py-3 w-10 text-xs text-gray-500 font-mono">
                    <span class="group-hover:hidden">{{ index + 1 }}</span>
                    <button @click.stop="playFromPreview(index)"
                      class="hidden group-hover:block text-white hover:text-green-400">
                      <svg class="w-3 h-3 fill-current" viewBox="0 0 24 24">
                        <path d="M8 5v14l11-7z" />
                      </svg>
                    </button>
                  </td>
                  <td class="px-2 py-3">
                    <div class="text-sm text-white truncate max-w-[180px]">{{ item.song.title }}</div>
                  </td>
                  <td class="px-2 py-3">
                    <div class="text-xs text-gray-400 truncate max-w-[100px]">{{ item.song.artist }}</div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick, onBeforeUpdate } from 'vue'
import draggable from 'vuedraggable'
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'
import api from '../utils/api'

const playerStore = usePlayerStore()
const { currentQueue, currentSong, isPlaying, isQueueVisible, userQueues, currentQueueId } = storeToRefs(playerStore)

// 状态管理
const activeTab = ref('queue')
const songRows = ref([])
const previewQueueId = ref(null) // 当前正在预览的队列ID (非播放)
const previewData = ref(null)    // 预览的数据
const previewLoading = ref(false)

// 切换 Tab 时重置预览状态
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'queue') {
    previewQueueId.value = null
  }
}

// 1. 点击队列：进入预览模式
const handlePreviewQueue = async (queueId) => {
  // 如果点击的是【当前正在播放】的队列，直接切回当前播放视图
  if (queueId === currentQueueId.value) {
    activeTab.value = 'queue'
    return
  }

  // 否则进入预览
  previewQueueId.value = queueId
  previewLoading.value = true
  previewData.value = null

  const data = await playerStore.fetchQueueDetails(queueId)
  // 后端返回结构通常是 { id:..., items: [...] } 或直接是对象
  // 假设后端 queue_to_dict 返回的是对象
  previewData.value = data.queue || data
  previewLoading.value = false
}

// 2. 从预览列表播放：切换队列并播放
const playFromPreview = async (index) => {
  if (!previewQueueId.value) return

  // 调用 Store 的切换并播放逻辑
  await playerStore.playSongInQueue(previewQueueId.value, index)

  // 切回"当前播放"视图
  activeTab.value = 'queue'
  previewQueueId.value = null
}

// 3. 返回队列列表
const backToQueueList = () => {
  previewQueueId.value = null
  previewData.value = null
}

onBeforeUpdate(() => {
  songRows.value = []
})

// 拖拽数据绑定
const dragQueue = computed({
  get: () => playerStore.currentQueue,
  set: (val) => {
    // 拖拽变化时，直接更新 Store，Store 内部会自动同步给后端
    playerStore.updateQueueOrder(val)
  }
})

// 【修复】onDragEnd 可以留空，因为 updateQueueOrder 已经处理了保存
const onDragEnd = () => {
  // 这里的逻辑已经在 computed set 里处理了，不需要重复发请求
}

// 1. 播放逻辑
const playFromQueue = (index) => {
  playerStore.playAtIndex(index)
}

// 2. 移除单曲逻辑
const removeFromQueue = async (itemId) => {
  await playerStore.removeQueueItem(itemId)
}

// 3. 清空队列
const clearQueue = async () => {
  if (confirm('确定要清空队列吗？')) {
    await api.post('/api/queue/clear', {})
    await playerStore.fetchCurrentQueue()
  }
}

// 4. 切换歌单逻辑
const handleSwitchQueue = async (queueId) => {
  if (queueId === currentQueueId.value) {
    activeTab.value = 'queue'
    return
  }
  await playerStore.switchQueue(queueId)
  activeTab.value = 'queue'
}

// 5. 删除歌单逻辑
const handleDeleteQueue = async (queueId) => {
  if (confirm('确定要永久删除这个歌单吗？')) {
    await playerStore.deleteQueue(queueId)
  }
}

// 工具函数
const formatDuration = (seconds) => {
  if (!seconds) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 滚动到当前歌曲的核心逻辑
const scrollToCurrent = async () => {
  // 【修复】必须等待 DOM 更新 (nextTick)
  await nextTick()

  if (!currentSong.value || !currentQueue.value) return

  // 找到索引
  const index = currentQueue.value.findIndex(item => item.song.id === currentSong.value.id)

  // 确保 DOM 元素存在
  if (index !== -1 && songRows.value[index]) {
    songRows.value[index].scrollIntoView({
      behavior: 'smooth',
      block: 'center'
    })
  }
}

// 监听抽屉打开
watch(isQueueVisible, (visible) => {
  if (visible) {
    // 延迟一点点，等抽屉动画开始后再滚
    setTimeout(() => {
      scrollToCurrent()
    }, 100)
  }
})

// 监听切歌 (如果抽屉开着，也跟着滚)
watch(() => currentSong.value?.id, () => {
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
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  /* 更平滑的缓动 */
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: rgba(255, 255, 255, 0.3);
}
</style>