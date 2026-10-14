import { useUserStore } from '@/stores/user'

/** 顶栏：登出逻辑。 */
export function useNavBar() {
  const userStore = useUserStore()

  const logout = async () => {
    return userStore.logout()
  }

  return { logout }
}
