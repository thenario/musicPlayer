import { ElMessage } from 'element-plus'
import { toActionFailure } from './action-result'

/** 由组件决定何时提示；不传 customMessage 时使用业务返回的 message。 */
export function reportError(error: unknown, customMessage?: string) {
  const failure = toActionFailure(error)
  if (failure.canceled || failure.validation) return
  console.error(error)
  ElMessage.error(customMessage ?? failure.message)
}
