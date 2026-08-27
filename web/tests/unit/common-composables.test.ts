import { describe, expect, it, vi } from 'vitest'
import { useAsyncTask } from '@/common/composables/use-async-task'
import { useClipboard } from '@/common/composables/use-clipboard'
import { useModal } from '@/common/composables/use-modal'
import { usePagination } from '@/common/composables/use-pagination'

describe('useAsyncTask', () => {
  it('keeps loading while a task is pending and resets after rejection', async () => {
    const task = useAsyncTask()
    let resolveTask!: (value: string) => void
    const promise = task.run(() => new Promise<string>((resolve) => { resolveTask = resolve }))

    expect(task.loading.value).toBe(true)
    resolveTask('done')
    await expect(promise).resolves.toBe('done')
    expect(task.loading.value).toBe(false)

    await expect(task.run(async () => { throw new Error('failed') })).rejects.toThrow('failed')
    expect(task.loading.value).toBe(false)
  })
})

describe('usePagination', () => {
  it('changes pages and resets to the first page when page size changes', () => {
    const pagination = usePagination(20)

    pagination.change(3)
    pagination.setTotal(61)
    expect(pagination.requestParams.value).toEqual({ page: 3, size: 20 })
    expect(pagination.state.total).toBe(61)

    pagination.change(4, 50)
    expect(pagination.requestParams.value).toEqual({ page: 1, size: 50 })
    pagination.reset()
    expect(pagination.state).toMatchObject({ current: 1, total: 0, pageSize: 50 })
  })
})

describe('useModal', () => {
  it('opens with a payload, hides, and resets', () => {
    const modal = useModal<{ id: number }>()

    modal.show({ id: 7 })
    expect(modal.open.value).toBe(true)
    expect(modal.payload.value).toEqual({ id: 7 })
    modal.hide()
    expect(modal.open.value).toBe(false)
    modal.reset()
    expect(modal.payload.value).toBeNull()
  })
})

describe('useClipboard', () => {
  it('copies text and clears copied state after the timeout', async () => {
    vi.useFakeTimers()
    const writeText = vi.fn().mockResolvedValue(undefined)
    Object.defineProperty(globalThis, 'navigator', { configurable: true, value: { clipboard: { writeText } } })
    Object.defineProperty(globalThis, 'window', { configurable: true, value: globalThis })

    const clipboard = useClipboard(100)
    await expect(clipboard.copy('hello')).resolves.toBe(true)
    expect(writeText).toHaveBeenCalledWith('hello')
    expect(clipboard.copied.value).toBe(true)
    vi.advanceTimersByTime(100)
    expect(clipboard.copied.value).toBe(false)
    expect(await clipboard.copy('')).toBe(false)
    vi.useRealTimers()
  })

  it('returns false when the browser clipboard rejects', async () => {
    Object.defineProperty(globalThis, 'navigator', {
      configurable: true,
      value: { clipboard: { writeText: vi.fn().mockRejectedValue(new Error('denied')) } },
    })
    Object.defineProperty(globalThis, 'window', { configurable: true, value: globalThis })
    const clipboard = useClipboard()

    await expect(clipboard.copy('hello')).resolves.toBe(false)
    expect(clipboard.copied.value).toBe(false)
  })
})
