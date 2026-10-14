import axios, { AxiosError, CanceledError } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const router = vi.hoisted(() => ({
  currentRoute: { value: { path: '/songs', fullPath: '/songs?page=2' } },
  replace: vi.fn().mockResolvedValue(undefined),
}))
const clearSession = vi.hoisted(() => vi.fn())
vi.mock('@/router', () => ({ router }))
vi.mock('@/stores/user', () => ({ useUserStore: () => ({ clearSession }) }))

import request from '@/api/axios'
import { tokenStorage } from '@/utils/storage'
import { requireSuccess, toActionFailure } from '@/utils/action-result'

beforeEach(() => {
  vi.clearAllMocks()
  localStorage.clear()
  tokenStorage.set('current-token')
  clearSession.mockImplementation(() => tokenStorage.remove())
})

const rejectWithStatus = (status: number) => {
  request.defaults.adapter = async (config) => {
    throw new AxiosError('request failed', 'ERR_BAD_RESPONSE', config, undefined, {
      data: { code: status, message: `server-${status}` }, status,
      statusText: 'error', headers: {}, config,
    })
  }
}

describe('request error boundaries', () => {
  it('preserves a session for 503 and returns the server message', async () => {
    rejectWithStatus(503)
    await expect(request.get('/songs')).rejects.toMatchObject({
      success: false, code: 503, message: 'server-503',
    })
    expect(clearSession).not.toHaveBeenCalled()
    expect(router.replace).not.toHaveBeenCalled()
    expect(tokenStorage.get()).toBe('current-token')
  })

  it('clears a rejected session for protected 401 requests', async () => {
    rejectWithStatus(401)
    await expect(request.get('/users/auth')).rejects.toMatchObject({ code: 401 })
    expect(clearSession).toHaveBeenCalledOnce()
    expect(router.replace).toHaveBeenCalledWith({ path: '/login', query: { redirect: '/songs?page=2' } })
  })

  it('does not treat a wrong login password as expiry of the current session', async () => {
    rejectWithStatus(401)
    await expect(request.post('/users/login', {})).rejects.toMatchObject({ code: 401 })
    expect(clearSession).not.toHaveBeenCalled()
    expect(router.replace).not.toHaveBeenCalled()
  })

  it('keeps cancellation distinguishable from a business failure', async () => {
    request.defaults.adapter = async () => { throw new CanceledError('canceled') }
    try {
      await request.get('/songs')
      throw new Error('expected cancellation')
    } catch (error) {
      expect(axios.isCancel(error)).toBe(true)
      expect(toActionFailure(error)).toMatchObject({ success: false, canceled: true })
    }
  })

  it('rethrows a failed child result with its status and stage message', () => {
    expect(() => requireSuccess({ success: false, message: 'offline', code: 503 }, '队列加载失败'))
      .toThrow('队列加载失败：offline')
    try {
      requireSuccess({ success: false, message: 'offline', code: 503 })
    } catch (error) {
      expect(toActionFailure(error)).toEqual({ success: false, message: 'offline', code: 503 })
    }
  })
})
