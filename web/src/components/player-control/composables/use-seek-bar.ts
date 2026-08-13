import { ref, computed, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { usePlayerStore } from '@/stores/player'

/** 进度条：滑块同步、拖拽中不跳、seek 落点。 */
export function useSeekBar() {
  const playerStore = usePlayerStore()
  const { bufferPercent } = storeToRefs(playerStore)
  const isDragging = ref(false)
  const sliderValue = ref(0)

  watch(() => playerStore.currentTime, (val) => {
    if (!isDragging.value) sliderValue.value = val || 0
  })

  const safeDuration = computed(() => Number(playerStore.duration) || 0)

  const handleSeekInput = () => {
    isDragging.value = true
  }

  const handleSeekChange = (e: any) => {
    const val = Number(e.target.value)
    playerStore.seek(val)
    isDragging.value = false
  }

  return { sliderValue, safeDuration, bufferPercent, handleSeekInput, handleSeekChange }
}
