import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { IUser } from '@/types'
import { userStorage } from '@/utils/storage'
import { createUserAuth } from '@/composables/user/use-user-auth'
import { createUserSession } from '@/composables/user/use-user-session'

export const useUserStore = defineStore('user', () => {
  const user = ref<IUser | null>(userStorage.get())
  const isAuthenticated = ref(!!user.value)
  const userCoverUrl = ref<string>()

  const session = createUserSession({ user, isAuthenticated, userCoverUrl })
  const auth = createUserAuth({ ...session, user, isAuthenticated, userCoverUrl })

  return { user, isAuthenticated, userCoverUrl, ...auth, ...session }
})
