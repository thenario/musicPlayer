import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ActionResult, IQueue, IQueueItem, ISong } from '@/types'
import { logBackgroundFailure, requireSuccess, toActionFailure } from '@/utils/action-result'
import { queueApi } from '@/api/queue-api'
import { getImageUrl, sameId } from '@/utils/format'
import { createAudioEngine } from '@/composables/player/use-audio-engine'
import { createAudioVisualizer } from '@/composables/player/use-audio-visualizer'
import { createLyricsLoader } from '@/composables/player/use-lyrics-loader'
import { createMediaSession } from '@/composables/player/use-media-session'
import { createPlaybackSync } from '@/composables/player/use-playback-sync'
import { createQueueMutations } from '@/composables/player/use-queue-mutations'
import { createQueueService } from '@/composables/player/use-queue-service'

import { useSongStore } from './song'

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
  const playbackSync = createPlaybackSync({
    currentSong,
    currentQueueId,
    currentIndex,
    currentTime,
    isPlaying,
    playMode,
  })
  const syncPlayStateToBackend = () => { void playbackSync.sync().then(logBackgroundFailure) }

  const mediaSession = createMediaSession({
    currentSong,
    onPlay: () => { void resumeSong().then(logBackgroundFailure) },
    onPause: () => pauseSong(),
    onPrevious: () => { void previousSong().then(logBackgroundFailure) },
    onNext: () => { void nextSong().then(logBackgroundFailure) },
  })

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
  const visualizer = createAudioVisualizer()

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
  const setAudioElement = (element: HTMLAudioElement) => {
    audio.setAudioElement(element)
    visualizer.setAudioElement(element)
  }
  const disposeAudio = () => {
    visualizer.dispose()
    audio.dispose()
  }

  const onAudioEnded = () => {
    if (playMode.value === 'repeat_one') {
      void audio.replay().then(logBackgroundFailure)
    } else {
      void nextSong(true).then(logBackgroundFailure)
    }
  }

  const updateMediaSession = () => mediaSession.update()

  const playAtIndex = async (index: number): Promise<ActionResult> => {
    if (index < 0 || index >= currentQueue.value.length) return { success: false, message: '没有可播放的歌曲' }
    try {
      const item = currentQueue.value[index]
      const song = item!.song
      currentIndex.value = index
      currentSong.value = song
      const element = audioElement.value
      if (!element) throw new Error('音频播放器尚未就绪')
      element.src = getImageUrl(song.song_url)
      await element.play()
      // 快速切歌时，旧请求的播放完成不能覆盖新选择。
      if (currentSong.value !== song) return { success: false, message: '播放已取消', canceled: true }
      isPlaying.value = true
      audio.fadeIn(element, 1500)
      updateMediaSession()
      syncPlayStateToBackend()
      // 只能在 Pinia 激活后、动作执行时获取 store，不能在模块加载时获取。
      void useSongStore().syncPlayHistory(song).then(logBackgroundFailure)
      return { success: true }
    } catch (err: unknown) {
      if (currentIndex.value === index) isPlaying.value = false
      return toActionFailure(err, '歌曲播放失败')
    }
  }

  const togglePlay = () => (isPlaying.value ? pauseSong() : resumeSong())

  const pauseSong = () => {
    if (currentSong.value && audioElement.value) {
      audio.pause()
    }
    return { success: true as const }
  }

  const resumeSong = async (): Promise<ActionResult> => {
    if (currentSong.value && audioElement.value) {
      return audio.resume()
    }
    return { success: false, message: '没有可播放的歌曲' }
  }

  const seek = (time: number) => {
    audio.seek(time)
    syncPlayStateToBackend()
  }

  const setVolume = (val: number) => audio.setVolume(val)

  const nextSong = async (isAuto = false): Promise<ActionResult> => {
    if (currentQueue.value.length === 0) return { success: false, message: '播放队列为空' }
    let nextIndex = currentIndex.value + 1

    if (nextIndex >= currentQueue.value.length) {
      if (playMode.value === 'repeat_all' || isAuto) {
        if (playMode.value === 'sequential' && isAuto) {
          isPlaying.value = false
          return { success: true }
        }
        nextIndex = 0
      } else {
        nextIndex = 0
      }
    }
    return playAtIndex(nextIndex)
  }

  const previousSong = async (): Promise<ActionResult> => {
    if (currentQueue.value.length === 0) return { success: false, message: '播放队列为空' }

    if (audioElement.value && audioElement.value.currentTime > 3) {
      audioElement.value.currentTime = 0
      return { success: true }
    }

    let prevIndex = currentIndex.value - 1
    if (prevIndex < 0) prevIndex = currentQueue.value.length - 1
    return playAtIndex(prevIndex)
  }

  const setPlayMode = async (mode: string): Promise<ActionResult> => {
    const previousMode = playMode.value
    const previousQueue = [...currentQueue.value]
    const previousIndex = currentIndex.value
    playMode.value = mode

    try {
      if (mode === 'shuffle') {
        const shuffled = await queueMutations.shuffleQueue()
        requireSuccess(shuffled, '队列随机排序失败')
      }
      await queueApi.setPlayMode(currentQueueId.value ?? -1, mode)
      return { success: true }
    } catch (err: unknown) {
      playMode.value = previousMode
      currentQueue.value = previousQueue
      currentIndex.value = previousIndex
      return toActionFailure(err, '播放模式切换失败')
    }
  }

  const stopPlayback = () => {
    audio.pause()
    currentSong.value = null
    currentIndex.value = -1
  }

  const prepareAudioSource = (songId: number | string, savedProgress: number) => {
    if (!audioElement.value || !currentSong.value) return
    const el = audioElement.value!

    el.src = getImageUrl(currentSong.value!.song_url)

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

  const queueService = createQueueService({
    currentQueue,
    currentQueueId,
    userQueues,
    currentIndex,
    currentSong,
    playMode,
    playAtIndex,
    pauseSong,
    prepareAudioSource,
  })

  const playSong = async (song: ISong, mode: 'now' | 'next'): Promise<ActionResult> => {
    if (mode === 'now' && sameId(currentSong.value?.song_id, song.song_id)) {
      return togglePlay()
    }

    const playImmediately = mode === 'now'

    const res = await queueMutations.addToQueue(song, playImmediately)
    if (!res.success) {
      return res
    }

    const newIndex = res.targetIndex

    if (mode === 'now' && newIndex !== -1) {
      return playAtIndex(newIndex)
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
    visualizerBars: visualizer.bars,

    hasNext,
    hasPrevious,

    setAudioElement,
    disposeAudio,
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

    fetchCurrentQueue: queueService.fetchCurrentQueue,
    fetchUserQueues: queueService.fetchUserQueues,
    fetchQueueDetails: queueService.fetchQueueDetails,
    switchQueue: queueService.switchQueue,
    deleteQueue: queueService.deleteQueue,
    clearQueue: queueService.clearQueue,
    playPlaylist: queueService.playPlaylist,
    playSongInQueue: queueService.playSongInQueue,

    toggleQueueVisibility,
    closeQueue,
    toggleSongDetail,
  }
})
