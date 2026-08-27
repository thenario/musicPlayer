import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { createSongEditingState } from '@/composables/song/use-song-editing'
import { createUserSession } from '@/composables/user/use-user-session'

vi.mock('@/utils/storage', () => ({
  tokenStorage: { remove: vi.fn() },
  userStorage: { remove: vi.fn() },
}))

import { tokenStorage, userStorage } from '@/utils/storage'

describe('createSongEditingState', () => {
  it('stores the currently edited song', () => {
    const state = createSongEditingState()
    const song = { song_id: 1, song_title: 'Song', song_cover_url: null }

    state.setEditingSong(song)

    expect(state.currentEditingSong.value).toEqual(song)
  })
})

describe('createUserSession', () => {
  beforeEach(() => vi.clearAllMocks())

  it('clears persisted and reactive session state', () => {
    const user = ref({ user_id: 1, user_name: 'Alice', user_email: 'a@example.com' })
    const isAuthenticated = ref(true)
    const userCoverUrl = ref<string | undefined>('/cover.jpg')
    const session = createUserSession({ user, isAuthenticated, userCoverUrl })

    session.clearSession()

    expect(tokenStorage.remove).toHaveBeenCalledOnce()
    expect(userStorage.remove).toHaveBeenCalledOnce()
    expect(user.value).toBeNull()
    expect(userCoverUrl.value).toBeUndefined()
    expect(isAuthenticated.value).toBe(false)
  })
})
