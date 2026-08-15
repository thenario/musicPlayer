import { computed, ref } from 'vue'

/** Async task wrapper whose loading state remains true while any request is active. */
export function useAsyncTask() {
  const pendingCount = ref(0)
  const loading = computed(() => pendingCount.value > 0)

  async function run<T>(task: () => Promise<T>): Promise<T> {
    pendingCount.value += 1
    try {
      return await task()
    } finally {
      pendingCount.value -= 1
    }
  }

  return { loading, run }
}