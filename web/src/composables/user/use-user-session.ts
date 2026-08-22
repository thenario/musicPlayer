import type { Ref } from 'vue'
import type { IUser } from '@/types'
import { tokenStorage, userStorage } from '@/utils/storage'

interface UserSessionContext {
  user: Ref<IUser | null>
  isAuthenticated: Ref<boolean>
  userCoverUrl: Ref<string | undefined>
}

/** Owns the client-side session state and persistent storage cleanup. */
export function createUserSession(ctx: UserSessionContext) {
  const clearSession = () => {
    tokenStorage.remove()
    userStorage.remove()
    ctx.user.value = null
    ctx.userCoverUrl.value = undefined
    ctx.isAuthenticated.value = false
  }

  return { clearSession }
}
