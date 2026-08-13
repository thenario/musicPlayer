import { watch } from 'vue'
import { useMagicKeys, useActiveElement } from '@vueuse/core'
import { usePlayerStore } from '@/stores/player'

/** 键盘快捷键：空格切换播放/暂停（输入框内不触发）。 */
export function useKeyboardShortcuts() {
  const playerStore = usePlayerStore()
  const { space } = useMagicKeys()
  const activeElement = useActiveElement()

  watch(() => space?.value, (v) => {
    const isTyping = ['INPUT', 'TEXTAREA'].includes(activeElement.value?.tagName || '')
    if (v && !isTyping) playerStore.togglePlay()
  })
}
