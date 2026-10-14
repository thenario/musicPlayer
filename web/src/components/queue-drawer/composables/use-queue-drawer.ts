import { ref, watch, nextTick, onBeforeUnmount } from 'vue'
import { storeToRefs } from 'pinia'
import { usePlayerStore } from '@/stores/player'
import { sameId } from '@/utils/format'
import type { ActionResult, IQueue } from '@/types'
import { requireSuccess, toActionFailure } from '@/utils/action-result'
import { tokenStorage } from '@/utils/storage'

/** 队列抽屉：标签切换、预览、清空/删除/切换队列、拖拽排序与滚动定位。 */
export function useQueueDrawer() {
  const playerStore = usePlayerStore()
  const { currentQueue, currentSong, isPlaying, isQueueVisible, currentQueueId, userQueues } =
    storeToRefs(playerStore)

  const activeTab = ref('queue')
  const previewQueueId = ref<number | string>(-1)
  const previewData = ref<IQueue | null>(null)
  const previewLoading = ref(false)
  let scrollTimerId: ReturnType<typeof window.setTimeout> | undefined

  const handleTabChange = (tab: string) => {
    activeTab.value = tab
    if (tab === 'queue') {
      previewQueueId.value = -1
      previewData.value = null
    }
  }

  const handlePreviewQueue = (queueId: number | string) => {
    previewQueueId.value = queueId
    const found = userQueues.value.find((q) => sameId(q.queue_id, queueId))
    if (found) previewData.value = found
  }

  const playFromPreview = async (index: number) => {
    if (previewQueueId.value === -1) return { success: false, message: '尚未选择队列' }
    const result = await playerStore.playSongInQueue(previewQueueId.value, index)
    if (!result.success) return result
    activeTab.value = 'queue'
    previewQueueId.value = -1
    return { success: true }
  }

  const backToQueueList = () => {
    previewQueueId.value = -1
    previewData.value = null
  }

  const playFromQueue = async (index: number) => {
    return playerStore.playAtIndex(index)
  }

  const removeFromQueue = async (itemId: number | string) => {
    return playerStore.removeQueueItem(itemId)
  }

  const clearCurrentQueue = async (): Promise<ActionResult> => {
    if (!currentQueueId.value) return { success: false, message: '没有当前播放队列' }
    return playerStore.clearQueue(currentQueueId.value)
  }

  const handleSwitchQueue = async (queueId: number | string) => {
    if (sameId(queueId, currentQueueId.value)) {
      activeTab.value = 'queue'
      return { success: true }
    }
    const result = await playerStore.switchQueue(queueId)
    if (result.success) activeTab.value = 'queue'
    return result
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
    if (scrollTimerId !== undefined) {
      window.clearTimeout(scrollTimerId)
      scrollTimerId = undefined
    }
    if (isQueueVisible.value) {
      scrollTimerId = window.setTimeout(() => {
        scrollTimerId = undefined
        void scrollToCurrent()
      }, 250)
    }
  })

  const loadQueues = async (): Promise<ActionResult> => {
    if (!tokenStorage.get()) return { success: true }
    try {
      requireSuccess(await playerStore.fetchCurrentQueue(), '当前队列加载失败')
      requireSuccess(await playerStore.fetchUserQueues(), '队列列表加载失败')
      return { success: true }
    } catch (error) {
      return toActionFailure(error, '队列初始化失败')
    }
  }

  onBeforeUnmount(() => {
    if (scrollTimerId !== undefined) window.clearTimeout(scrollTimerId)
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
    updateQueueOrder: playerStore.updateQueueOrder,
    loadQueues,
    handleTabChange,
    handlePreviewQueue,
    playFromPreview,
    backToQueueList,
    playFromQueue,
    removeFromQueue,
    clearCurrentQueue,
    handleSwitchQueue,
    deleteQueue: playerStore.deleteQueue,
  }
}
