import type { Ref } from 'vue'

export interface AudioEngineContext {
  audioElement: Ref<HTMLAudioElement | null>
  currentTime: Ref<number>
  duration: Ref<number>
  progress: Ref<number>
  bufferPercent: Ref<number>
  volume: Ref<number>
  isPlaying: Ref<boolean>
  /** 播放进度同步（周期 + pause 时触发） */
  syncPlayStateToBackend: () => void
  /** 播放结束回调（单曲循环或切下一首） */
  onEnded: () => void
  /** 开始播放回调（更新 Media Session） */
  onPlay: () => void
}

const SYNC_INTERVAL = 500000

/**
 * 音频引擎：audio 元素的事件接线、淡入淡出、播放/暂停/跳转/音量。
 * 通过 context 读写共享状态，供 player store 组合。
 */
export function createAudioEngine(ctx: AudioEngineContext) {
  let fadeTimer: ReturnType<typeof setInterval> | null = null
  let lastSyncTime = 0

  const setAudioElement = (element: HTMLAudioElement) => {
    ctx.audioElement.value = element
    if (!element) return

    element.addEventListener('timeupdate', () => {
      ctx.currentTime.value = element.currentTime
      if (ctx.duration.value > 0) {
        ctx.progress.value = (ctx.currentTime.value / ctx.duration.value) * 100
      }

      const now = Date.now()
      if (now - lastSyncTime > SYNC_INTERVAL && ctx.isPlaying.value) {
        ctx.syncPlayStateToBackend()
        lastSyncTime = now
      }
    })

    element.addEventListener('loadedmetadata', () => {
      ctx.duration.value = element.duration
    })

    element.addEventListener('ended', () => ctx.onEnded())

    element.addEventListener('pause', () => {
      ctx.isPlaying.value = false
      ctx.syncPlayStateToBackend()
    })

    element.addEventListener('play', () => {
      ctx.isPlaying.value = true
      ctx.onPlay()
    })

    element.addEventListener('progress', () => {
      if (element.duration > 0) {
        for (let i = 0; i < element.buffered.length; i++) {
          if (element.buffered.start(element.buffered.length - 1 - i) < element.currentTime) {
            const bufferEnd = element.buffered.end(element.buffered.length - 1 - i)
            ctx.bufferPercent.value = (bufferEnd / element.duration) * 100
            break
          }
        }
      }
    })

    element.volume = ctx.volume.value / 100
  }

  const fadeIn = (audio: HTMLAudioElement, duration: number = 1500) => {
    if (fadeTimer) clearInterval(fadeTimer)

    const targetVolume = ctx.volume.value / 100
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

    const step = (0.1 * ctx.volume.value) / 100
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

  const pause = () => {
    const audio = ctx.audioElement.value
    if (!audio) return
    fadeOut(audio, 800, () => audio.pause())
  }

  const resume = () => {
    const audio = ctx.audioElement.value
    if (!audio) return
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

  /** 单曲循环：从头重播。 */
  const replay = () => {
    const audio = ctx.audioElement.value
    if (!audio) return
    audio.currentTime = 0
    audio
      .play()
      .then(() => {
        fadeIn(audio, 1500)
      })
      .catch((e) => {
        console.warn('单曲循环播放被拦截', e)
        audio.volume = 1
      })
  }

  const seek = (time: number) => {
    if (ctx.audioElement.value) {
      ctx.audioElement.value.currentTime = time
      ctx.currentTime.value = time
    }
  }

  const setVolume = (val: number) => {
    ctx.volume.value = val
    if (ctx.audioElement.value) ctx.audioElement.value.volume = val / 100
  }

  return { setAudioElement, fadeIn, fadeOut, pause, resume, replay, seek, setVolume }
}
