import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { usePlayerStore } from '@/stores/player'
import type { IQueue } from '@/types'

/** 队列抽屉：标签切换、预览、清空/删除/切换队列、拖拽排序与滚动定位。 */
export function useQueueDrawer() {
  const playerStore = usePlayerStore()
  const { currentQueue, currentSong, isPlaying, isQueueVisible, currentQueueId, userQueues } =
    storeToRefs(playerStore)

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

  const handlePreviewQueue = (queueId: number) => {
    previewQueueId.value = queueId
    const found = userQueues.value.find((q) => q.queue_id === queueId)
    if (found) previewData.value = found
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
    set: (newVal) => playerStore.updateQueueOrder(newVal),
  })

  const playFromQueue = async (index: number) => {
    await playerStore.playAtIndex(index)
  }

  const removeFromQueue = async (itemId: number | string) => {
    await playerStore.removeQueueItem(itemId)
  }

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

  const handleSwitchQueue = async (queueId: number) => {
    if (queueId === currentQueueId.value) {
      activeTab.value = 'queue'
      return
    }
    try {
      await playerStore.switchQueue(queueId)
      activeTab.value = 'queue'
    } catch (err: any) {
      // 错误已由拦截器统一提示，这里只记录日志
      console.log(err)
    }
  }

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

  return {
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
  }
}
