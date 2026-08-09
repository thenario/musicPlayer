import { ref, watch } from 'vue'
import { usePlayerStore } from '@/stores/player'
import { VOLUME_DEFAULT } from '../const'

/** 音量控制：滑块值同步 + 静音切换（记住静音前音量）。 */
export function useVolumeControl() {
  const playerStore = usePlayerStore()
  const volumeValue = ref(playerStore.volume)
  const prevVol = ref(VOLUME_DEFAULT)

  watch(() => playerStore.volume, (val) => {
    volumeValue.value = val
  })

  const toggleMute = () => {
    if (playerStore.volume > 0) {
      prevVol.value = playerStore.volume
      playerStore.setVolume(0)
    } else {
      playerStore.setVolume(prevVol.value)
    }
  }

  const setVolume = (value: number) => playerStore.setVolume(value)

  return { volumeValue, toggleMute, setVolume }
}
