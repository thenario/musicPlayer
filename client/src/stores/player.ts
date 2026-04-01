import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { IQueue, IQueueItem, ISong, LyricLine } from '../../type'
import { queueApi } from '../../axios/queueApi'
import { songApi } from '../../axios/songApi'
import { parseLyrics } from '../utils/lrcParser'
const API_BASE_URL = import.meta.env.VITE_API_URL

export const usePlayerStore = defineStore('player', () => {
  const bufferPercent = ref(0)
  const currentSong = ref<ISong | null>(null)
  const isPlaying = ref<boolean>(false)
  const audioElement = ref<HTMLAudioElement | null>(null)
  let fadeTimer: ReturnType<typeof setInterval> | null = null

  const lyrics = ref<LyricLine[]>()
  const isLoadingLyrics = ref(false)

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

  const SYNC_INTERVAL = 500000
  let lastSyncTime = 0

  const hasNext = computed(() => currentQueue.value.length > 0)
  const hasPrevious = computed(() => currentQueue.value.length > 0)

  const syncPlayStateToBackend = async () => {
    if (!currentSong.value) return
    try {
      if (currentSong.value) {
        const now = new Date()
        await queueApi.updateCurrentQueueState({
          current_song_id: currentSong.value.song_id,
          current_position: currentIndex.value,
          current_progress: currentTime.value,
          is_playing: isPlaying.value,
          current_queue_id: currentQueueId.value ?? 0,
          playmode: playMode.value,
          updated_date: now,
        })
      }
      return { success: true }
    } catch (err: any) {
      console.log(err)
      return { success: false }
    }
  }

  const reorderQueueOrder = async () => {
    if (!currentQueueId.value) return
    else {
      try {
        const songIds = currentQueue.value.map((item) => item.song.song_id)
        await queueApi.reorderQueue(songIds, currentQueueId.value)
        return { success: true }
      } catch (err: any) {
        console.log(err)
        return { success: false }
      }
    }
  }

  const onAudioEnded = () => {
    if (playMode.value === 'repeat_one') {
      if (audioElement.value) {
        audioElement.value.currentTime = 0
        audioElement.value
          .play()
          .then(() => {
            fadeIn(audioElement.value!, 1500)
          })
          .catch((e) => {
            console.warn('单曲循环播放被拦截', e)
            audioElement.value!.volume = 1
          })
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

    element.addEventListener('progress', () => {
      if (element.duration > 0) {
        for (let i = 0; i < element.buffered.length; i++) {
          if (element.buffered.start(element.buffered.length - 1 - i) < element.currentTime) {
            const bufferEnd = element.buffered.end(element.buffered.length - 1 - i)
            bufferPercent.value = (bufferEnd / element.duration) * 100
            break
          }
        }
      }
    })

    element.volume = volume.value / 100
  }

  const fadeIn = (audio: HTMLAudioElement, duration: number = 1500) => {
    if (fadeTimer) clearInterval(fadeTimer)

    const targetVolume = volume.value / 100
    const step = 0.05 * targetVolume
    const interval = duration / 20

    fadeTimer = setInterval(() => {
      if (!audio) {
        if (fadeTimer) clearInterval(fadeTimer)
        return
      }

      const nextVolume = audio.volume + step
      if (nextVolume < targetVolume) {
        audio.volume = nextVolume
      } else {
        audio.volume = targetVolume
        if (fadeTimer) {
          clearInterval(fadeTimer)
          fadeTimer = null
        }
      }
    }, interval)
  }

  const fadeOut = (audio: HTMLAudioElement, duration: number = 1000, onComplete?: () => void) => {
    if (fadeTimer) clearInterval(fadeTimer)

    const step = (0.1 * volume.value) / 100
    const interval = duration / 10

    fadeTimer = setInterval(() => {
      if (!audio) {
        clearInterval(fadeTimer!)
        return
      }

      const nextVolume = audio.volume - step
      if (nextVolume > 0.01) {
        audio.volume = nextVolume
      } else {
        audio.volume = 0
        clearInterval(fadeTimer!)
        fadeTimer = null
        if (onComplete) onComplete()
      }
    }, interval)
  }

  const playAtIndex = async (index: number) => {
    if (index < 0 || index >= currentQueue.value.length) return { success: false }
    try {
      const item = currentQueue.value[index]
      const song = item!.song
      currentIndex.value = index
      currentSong.value = song
      isPlaying.value = true
      if (audioElement.value) {
        const token = localStorage.getItem('token')
        audioElement.value.src = `${API_BASE_URL}/api/songs/${song.song_id}/stream?token=${token}`
        audioElement.value
          .play()
          .then(() => {
            fadeIn(audioElement.value!, 1500)
          })
          .catch((e) => {
            console.warn('自动播放被拦截', e)
            audioElement.value!.volume = 1
          })
      }
      updateMediaSession()
      syncPlayStateToBackend()
      return { success: true }
    } catch (err: any) {
      console.log(err)
      return { success: false }
    }
  }

  const togglePlay = () => (isPlaying.value ? pauseSong() : resumeSong())

  const pauseSong = () => {
    if (currentSong.value && audioElement.value) {
      const audio = audioElement.value
      isPlaying.value = false
      fadeOut(audio, 800, () => {
        audio.pause()
      })
    }
  }

  const resumeSong = () => {
    if (currentSong.value && audioElement.value) {
      const audio = audioElement.value

      audio.volume = 0
      audio
        .play()
        .then(() => {
          fadeIn(audio, 1000)
        })
        .catch((e) => {
          console.warn('恢复播放失败', e)
          audio.volume = 1
        })
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

  const playSong = async (song: ISong, mode: 'now' | 'next') => {
    if (mode === 'now' && currentSong.value?.song_id === song.song_id) {
      togglePlay()
      return { success: true }
    }

    const insertNext = mode === 'next'

    const res = await addToQueue(song, insertNext)
    if (!res.success) {
      return { success: false }
    }

    const newIndex = res.targetIndex

    if (mode === 'now' && newIndex !== -1) {
      playAtIndex(newIndex)
    }
    return { success: true }
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
    try {
      await queueApi.setPlayMode(currentQueueId.value ?? -1, mode)
      return { success: true }
    } catch (err: any) {
      console.log(err)
      return { success: false }
    }
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
      ;[others[i]!, others[j]!] = [others[j]!, others[i]!]
    }

    if (currentItem) {
      const cleanOthers = others.filter((item) => item.queue_item_id !== currentItem.queue_item_id)
      currentQueue.value = [currentItem, ...cleanOthers]
      currentIndex.value = 0
    } else {
      currentQueue.value = others
      currentIndex.value = -1
    }

    const res = await reorderQueueOrder()
    if (res?.success) {
      return { success: true }
    } else {
      return { success: false }
    }
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
    const res = await reorderQueueOrder()
    if (res?.success) {
      return { success: true }
    } else {
      return { success: false }
    }
  }

  const addToQueue = async (song: ISong, insertNext = false) => {
    if (!song?.song_id) return { targetIndex: -1, success: false }

    const oldIdx = handleDuplicateSong(song.song_id)
    const targetIndex = Math.max(0, insertNext ? currentIndex.value + 1 : currentIndex.value)

    const tempId = `temp-${Date.now()}`
    const newItem = {
      queue_item_id: tempId,
      queue_item_position: targetIndex,
      queue_id: currentQueueId.value ?? -1,
      song,
      added_date: new Date(),
    }

    currentQueue.value.splice(targetIndex, 0, newItem)
    if (!insertNext) currentIndex.value = targetIndex

    try {
      const res = await queueApi.addSongToQueue(song.song_id, currentQueueId.value || 0, insertNext)
      finalizeQueueItem(tempId, res.data, oldIdx === -1)
      return { targetIndex, success: true }
    } catch (err: any) {
      console.log(err)
      rollbackQueue(tempId)
      return { targetIndex: -1, success: false }
    }
  }

  const handleDuplicateSong = (songId: number) => {
    const idx = currentQueue.value.findIndex((item) => item.song.song_id === songId)
    if (idx !== -1) {
      currentQueue.value.splice(idx, 1)
      if (idx < currentIndex.value) currentIndex.value--
    }
    return idx
  }

  const finalizeQueueItem = (tempId: string, data: any, isNewAddition: boolean) => {
    const item = currentQueue.value.find((i) => i.queue_item_id === tempId)
    if (item && data) {
      Object.assign(item, {
        queue_item_id: data.queue_item_id,
        queue_item_position: data.queue_item_position,
        queue_id: data.queue_id,
      })
      currentQueueId.value = data.queue_id
    }

    if (currentQueueId.value && isNewAddition) {
      const target = userQueues.value.find((q) => q.queue_id === currentQueueId.value)
      if (target) target.song_count++
    }
  }

  const rollbackQueue = (tempId: string) => {
    const idx = currentQueue.value.findIndex((i) => i.queue_item_id === tempId)
    if (idx !== -1) currentQueue.value.splice(idx, 1)
  }

  const removeQueueItem = async (itemId: number | string) => {
    if (!itemId) return { success: false }

    try {
      const isTempId = typeof itemId === 'string' && itemId.startsWith('temp-')
      if (!isTempId) {
        const res = await syncRemoveToServer(itemId as number)
        if (!res.success) return { success: false }
      }

      const targetIdx = currentQueue.value.findIndex((item) => item.queue_item_id === itemId)
      if (targetIdx !== -1) {
        handleStateAfterRemoval(targetIdx)
      }
      updateQueueCount(-1)
      return { success: true }
    } catch (e) {
      console.error('移除歌曲失败', e)
      return { success: false }
    }
  }

  const syncRemoveToServer = async (id: number) => {
    if (!currentQueueId.value) {
      ElMessage.error('移除歌曲时出错')
      return { success: false }
    }
    try {
      await queueApi.removeSongFromQueue(currentQueueId.value, id)
      return { success: true }
    } catch (err: any) {
      console.log(err)
      return { success: false }
    }
  }

  const handleStateAfterRemoval = (idx: number) => {
    const isDeletingCurrent = idx === currentIndex.value
    currentQueue.value.splice(idx, 1)
    if (isDeletingCurrent) {
      if (currentQueue.value.length > 0) {
        const nextIdx = Math.min(idx, currentQueue.value.length - 1)
        playAtIndex(nextIdx)
      } else {
        stopPlayback()
      }
    } else if (idx < currentIndex.value) {
      currentIndex.value--
    }
  }

  const stopPlayback = () => {
    pauseSong()
    currentSong.value = null
    currentIndex.value = -1
  }

  const updateQueueCount = (delta: number) => {
    if (!currentQueueId.value) return
    const target = userQueues.value.find((q) => q.queue_id === currentQueueId.value)
    if (target && target.song_count + delta >= 0) {
      target.song_count += delta
    }
  }

  const fetchCurrentQueue = async () => {
    try {
      const res = await queueApi.getCurrentQueue()
      const { queue, queue_state } = res
      updateQueueData(queue)
      if (queue_state) {
        syncPlaybackState(queue_state)
      }
      return { success: true }
    } catch (e: any) {
      console.error(e)
      return { success: false, message: e.message }
    }
  }

  const updateQueueData = (queue: any) => {
    if (queue.queue_items) {
      queue.queue_items.sort((a: any, b: any) => a.queue_item_position - b.queue_item_position)
      currentQueue.value = [...queue.queue_items]
    }
    currentQueueId.value = queue.queue_id
  }

  const syncPlaybackState = (state: any) => {
    playMode.value = state.playmode || 'sequential'

    if (!state.current_song_id) return

    const idx = currentQueue.value.findIndex((i) => i.song.song_id === state.current_song_id)
    if (idx === -1) return

    currentIndex.value = idx
    currentSong.value = currentQueue.value[idx]!.song

    if (audioElement.value && currentSong.value) {
      prepareAudioSource(currentSong.value.song_id, state.current_progress || 0)
    }
  }

  const prepareAudioSource = (songId: number, savedProgress: number) => {
    const el = audioElement.value!
    const token = localStorage.getItem('token')

    el.src = `${API_BASE_URL}/api/songs/${songId}/stream?token=${token}`

    el.addEventListener(
      'loadedmetadata',
      () => {
        const duration = el.duration
        const isProgressValid = savedProgress > 0 && savedProgress < duration - 2

        const targetTime = isProgressValid ? savedProgress : 0
        el.currentTime = targetTime
        currentTime.value = targetTime
      },
      { once: true },
    )
  }

  const fetchUserQueues = async () => {
    try {
      const res = await queueApi.getMyQueues()
      const rawQueues = res.queues ?? []
      rawQueues.forEach((queue: any) => {
        if (queue.queue_items && Array.isArray(queue.queue_items)) {
          queue.queue_items.sort((a: any, b: any) => {
            return a.queue_item_position - b.queue_item_position
          })
        }
      })
      userQueues.value = rawQueues
      return { success: true }
    } catch (e: any) {
      console.error(e)
      return { success: false, message: e.message }
    }
  }

  const fetchQueueDetails = async (queueId: number) => {
    try {
      const res = await queueApi.getQueueById(queueId)
      return {
        success: true,
        queue: res.queue,
      }
    } catch (err: any) {
      console.log(err)
      return {
        success: false,
        message: err.message,
      }
    }
  }

  const playSongInQueue = async (queueId: number, index: number) => {
    try {
      if (currentQueueId.value !== queueId) {
        await switchQueue(queueId)
      }
      playAtIndex(index)
      return { success: true }
    } catch (e) {
      console.error('切换播放失败', e)
      return { success: false }
    }
  }

  const switchQueue = async (queueId: number) => {
    try {
      await queueApi.alterQueueToCurrent(queueId)
      currentQueueId.value = queueId
      await fetchCurrentQueue()
      await fetchUserQueues()
      pauseSong()
    } catch (err: any) {
      console.log(err)
      throw err
    }
  }

  const deleteQueue = async (queueId: number) => {
    try {
      const res = await queueApi.deletQueue(queueId)
      const { newQueueId, wasActive } = res.data
      if (wasActive) {
        currentSong.value = null
        currentQueue.value = []
        pauseSong()
        if (newQueueId) {
          await switchQueue(newQueueId)
        } else {
          currentQueueId.value = null
        }
      }
      const resF = await fetchUserQueues()
      if (!resF.success) return { success: false }
      return { success: true }
    } catch (err: any) {
      console.log(err)
      return { success: false }
    }
  }

  const clearQueue = async (queueId: number) => {
    try {
      await queueApi.clearQueue(queueId)
      if (currentQueueId.value === queueId) {
        currentQueue.value = []
        currentSong.value = null
        currentIndex.value = -1
        pauseSong()
        const resF = await fetchUserQueues()
        if (!resF.success) return { success: false }
        return { success: true }
      }
    } catch (err: any) {
      console.log(err)
      return { success: false }
    }
  }

  const playPlaylist = async (playlistId: number, startSongId = null) => {
    ElMessage.success(`播放歌单: ${playlistId}`)
    try {
      await queueApi.createQueueFromPlaylist(playlistId)
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
      return { success: true }
    } catch (err: any) {
      console.log(err)
      return { success: false }
    }
  }

  const toggleQueueVisibility = () => (isQueueVisible.value = !isQueueVisible.value)
  const closeQueue = () => (isQueueVisible.value = false)
  const toggleSongDetail = () => (isSongDetailVisible.value = !isSongDetailVisible.value)

  watch(
    () => currentSong.value?.song_id,
    async (id) => {
      if (!id) {
        lyrics.value = []
        return
      }
      try {
        isLoadingLyrics.value = true
        const res = await songApi.getLyrics(id)

        if (res.success) {
          lyrics.value = parseLyrics(res.lyrics || '', res.t_lyrics || '')
        } else {
          lyrics.value = [{ time: 0, content: '未找到歌词' }]
        }
      } catch (error) {
        console.error('获取歌词发生硬错误:', error)
        lyrics.value = [{ time: 0, content: '歌词加载失败' }]
      } finally {
        isLoadingLyrics.value = false
      }
    },
  )

  return {
    currentSong,
    bufferPercent,
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
    lyrics,

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
    clearQueue,
    playPlaylist,
    playSongInQueue,

    toggleQueueVisibility,
    closeQueue,
    toggleSongDetail,
  }
})
