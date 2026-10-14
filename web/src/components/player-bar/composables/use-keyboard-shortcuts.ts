import { watch } from 'vue'
import { useMagicKeys, useActiveElement } from '@vueuse/core'
import { usePlayerStore } from '@/stores/player'
import type { ActionFailure } from '@/types'

/** 键盘快捷键：空格切换播放/暂停（输入框内不触发）。 */
export function useKeyboardShortcuts(onFailure: (result: ActionFailure) => void) {
  const playerStore = usePlayerStore()
  const { space } = useMagicKeys()
  const activeElement = useActiveElement()

  watch(() => space?.value, async (v) => {
    const isTyping = ['INPUT', 'TEXTAREA'].includes(activeElement.value?.tagName || '')
    if (v && !isTyping) {
      const result = await playerStore.togglePlay()
      if (!result.success) onFailure(result)
    }
  })
}
