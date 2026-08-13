import { ref } from 'vue'

/** 复制到剪贴板并短暂维持一个已复制的反馈态。 */
export function useClipboard(resetAfter = 1_500) {
  const copied = ref(false)

  async function copy(value: string | null | undefined): Promise<boolean> {
    if (!value) return false
    try {
      await navigator.clipboard.writeText(value)
      copied.value = true
      window.setTimeout(() => {
        copied.value = false
      }, resetAfter)
      return true
    } catch {
      copied.value = false
      return false
    }
  }

  return { copied, copy }
}
