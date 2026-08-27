import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  patch: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
}))
const storage = vi.hoisted(() => ({
  tokenStorage: { set: vi.fn(), remove: vi.fn() },
  userStorage: { set: vi.fn(), remove: vi.fn() },
}))
const encryptPassword = vi.hoisted(() => vi.fn().mockResolvedValue('encrypted'))

vi.mock('@/api/axios', () => ({ default: request }))
vi.mock('@/utils/storage', () => storage)
vi.mock('@/utils/crypto', () => ({ encryptPassword }))

import { playlistApi } from '@/api/playlist-api'
import { queueApi } from '@/api/queue-api'
import { songApi } from '@/api/song-api'
import { userApi } from '@/api/user-api'

const response = (data: unknown = null) => ({ success: true, message: 'ok', code: 200, data })

beforeEach(() => {
  vi.resetAllMocks()
  encryptPassword.mockResolvedValue('encrypted')
})

describe('playlistApi', () => {
  it('maps playlist CRUD and like requests', async () => {
    request.post.mockResolvedValueOnce(response({ playlist_id: 3 }))
      .mockResolvedValueOnce(response())
      .mockResolvedValueOnce(response())
    request.patch.mockResolvedValue(response())
    request.delete.mockResolvedValue(response())

    const form = new FormData()
    expect(await playlistApi.createPlaylist(form)).toEqual({ success: true, message: 'ok', playlist_id: 3 })
    expect(await playlistApi.editPlaylistDetails(form)).toEqual({ success: true, message: 'ok' })
    expect(await playlistApi.deletePlaylist(3)).toEqual({ success: true, message: 'ok' })
    expect(await playlistApi.likePlaylist(3)).toEqual({ success: true, message: 'ok' })
    expect(await playlistApi.unlikePlaylist(3)).toEqual({ success: true, message: 'ok' })
    expect(request.patch).toHaveBeenCalledWith('/playlists', form)
    expect(request.delete).toHaveBeenCalledWith('/playlists/3/unlikes')
  })

  it('maps playlist reads and song mutations', async () => {
    request.get.mockResolvedValueOnce(response({ playlists: [] }))
      .mockResolvedValueOnce(response({ playlist: { playlist_id: 1 }, songs: [], is_liked: true }))
    request.post.mockResolvedValue(response({ song_position: 4 }))
    request.delete.mockResolvedValue(response())

    expect((await playlistApi.getMyPlaylists()).playlists).toEqual([])
    expect((await playlistApi.getPlaylistById(1)).is_liked).toBe(true)
    expect((await playlistApi.addSongToPlaylist(1, 2)).song_position).toBe(4)
    await playlistApi.removeSongFromPlaylist(1, 2)
    expect(request.post).toHaveBeenCalledWith('/playlists/1/songs/2')
    expect(request.delete).toHaveBeenCalledWith('/playlists/1/songs/2')
  })
})

describe('queueApi', () => {
  it('maps queue reads and mutations to endpoint payloads', async () => {
    request.get.mockResolvedValueOnce(response({ queues: [] }))
      .mockResolvedValueOnce(response({ queue: {} }))
      .mockResolvedValueOnce(response({ queue: {}, queue_state: {} }))
    request.put.mockResolvedValue(response())
    request.delete.mockResolvedValueOnce(response({ was_active: true, new_queue_id: 9 }))
      .mockResolvedValue(response())
    request.patch.mockResolvedValue(response())
    request.post.mockResolvedValueOnce(response({ queue_id: 8, song_count: 2 }))
      .mockResolvedValue(response({ queue_id: 8, queue_item_position: 2, queue_item_id: 7 }))

    expect((await queueApi.getMyQueues()).queues).toEqual([])
    await queueApi.getQueueById(2)
    await queueApi.getCurrentQueue()
    await queueApi.alterQueueToCurrent(2)
    expect((await queueApi.deleteQueue(2)).data.was_active).toBe(true)
    expect((await queueApi.createQueueFromPlaylist(4)).song_count).toBe(2)
    expect((await queueApi.addSongToQueue(5, 2, true)).data.queue_item_id).toBe(7)
    await queueApi.updateCurrentQueueState({} as never)
    await queueApi.reorderQueue([5], 2)
    await queueApi.setPlayMode(2, 'shuffle')
    await queueApi.removeSongFromQueue(2, 7)
    await queueApi.clearQueue(2)

    expect(request.post).toHaveBeenCalledWith('/queues', { source: 'playlist', playlist_id: 4 })
    expect(request.patch).toHaveBeenCalledWith('/queues/2/play-mode', { play_mode: 'shuffle' })
  })
})

describe('songApi', () => {
  it('maps song search, lyrics, uploads, and edits', async () => {
    request.get.mockResolvedValueOnce(response({ songs: [], pagination: {} }))
      .mockResolvedValueOnce(response({ lyrics: 'lrc', t_lyrics: '译文' }))
      .mockResolvedValueOnce(response({ records: [], total: 0 }))
      .mockResolvedValueOnce(response({ song_id: 1 }))
    request.post.mockResolvedValue(response())
    request.patch.mockResolvedValue(response())
    const form = new FormData()

    expect((await songApi.getSongs(1, 'test')).songs).toEqual([])
    expect((await songApi.getLyrics(1)).lyrics).toBe('lrc')
    expect((await songApi.getUserUploadSongs(1, 10)).total).toBe(0)
    expect((await songApi.getUserUploadSong(1)).song).toEqual({ song_id: 1 })
    await songApi.uploadSong(form)
    await songApi.editUserUploadSongs(form, 1)

    expect(request.get).toHaveBeenCalledWith('/songs/1/lyrics', { silent: true })
    expect(request.patch).toHaveBeenCalledWith('/songs/my-uploads/1', form, { timeout: 300_000 })
  })
})

describe('userApi', () => {
  it('encrypts credentials and persists login data', async () => {
    request.post.mockResolvedValueOnce(response({ user: { user_id: 1 }, token: 'token' }))
    const result = await userApi.login('Alice', 'password')

    expect(result.token).toBe('token')
    expect(request.post).toHaveBeenCalledWith('/users/login', {
      user_name: 'Alice', password: 'encrypted', user_email: '',
    })
    expect(storage.userStorage.set).toHaveBeenCalledWith({ user_id: 1 })
    expect(storage.tokenStorage.set).toHaveBeenCalledWith('token')
  })

  it('registers, logs out, and maps account endpoints', async () => {
    request.post.mockResolvedValue(response())
    request.get.mockResolvedValueOnce(response({ user_cover_url: '/cover.jpg' }))
      .mockResolvedValueOnce(response())
    request.patch.mockResolvedValue(response({ user_name: 'Alice', user_cover_url: '/new.jpg' }))

    await userApi.register({ user_name: 'Alice', password: 'password', user_email: 'a@example.com' })
    await userApi.logout()
    expect((await userApi.getUserCover()).user_cover_url).toBe('/cover.jpg')
    expect((await userApi.editUserProfile(new FormData())).user_name).toBe('Alice')
    expect((await userApi.authUser()).success).toBe(true)
    expect(storage.userStorage.remove).toHaveBeenCalledOnce()
    expect(storage.tokenStorage.remove).toHaveBeenCalledOnce()
  })
})
