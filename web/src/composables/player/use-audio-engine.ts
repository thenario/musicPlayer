import type { Ref } from 'vue'

export interface AudioEngineContext {
  audioElement: Ref<HTMLAudioElement | null>
  currentTime: Ref<number>
  duration: Ref<number>
  progress: Ref<number>
  bufferPercent: Ref<number>
  volume: Ref<number>
  isPlaying: Ref<boolean>
  syncPlayStateToBackend: () => void
  onEnded: () => void
  onPlay: () => void
}

const SYNC_INTERVAL = 500000

/** Owns the audio element listeners and releases them when the element is replaced or the app unmounts. */
export function createAudioEngine(ctx: AudioEngineContext) {
  let fadeTimer: ReturnType<typeof setInterval> | null = null
  let lastSyncTime = 0
  let boundElement: HTMLAudioElement | null = null

  const onTimeUpdate = () => {
    const element = boundElement
    if (!element) return
    ctx.currentTime.value = element.currentTime
    if (ctx.duration.value > 0) ctx.progress.value = (ctx.currentTime.value / ctx.duration.value) * 100

    const now = Date.now()
    if (now - lastSyncTime > SYNC_INTERVAL && ctx.isPlaying.value) {
      ctx.syncPlayStateToBackend()
      lastSyncTime = now
    }
  }

  const onLoadedMetadata = () => {
    if (boundElement) ctx.duration.value = boundElement.duration
  }

  const onProgress = () => {
    const element = boundElement
    if (!element || element.duration <= 0) return
    for (let index = element.buffered.length - 1; index >= 0; index -= 1) {
      if (element.buffered.start(index) < element.currentTime) {
        ctx.bufferPercent.value = (element.buffered.end(index) / element.duration) * 100
        return
      }
    }
  }

  const onPause = () => {
    ctx.isPlaying.value = false
    ctx.syncPlayStateToBackend()
  }

  const onPlay = () => {
    ctx.isPlaying.value = true
    ctx.onPlay()
  }

  const onEnded = () => ctx.onEnded()

  const onError = () => {
    ctx.isPlaying.value = false
    ctx.bufferPercent.value = 0
    ctx.syncPlayStateToBackend()
  }

  const detachListeners = () => {
    const element = boundElement
    if (!element) return
    element.removeEventListener('timeupdate', onTimeUpdate)
    element.removeEventListener('loadedmetadata', onLoadedMetadata)
    element.removeEventListener('progress', onProgress)
    element.removeEventListener('pause', onPause)
    element.removeEventListener('play', onPlay)
    element.removeEventListener('ended', onEnded)
    element.removeEventListener('error', onError)
    boundElement = null
  }

  const setAudioElement = (element: HTMLAudioElement | null) => {
    detachListeners()
    ctx.audioElement.value = element
    if (!element) return

    boundElement = element
    element.addEventListener('timeupdate', onTimeUpdate)
    element.addEventListener('loadedmetadata', onLoadedMetadata)
    element.addEventListener('progress', onProgress)
    element.addEventListener('pause', onPause)
    element.addEventListener('play', onPlay)
    element.addEventListener('ended', onEnded)
    element.addEventListener('error', onError)
    element.volume = ctx.volume.value / 100
  }

  const clearFadeTimer = () => {
    if (fadeTimer) clearInterval(fadeTimer)
    fadeTimer = null
  }

  const fadeIn = (audio: HTMLAudioElement, duration = 1500) => {
    clearFadeTimer()
    const targetVolume = ctx.volume.value / 100
    const step = 0.05 * targetVolume
    const interval = duration / 20
    fadeTimer = setInterval(() => {
      const nextVolume = audio.volume + step
      if (nextVolume < targetVolume) {
        audio.volume = nextVolume
      } else {
        audio.volume = targetVolume
        clearFadeTimer()
      }
    }, interval)
  }

  const fadeOut = (audio: HTMLAudioElement, duration = 1000, onComplete?: () => void) => {
    clearFadeTimer()
    const step = (0.1 * ctx.volume.value) / 100
    const interval = duration / 10
    fadeTimer = setInterval(() => {
      const nextVolume = audio.volume - step
      if (nextVolume > 0.01) {
        audio.volume = nextVolume
      } else {
        audio.volume = 0
        clearFadeTimer()
        onComplete?.()
      }
    }, interval)
  }

  const pause = () => {
    const audio = ctx.audioElement.value
    if (audio) fadeOut(audio, 800, () => audio.pause())
  }

  const resume = () => {
    const audio = ctx.audioElement.value
    if (!audio) return
    audio.volume = 0
    audio.play().then(() => fadeIn(audio, 1000)).catch((error) => {
      console.warn('恢复播放失败', error)
      audio.volume = ctx.volume.value / 100
    })
  }

  const replay = () => {
    const audio = ctx.audioElement.value
    if (!audio) return
    audio.currentTime = 0
    audio.play().then(() => fadeIn(audio, 1500)).catch((error) => {
      console.warn('单曲循环播放被拦截', error)
      audio.volume = ctx.volume.value / 100
    })
  }

  const seek = (time: number) => {
    const audio = ctx.audioElement.value
    if (!audio) return
    audio.currentTime = time
    ctx.currentTime.value = time
  }

  const setVolume = (value: number) => {
    ctx.volume.value = value
    if (ctx.audioElement.value) ctx.audioElement.value.volume = value / 100
  }

  const dispose = () => {
    clearFadeTimer()
    detachListeners()
    ctx.audioElement.value = null
  }

  return { setAudioElement, fadeIn, fadeOut, pause, resume, replay, seek, setVolume, dispose }
}