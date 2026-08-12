import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { IQueue, IQueueItem, IQueueState, ISong } from '@/types'
import { queueApi } from '@/api/queueApi'
import { getImageUrl, sameId } from '@/utils/format'
import { createAudioEngine } from '@/composables/player/useAudioEngine'
import { createLyricsLoader } from '@/composables/player/useLyricsLoader'
import { createQueueMutations } from '@/composables/player/useQueueMutations'

export const usePlayerStore = defineStore('player', () => {
  // ================= 状态 =================
  const audioElement = ref<HTMLAudioElement | null>(null)
  const bufferPercent = ref(0)
  const currentSong = ref<ISong | null>(null)
  const isPlaying = ref<boolean>(false)
  const progress = ref<number>(0)
  const currentTime = ref<number>(0)
  const duration = ref<number>(0)
  const volume = ref<number>(80)

  const currentQueue = ref<IQueueItem[]>([])
  const currentQueueId = ref<number | string | null>(null)
  const userQueues = ref<IQueue[]>([])
  const currentIndex = ref<number>(-1)
  const playMode = ref<string>('sequential')

  const isQueueVisible = ref<boolean>(false)
  const isSongDetailVisible = ref<boolean>(false)

  // ================= 播放状态同步到后端 =================
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

  // ================= 音频引擎（audio 元素 + 淡入淡出） =================
  const audio = createAudioEngine({
    audioElement,
    currentTime,
    duration,
    progress,
    bufferPercent,
    volume,
    isPlaying,
    syncPlayStateToBackend,
    onEnded: () => onAudioEnded(),
    onPlay: () => updateMediaSession(),
  })

  // ================= 歌词加载 =================
  const lyricsLoader = createLyricsLoader(() => currentSong.value?.song_id)
  const { lyrics } = lyricsLoader

  // ================= 队列变更（乐观增删 / 排序 / 乱序） =================
  const queueMutations = createQueueMutations({
    currentQueue,
    currentQueueId,
    userQueues,
    currentIndex,
    currentSong,
    playAtIndex: (index) => playAtIndex(index),
    stopPlayback: () => stopPlayback(),
  })

  // ================= 播放控制 =================
  const setAudioElement = (element: HTMLAudioElement) => audio.setAudioElement(element)

  const onAudioEnded = () => {
    if (playMode.value === 'repeat_one') {
      audio.replay()
    } else {
      nextSong(true)
    }
  }

  const updateMediaSession = () => {
    if ('mediaSession' in navigator && currentSong.value) {
      const song = currentSong.value

      const coverUrl = getImageUrl(song.song_cover_url)

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

  const playAtIndex = async (index: number) => {
    if (index < 0 || index >= currentQueue.value.length) return { success: false }
    try {
      const item = currentQueue.value[index]
      const song = item!.song
      currentIndex.value = index
      currentSong.value = song
      isPlaying.value = true
      if (audioElement.value) {
        audioElement.value.src = currentSong.value.song_url
        audioElement.value
          .play()
          .then(() => {
            audio.fadeIn(audioElement.value!, 1500)
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
      audio.pause()
    }
  }

  const resumeSong = () => {
    if (currentSong.value && audioElement.value) {
      audio.resume()
    }
  }

  const seek = (time: number) => {
    audio.seek(time)
    syncPlayStateToBackend()
  }

  const setVolume = (val: number) => audio.setVolume(val)

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
      await queueMutations.shuffleQueue()
    }
    try {
      await queueApi.setPlayMode(currentQueueId.value ?? -1, mode)
      return { success: true }
    } catch (err: any) {
      console.log(err)
      return { success: false }
    }
  }

  const stopPlayback = () => {
    audio.pause()
    currentSong.value = null
    currentIndex.value = -1
  }

  // ================= 队列数据同步 =================
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

  const updateQueueData = (queue: IQueue) => {
    if (queue.queue_items) {
      queue.queue_items.sort((a, b) => a.queue_item_position - b.queue_item_position)
      currentQueue.value = [...queue.queue_items]
    }
    currentQueueId.value = queue.queue_id
  }

  const syncPlaybackState = (state: IQueueState) => {
    playMode.value = state.playmode || 'sequential'

    if (!state.current_song_id) return

    const idx = currentQueue.value.findIndex((i) => sameId(i.song.song_id, state.current_song_id))
    if (idx === -1) return

    currentIndex.value = idx
    currentSong.value = currentQueue.value[idx]!.song

    if (audioElement.value && currentSong.value) {
      prepareAudioSource(currentSong.value.song_id, state.current_progress || 0)
    }
  }

  const prepareAudioSource = (songId: number | string, savedProgress: number) => {
    const el = audioElement.value!

    el.src = currentSong.value!.song_url

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
      rawQueues.sort((a, b) => {
        const timeA = new Date(a.updated_date).getTime()
        const timeB = new Date(b.updated_date).getTime()
        return timeB - timeA
      })
      rawQueues.forEach((queue) => {
        if (queue.queue_items && Array.isArray(queue.queue_items)) {
          queue.queue_items.sort((a, b) => {
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

  const fetchQueueDetails = async (queueId: number | string) => {
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

  const playSongInQueue = async (queueId: number | string, index: number) => {
    try {
      if (!sameId(currentQueueId.value, queueId)) {
        await switchQueue(queueId)
      }
      playAtIndex(index)
      return { success: true }
    } catch (e) {
      console.error('切换播放失败', e)
      return { success: false }
    }
  }

  const switchQueue = async (queueId: number | string) => {
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

  const deleteQueue = async (queueId: number | string) => {
    try {
      const res = await queueApi.deleteQueue(queueId)
      const { new_queue_id, was_active } = res.data
      if (was_active) {
        currentSong.value = null
        currentQueue.value = []
        pauseSong()
        if (new_queue_id) {
          await switchQueue(new_queue_id)
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

  const clearQueue = async (queueId: number | string) => {
    try {
      await queueApi.clearQueue(queueId)
      if (sameId(currentQueueId.value, queueId)) {
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

  const playPlaylist = async (playlistId: number | string, startSongId: number | string | null = null) => {
    try {
      await queueApi.createQueueFromPlaylist(playlistId)
      await fetchCurrentQueue()
      await fetchUserQueues()
      let startIndex = 0
      if (startSongId) {
        const foundIndex = currentQueue.value.findIndex((item) => sameId(item.song.song_id, startSongId))
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

  const playSong = async (song: ISong, mode: 'now' | 'next') => {
    if (mode === 'now' && sameId(currentSong.value?.song_id, song.song_id)) {
      togglePlay()
      return { success: true }
    }

    const insertNext = mode === 'next'

    const res = await queueMutations.addToQueue(song, insertNext)
    if (!res.success) {
      return { success: false }
    }

    const newIndex = res.targetIndex

    if (mode === 'now' && newIndex !== -1) {
      playAtIndex(newIndex)
    }
    return { success: true }
  }

  // ================= UI 开关 =================
  const toggleQueueVisibility = () => (isQueueVisible.value = !isQueueVisible.value)
  const closeQueue = () => (isQueueVisible.value = false)
  const toggleSongDetail = () => (isSongDetailVisible.value = !isSongDetailVisible.value)

  // ================= 派生状态 =================
  // nextSong/previousSong 均支持循环环绕，故队列非空时两个方向都可用
  const hasNext = computed(() => currentQueue.value.length > 0)
  const hasPrevious = computed(() => currentQueue.value.length > 0)

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

    addToQueue: queueMutations.addToQueue,
    removeQueueItem: queueMutations.removeQueueItem,
    updateQueueOrder: queueMutations.updateQueueOrder,

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
