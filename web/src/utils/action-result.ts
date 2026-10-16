import axios from 'axios'
import type { ActionFailure } from '@/types'

export function toActionFailure(error: unknown, fallback = '操作失败'): ActionFailure {
  const detail = error && typeof error === 'object' ? error as Record<string, unknown> : undefined
  const message = typeof detail?.message === 'string' && detail.message ? detail.message : fallback
  return {
    success: false,
    message,
    ...(typeof detail?.code === 'number' ? { code: detail.code } : {}),
    ...(axios.isCancel(error) || detail?.name === 'AbortError' || detail?.canceled === true || error === 'cancel' || error === 'close'
      ? { canceled: true } : {}),
    ...(detail?.validation === true ? { validation: true } : {}),
    ...(detail?.completed === true ? { completed: true } : {}),
  }
}

export function requireSuccess<T extends { success: boolean; message?: string }>(
  result: T, fallback = '操作失败',
): asserts result is T & { success: true } {
  if (!result.success) {
    const failure = toActionFailure(result, fallback)
    const message = result.message && fallback !== '操作失败'
      ? `${fallback}：${result.message}` : failure.message
    throw Object.assign(new Error(message), failure, { message })
  }
}

export function logBackgroundFailure(result: { success: boolean; message?: string; canceled?: boolean }) {
  if (!result.success && !result.canceled) console.warn(result.message || '后台任务失败')
}
