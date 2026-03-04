import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { IQueue, IQueueItem, ISong } from '../../type'
import { queueApi } from '../../api/queueApi'
import { ElMessage } from 'element-plus'

const API_BASE_URL = 'http://127.0.0.1:5000'

export const usePlayerStore = defineStore('player', () => {
  const currentSong = ref<ISong | null>(null)
  const isPlaying = ref<boolean>(false)
  const audioElement = ref<HTMLAudioElement | null>(null)

  const progress = ref<number>(0)
  const currentTime = ref<number>(0)
  const duration = ref<number>(0)
  const volume = ref<number>(80)

  const currentQueue = ref<IQueueItem[]>([])
  const currentQueueId = ref<number | null>(null)
  const userQueues = ref<IQueue[]>([])

  const currentIndex = ref<number>(-1)
  const playMode = ref<string>('sequential')

  const isQueueVisible = ref<boolean>(false)
  const isSongDetailVisible = ref<boolean>(false)

  const SYNC_INTERVAL = 5000
  let lastSyncTime = 0

  const hasNext = computed(() => currentQueue.value.length > 0)
  const hasPrevious = computed(() => currentQueue.value.length > 0)

  const syncPlayStateToBackend = async () => {
    if (!currentSong.value) return
    if (currentSong.value) {
      const now = new Date()
      const res = await queueApi.updateCurrentQueueState({
        current_song_id: currentSong.value.song_id,
        current_position: currentIndex.value,
        current_progress: currentTime.value,
        is_playing: isPlaying.value,
        current_queue_id: currentQueueId.value ?? 0,
        playmode: playMode.value,
        updated_date: now,
      })
      if (!res.success) ElMessage.error('更新状态时出错')
    }
  }

  const reorderQueueOrder = async () => {
    if (!currentQueueId.value) return
    else {
      const songIds = currentQueue.value.map((item) => item.song.song_id)
      const res = await queueApi.reorderQueue(songIds, currentQueueId.value)
      if (!res.success) ElMessage.error('队列顺序更新失败')
    }
  }

  const onAudioEnded = () => {
    if (playMode.value === 'repeat_one') {
      if (audioElement.value) {
        audioElement.value.currentTime = 0
        audioElement.value.play()
      }
    } else {
      nextSong(true)
    }
  }

  const updateMediaSession = () => {
    if ('mediaSession' in navigator && currentSong.value) {
      const song = currentSong.value

      let coverUrl = song.song_cover_url || ''
      if (coverUrl && !coverUrl.startsWith('http')) {
        coverUrl = `${API_BASE_URL}${coverUrl.startsWith('/') ? '' : '/'}${coverUrl}`
      }

      navigator.mediaSession.metadata = new MediaMetadata({
        title: song.song_title,
        artist: song.artist,
        album: song.album,
        artwork: coverUrl ? [{ src: coverUrl, sizes: '512x512', type: 'image/jpeg' }] : [],
      })

      navigator.mediaSession.setActionHandler('play', () => resumeSong())
      navigator.mediaSession.setActionHandler('pause', () => pauseSong())
      navigator.mediaSession.setActionHandler('previoustrack', () => previousSong())
      navigator.mediaSession.setActionHandler('nexttrack', () => nextSong())
    }
  }

  const setAudioElement = (element: HTMLAudioElement) => {
    audioElement.value = element
    if (!element) return

    element.addEventListener('timeupdate', () => {
      currentTime.value = element.currentTime
      if (duration.value > 0) {
        progress.value = (currentTime.value / duration.value) * 100
      }

      const now = Date.now()
      if (now - lastSyncTime > SYNC_INTERVAL && isPlaying.value) {
        syncPlayStateToBackend()
        lastSyncTime = now
      }
    })

    element.addEventListener('loadedmetadata', () => {
      duration.value = element.duration
    })

    element.addEventListener('ended', onAudioEnded)

    element.addEventListener('pause', () => {
      isPlaying.value = false
      syncPlayStateToBackend()
    })

    element.addEventListener('play', () => {
      isPlaying.value = true
      updateMediaSession()
    })

    element.volume = volume.value / 100
  }

  const playAtIndex = async (index: number) => {
    if (index < 0 || index >= currentQueue.value.length) return

    const item = currentQueue.value[index]
    const song = item.song

    currentIndex.value = index
    currentSong.value = song
    isPlaying.value = true

    if (audioElement.value) {
      const token = localStorage.getItem('token')
      audioElement.value.src = `${API_BASE_URL}/api/songs/${song.song_id}/stream?token=${token}`
      audioElement.value.play().catch((e) => console.warn('自动播放被拦截', e))
    }

    updateMediaSession()

    syncPlayStateToBackend()
  }

  const togglePlay = () => (isPlaying.value ? pauseSong() : resumeSong())

  const pauseSong = () => {
    audioElement.value?.pause()
  }

  const resumeSong = () => {
    if (currentSong.value) {
      audioElement.value?.play()
    }
  }

  const seek = (time: number) => {
    if (audioElement.value) {
      audioElement.value.currentTime = time
      currentTime.value = time
      syncPlayStateToBackend()
    }
  }

  const setVolume = (val: number) => {
    volume.value = val
    if (audioElement.value) audioElement.value.volume = val / 100
  }

  const playSong = async (song: ISong) => {
    if (currentSong.value?.song_id === song.song_id) {
      togglePlay()
      return
    }
    const res = await addToQueue(song, true)
    const newIndex = res.targetIndex
    if (newIndex !== -1) {
      playAtIndex(newIndex)
    }
  }

  const nextSong = (isAuto = false) => {
    if (currentQueue.value.length === 0) return
    let nextIndex = currentIndex.value + 1

    if (nextIndex >= currentQueue.value.length) {
      if (playMode.value === 'repeat_all' || isAuto) {
        if (playMode.value === 'sequential' && isAuto) {
          isPlaying.value = false
          return
        }
        nextIndex = 0
      } else {
        nextIndex = 0
      }
    }
    playAtIndex(nextIndex)
  }

  const previousSong = () => {
    if (currentQueue.value.length === 0) return

    if (audioElement.value && audioElement.value.currentTime > 3) {
      audioElement.value.currentTime = 0
      return
    }

    let prevIndex = currentIndex.value - 1
    if (prevIndex < 0) prevIndex = currentQueue.value.length - 1
    playAtIndex(prevIndex)
  }

  const setPlayMode = async (mode: string) => {
    playMode.value = mode

    if (mode === 'shuffle') {
      await shuffleQueue()
    }
    const res = await queueApi.setPlayMode(mode)
    if (!res.success) ElMessage.error('播放模式更新失败')
  }

  const shuffleQueue = async () => {
    if (currentQueue.value.length <= 1) return

    const currentId = currentSong.value?.song_id
    let currentItem = null
    const others = []

    for (const item of currentQueue.value) {
      if (item.song.song_id === currentId && !currentItem) {
        currentItem = item
      } else {
        others.push(item)
      }
    }

    for (let i = others.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[others[i], others[j]] = [others[j], others[i]]
    }

    if (currentItem) {
      currentQueue.value = [currentItem, ...others]
      currentIndex.value = 0
    } else {
      currentQueue.value = others
      currentIndex.value = -1
    }

    await reorderQueueOrder()
  }

  const updateQueueOrder = async (newQueue: IQueueItem[]) => {
    currentQueue.value = newQueue

    if (currentSong.value) {
      const tempCurrentSong = currentSong.value
      const newIndex = currentQueue.value.findIndex(
        (item) => item.song.song_id === tempCurrentSong.song_id,
      )
      if (newIndex !== -1) {
        currentIndex.value = newIndex
      }
    }
    await reorderQueueOrder()
  }

  const addToQueue = async (song: ISong, insertNext = false) => {
    if (!song || !song.song_id) return { targetIndex: -1, success: false }
    let success = true
    const oldIdx = currentQueue.value.findIndex((item) => item.song.song_id === song.song_id)
    if (oldIdx !== -1) {
      if (insertNext && oldIdx === currentIndex.value) return { targetIndex: oldIdx, success: true }
      currentQueue.value.splice(oldIdx, 1)
      if (oldIdx < currentIndex.value) currentIndex.value--
    }

    // 2. 计算位置 (保持不变)
    let targetIndex = insertNext ? currentIndex.value + 1 : currentQueue.value.length

    // 3. 本地插入 (临时 ID)
    const tempId = `temp-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    const newItem = {
      queue_item_id: tempId,
      queue_item_poition: -1,
      queue_id: currentQueueId.value ?? -1,
      song: song,
      added_date: new Date(),
    }

    currentQueue.value.splice(targetIndex, 0, newItem)

    const res = await queueApi.addSongToQueue(song.song_id, currentQueueId.value ?? -1, targetIndex)
    if (res.success && res.action === 'added') {
      const itemToUpdate = currentQueue.value.find((item) => item.queue_item_id === tempId)

      if (itemToUpdate) {
        if (res.queue_item.queue_item_id) {
          itemToUpdate.queue_item_id = res.queue_item.queue_item_id
        }

        if (res.queue_item.song) {
          itemToUpdate.song = res.queue_item.song
        }

        if (currentQueueId.value) {
          const targetQueue = userQueues.value.find((q) => q.queue_id === currentQueueId.value)
          if (targetQueue) targetQueue.song_count++
        }
      }
    } else if (res.success && res.action === 'jump_to_existing') {
    } else if (!res.success) {
      success = false
      const failIdx = currentQueue.value.findIndex((item) => item.queue_item_id === tempId)
      if (failIdx !== -1) currentQueue.value.splice(failIdx, 1)
      ElMessage.error('歌曲插入时出错')
    }

    return { targetIndex, success }
  }

  const removeQueueItem = async (itemId: number | string) => {
    if (!itemId) return

    try {
      const isTempId = typeof itemId === 'string' && itemId.startsWith('temp-')
      if (!isTempId) {
        const res = await queueApi.removeSongFromQueue(typeof itemId !== 'string' ? itemId : 0)
        if (!res.success) {
          ElMessage.error('移除歌曲时出错')
          return
        }
      }

      const currentIdx = currentQueue.value.findIndex((item) => item.queue_item_id === itemId)
      if (currentIdx !== -1) {
        const isDeletingCurrent = currentIdx === currentIndex.value

        currentQueue.value.splice(currentIdx, 1)

        if (isDeletingCurrent) {
          if (currentQueue.value.length > 0) {
            const nextIdx = Math.min(currentIdx, currentQueue.value.length - 1)
            playAtIndex(nextIdx)
          } else {
            pauseSong()
            currentSong.value = null
            currentIndex.value = -1
          }
        } else if (currentIdx < currentIndex.value) {
          currentIndex.value--
        }
      }

      if (currentQueueId.value) {
        const targetQueue = userQueues.value.find((q) => q.queue_id === currentQueueId.value)
        if (targetQueue && targetQueue.song_count > 0) {
          targetQueue.song_count--
        }
      }
    } catch (e) {
      console.error('移除歌曲失败', e)
      fetchUserQueues()
    }
  }

  const fetchCurrentQueue = async () => {
    try {
      const res = await queueApi.getCurrentQueue()
      if (!res.success) {
        ElMessage.error('获取列表失败')
        return
      }
      const queueData = res.queue
      const stateData = res.queue_state

      if (queueData && queueData.queue_items) {
        const items = queueData.queue_items
        // 初始化本地队列
        currentQueue.value = [...items]
        currentQueueId.value = queueData.queue_id

        if (stateData) {
          playMode.value = stateData.playmode || 'sequential'

          if (stateData.current_song_id) {
            const idx = currentQueue.value.findIndex(
              (i) => i.song.song_id === stateData.current_song_id,
            )
            if (idx !== -1) {
              currentIndex.value = idx
              currentSong.value = currentQueue.value[idx].song

              if (audioElement.value) {
                audioElement.value.src = `${API_BASE_URL}/api/songs/${currentSong.value.song_id}/stream`

                const savedProgress = stateData.current_progress || 0

                const restoreProgress = () => {
                  const d = audioElement.value ? audioElement.value.duration : 0
                  if (savedProgress > 0 && savedProgress < d - 2) {
                    if (audioElement.value) {
                      audioElement.value.currentTime = savedProgress
                    }
                    currentTime.value = savedProgress
                  } else {
                    if (audioElement.value) {
                      audioElement.value.currentTime = 0
                    }
                    currentTime.value = 0
                  }
                }
                audioElement.value.addEventListener('loadedmetadata', restoreProgress, {
                  once: true,
                })
              }
            }
          }
        }
      }
    } catch (e) {
      console.error(e)
      return {
        success: false,
      }
    }
  }

  const fetchUserQueues = async () => {
    try {
      const res = await queueApi.getMyQueues()
      if (!res.success) {
        ElMessage.error('获取用户队列失败')
        return
      }
      userQueues.value = res.queues ?? []
    } catch (e) {
      console.error(e)
    }
  }

  const fetchQueueDetails = async (queueId: number) => {
    const res = await queueApi.getQueueById(queueId)
    if (!res.success) {
      ElMessage.error('获取队列详情失败')
      return {
        success: false,
      }
    }
    return {
      success: true,
      queue: res.queue,
    }
  }

  const playSongInQueue = async (queueId: number, index: number) => {
    try {
      if (currentQueueId.value !== queueId) {
        await switchQueue(queueId)
      }
      playAtIndex(index)
    } catch (e) {
      console.error('切换播放失败', e)
    }
  }

  const switchQueue = async (queueId: number) => {
    const res = await queueApi.alterQueueToCurrent(queueId)
    if (!res.success) {
      ElMessage.error('切换队列时出错')
      return
    }
    currentQueueId.value = queueId
    await fetchCurrentQueue()
    await fetchUserQueues()
    pauseSong() // 切换后暂停
  }

  const deleteQueue = async (queueId: number) => {
    const res = await queueApi.deletQueue(queueId)
    if (!res.success) {
      ElMessage.error('切换队列时出错')
      return
    }
    await fetchUserQueues()
    if (currentQueueId.value === queueId) {
      currentQueue.value = []
      currentSong.value = null
      isPlaying.value = false
      currentQueueId.value = null
    }
  }

  const playPlaylist = async (playlistId: number, startSongId = null) => {
    ElMessage.success(`播放歌单: ${playlistId}`)
    const res = await queueApi.createQueueFromPlaylist(playlistId)
    if (!res.success) {
      ElMessage.error('播放歌单时出错')
      return
    }
    await fetchCurrentQueue()
    await fetchUserQueues()

    let startIndex = 0
    if (startSongId) {
      const foundIndex = currentQueue.value.findIndex((item) => item.song.song_id === startSongId)
      if (foundIndex !== -1) startIndex = foundIndex
    }

    if (currentQueue.value.length > 0) {
      playAtIndex(startIndex)
    }
  }

  const toggleQueueVisibility = () => (isQueueVisible.value = !isQueueVisible.value)
  const closeQueue = () => (isQueueVisible.value = false)
  const toggleSongDetail = () => (isSongDetailVisible.value = !isSongDetailVisible.value)

  return {
    currentSong,
    isPlaying,
    progress,
    currentTime,
    duration,
    volume,
    playMode,
    currentQueue,
    currentQueueId,
    userQueues,
    isQueueVisible,
    isSongDetailVisible,

    hasNext,
    hasPrevious,

    setAudioElement,
    playSong,
    playAtIndex,
    togglePlay,
    pauseSong,
    resumeSong,
    nextSong,
    previousSong,
    seek,
    setVolume,
    setPlayMode,

    addToQueue,
    removeQueueItem,
    updateQueueOrder,

    fetchCurrentQueue,
    fetchUserQueues,
    fetchQueueDetails,
    switchQueue,
    deleteQueue,
    playPlaylist,
    playSongInQueue,

    toggleQueueVisibility,
    closeQueue,
    toggleSongDetail,
  }
})
