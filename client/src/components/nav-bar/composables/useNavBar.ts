import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

/** 顶栏：登出逻辑。 */
export function useNavBar() {
  const userStore = useUserStore()
  const router = useRouter()

  const logout = async () => {
    if (!userStore.user) return
    await userStore.logout()
    ElMessage.success('登出成功')
    router.push('/login')
  }

  return { logout }
}
