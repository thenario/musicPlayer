import { ref } from 'vue'

/** 异步任务的 loading 包装器：run 期间 loading 为 true，finally 里复位。 */
export function useAsyncTask() {
  const loading = ref(false)

  async function run<T>(task: () => Promise<T>): Promise<T> {
    loading.value = true
    try {
      return await task()
    } finally {
      loading.value = false
    }
  }

  return { loading, run }
}
