import { computed, reactive } from 'vue'

export interface PaginationState {
  current: number
  pageSize: number
  total: number
}

/** 分页状态：当前页 / 每页条数 / 总数，统一由 usePagination 维护。 */
export function usePagination(defaultPageSize = 10) {
  const state = reactive<PaginationState>({
    current: 1,
    pageSize: defaultPageSize,
    total: 0,
  })

  const requestParams = computed(() => ({
    page: state.current,
    size: state.pageSize,
  }))

  function setTotal(total: number): void {
    state.total = total
  }

  /** 切换页码/每页条数；每页条数变化时回到第一页。 */
  function change(current: number, pageSize?: number): void {
    if (pageSize && pageSize !== state.pageSize) {
      state.current = 1
      state.pageSize = pageSize
      return
    }
    state.current = current
  }

  function reset(): void {
    state.current = 1
    state.total = 0
  }

  return { state, requestParams, setTotal, change, reset }
}
