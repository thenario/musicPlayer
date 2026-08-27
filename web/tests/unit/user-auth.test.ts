import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'

const router = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
const userApi = vi.hoisted(() => ({
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn(),
  getUserCover: vi.fn(),
  authUser: vi.fn(),
}))

vi.mock('vue-router', () => ({ useRouter: () => router }))
vi.mock('@/api/user-api', () => ({ userApi }))

import { createUserAuth } from '@/composables/user/use-user-auth'

const context = () => ({
  user: ref(null),
  isAuthenticated: ref(false),
  userCoverUrl: ref<string | undefined>(),
  clearSession: vi.fn(),
})

beforeEach(() => vi.resetAllMocks())

describe('createUserAuth', () => {
  it('logs in, updates state, and fetches the cover URL', async () => {
    const ctx = context()
    userApi.login.mockResolvedValue({ success: true, message: 'ok', user: { user_id: 1 } })
    userApi.getUserCover.mockResolvedValue({ user_cover_url: '/cover.jpg' })
    const auth = createUserAuth(ctx)

    await expect(auth.login('Alice', 'password')).resolves.toEqual({ success: true, message: 'ok' })
    expect(ctx.user.value).toEqual({ user_id: 1 })
    expect(ctx.isAuthenticated.value).toBe(true)

    await vi.waitFor(() => expect(ctx.userCoverUrl.value).toBe('/cover.jpg'))
    expect(router.push).toHaveBeenCalledWith({ name: 'login' })
  })

  it('returns a stable error result and always clears session on logout', async () => {
    const ctx = context()
    userApi.logout.mockRejectedValue(new Error('offline'))
    const auth = createUserAuth(ctx)

    await expect(auth.logout()).resolves.toEqual({ success: false, message: 'offline' })
    expect(ctx.clearSession).toHaveBeenCalledOnce()
    expect(router.replace).not.toHaveBeenCalled()
  })

  it('maps registration and authentication responses', async () => {
    const ctx = context()
    userApi.register.mockResolvedValue({ success: true, message: 'registered' })
    userApi.authUser.mockResolvedValue({ success: true, message: 'authenticated' })
    const auth = createUserAuth(ctx)

    await expect(auth.register({ user_name: 'Alice', user_email: 'a@example.com', password: 'password' }))
      .resolves.toEqual({ success: true, message: 'registered' })
    await expect(auth.auth()).resolves.toEqual({ success: true, message: 'authenticated' })
  })
})
