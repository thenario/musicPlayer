import { ref } from 'vue'
import type { ActionResult } from '@/types'
import { toActionFailure } from '@/utils/action-result'

/** 复制到剪贴板并短暂维持一个已复制的反馈态。 */
export function useClipboard(resetAfter = 1_500) {
  const copied = ref(false)

  async function copy(value: string | null | undefined): Promise<ActionResult> {
    if (!value) return { success: false, message: '没有可复制的内容' }
    try {
      await navigator.clipboard.writeText(value)
      copied.value = true
      window.setTimeout(() => {
        copied.value = false
      }, resetAfter)
      return { success: true }
    } catch (error) {
      copied.value = false
      return toActionFailure(error, '复制失败')
    }
  }

  return { copied, copy }
}
