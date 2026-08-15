/** 秒 → "分:秒"（如 245 → "4:05"）。 */
export function formatDuration(seconds?: number): string {
  if (!seconds) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

/** 时间 → "YYYY/MM/DD"（无效值返回"未知时间"）。 */
export function formatDate(value?: string | number | Date | null): string {
  if (!value) return '未知时间'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '未知时间'
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

/** 拼装静态资源完整地址：已是 http 开头则原样返回，否则拼接 VITE_API_URL。 */
export function getImageUrl(url?: string | null): string {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = import.meta.env.VITE_API_URL ?? ''
  const separator = url.startsWith('/') ? '' : '/'
  return `${base}${separator}${url}`
}

/**
 * 比较两个 ID 是否相同。
 * 后端雪花 ID 在 JSON 里是字符串，前端可能持有 number/string/临时字符串，
 * 统一转字符串比较，避免 number/string 混用导致 === 失配。
 */
export function sameId(a: number | string | null | undefined, b: number | string | null | undefined): boolean {
  return a != null && b != null && String(a) === String(b)
}
