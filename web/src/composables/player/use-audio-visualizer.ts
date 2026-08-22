import { ref, type Ref } from 'vue'

const IDLE_BARS = [28, 48, 36, 58]
const FREQUENCY_BINS = [1, 3, 6, 10]

/**
 * Reads the currently playing HTMLAudioElement through Web Audio API.
 * It owns only analyser lifecycle; play/pause remains the audio engine's job.
 */
export function createAudioVisualizer() {
  const bars = ref([...IDLE_BARS])
  let audioContext: AudioContext | null = null
  let source: MediaElementAudioSourceNode | null = null
  let analyser: AnalyserNode | null = null
  let frequencyData: Uint8Array<ArrayBuffer> | null = null
  let animationFrameId: number | null = null
  let boundElement: HTMLAudioElement | null = null

  const resetBars = () => {
    bars.value = [...IDLE_BARS]
  }

  const stop = () => {
    if (animationFrameId !== null) cancelAnimationFrame(animationFrameId)
    animationFrameId = null
    resetBars()
  }

  const update = () => {
    if (!analyser || !frequencyData) return

    analyser.getByteFrequencyData(frequencyData)
    bars.value = FREQUENCY_BINS.map((index) => Math.max(18, Math.round(((frequencyData![index] ?? 0) / 255) * 100)))
    animationFrameId = requestAnimationFrame(update)
  }

  const ensureGraph = () => {
    const element = boundElement
    if (!element || audioContext) return

    audioContext = new AudioContext()
    source = audioContext.createMediaElementSource(element)
    analyser = audioContext.createAnalyser()
    analyser.fftSize = 64
    analyser.smoothingTimeConstant = 0.75
    frequencyData = new Uint8Array(analyser.frequencyBinCount)

    source.connect(analyser)
    analyser.connect(audioContext.destination)
  }

  const start = async () => {
    ensureGraph()
    if (!audioContext || animationFrameId !== null) return

    try {
      await audioContext.resume()
      update()
    } catch (error) {
      // Audio playback must remain usable if a browser rejects analyser access.
      console.warn('音频可视化启动失败', error)
      resetBars()
    }
  }

  const onPlay = () => void start()
  const onPause = () => stop()

  const setAudioElement = (element: HTMLAudioElement | null) => {
    if (boundElement === element) return
    dispose()
    boundElement = element
    if (!element) return

    element.addEventListener('play', onPlay)
    element.addEventListener('pause', onPause)
    element.addEventListener('ended', onPause)
  }

  const dispose = () => {
    stop()
    if (boundElement) {
      boundElement.removeEventListener('play', onPlay)
      boundElement.removeEventListener('pause', onPause)
      boundElement.removeEventListener('ended', onPause)
    }
    source?.disconnect()
    analyser?.disconnect()
    if (audioContext && audioContext.state !== 'closed') void audioContext.close()
    audioContext = null
    source = null
    analyser = null
    frequencyData = null
    boundElement = null
  }

  return { bars: bars as Ref<number[]>, setAudioElement, dispose }
}
