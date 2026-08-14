import { ref, shallowRef } from 'vue'

/** 弹窗开关 + 可选 payload（用于打开时携带数据，如编辑对象）。 */
export function useModal<T = unknown>() {
  const open = ref(false)
  const payload = shallowRef<T | null>(null)

  function show(value: T | null = null): void {
    payload.value = value
    open.value = true
  }

  function hide(): void {
    open.value = false
  }

  function reset(): void {
    open.value = false
    payload.value = null
  }

  return { open, payload, show, hide, reset }
}
