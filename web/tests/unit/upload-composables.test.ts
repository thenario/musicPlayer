import { beforeEach, describe, expect, it, vi } from 'vitest'

const uploadSong = vi.hoisted(() => vi.fn())
const editUserUploadSongs = vi.hoisted(() => vi.fn())
const getLyrics = vi.hoisted(() => vi.fn())
vi.mock('@/api/song-api', () => ({ songApi: { uploadSong, editUserUploadSongs, getLyrics } }))

import { useSongUpload } from '@/views/song/upload/composables/use-song-upload'
import { useEditUpload } from '@/views/song/edit-upload/composables/use-edit-upload'

const message = vi.hoisted(() => ({ warning: vi.fn(), success: vi.fn() }))
Object.defineProperty(globalThis, 'ElMessage', { configurable: true, value: message })

beforeEach(() => {
  vi.resetAllMocks()
})

describe('useSongUpload', () => {
  it('rejects unsupported audio and derives metadata from a valid filename', () => {
    const upload = useSongUpload()

    const invalid = new File(['x'], 'song.txt', { type: 'text/plain' })
    const valid = new File(['x'], 'Title_Artist.mp3', { type: 'audio/mpeg' })
    const input = (file: File) => ({ target: { files: [file] } }) as unknown as Event

    upload.handleAudioSelect(input(invalid))
    expect(upload.audioFile.value).toBeNull()
    expect(message.warning).toHaveBeenCalledWith('不支持的文件格式')

    upload.handleAudioSelect(input(valid))
    expect(upload.audioFile.value).toBe(valid)
    expect(upload.form.value.title).toBe('Title')
    expect(upload.form.value.artist).toBe('Artist')
  })

  it('requires files before submitting and resets its state', async () => {
    const upload = useSongUpload()

    await upload.submit()
    expect(message.warning).toHaveBeenCalledWith('请选择音频文件')

    upload.form.value.title = 'Song'
    upload.reset()
    expect(upload.form.value.title).toBe('')
    expect(upload.audioFile.value).toBeNull()
    expect(upload.coverFile.value).toBeNull()
    expect(upload.uploadProgress.value).toBe(0)
  })
})

describe('useEditUpload', () => {
  it('loads lyrics into the edit form and handles validation failure', async () => {
    const edit = useEditUpload(1)
    getLyrics.mockResolvedValue({ lyrics: 'main', t_lyrics: 'translation' })

    await edit.fetchLyrics()
    expect(edit.formData.lyrics).toBe('main')
    expect(edit.formData.t_lyrics).toBe('translation')
    expect(edit.lyricsLoading.value).toBe(false)
    expect(await edit.save()).toBe(false)
  })

  it('submits a valid edit form and clears submitting state', async () => {
    const edit = useEditUpload(1)
    edit.formRef.value = { validate: vi.fn().mockResolvedValue(undefined) } as never
    edit.formData.song_name = 'Updated'
    edit.formData.lyrics = 'lyrics'
    edit.formData.t_lyrics = 'translated'
    editUserUploadSongs.mockResolvedValue({ success: true })

    await expect(edit.save()).resolves.toBe(true)
    expect(editUserUploadSongs).toHaveBeenCalledWith(expect.any(FormData), 1)
    expect(message.success).toHaveBeenCalledWith('修改成功')
    expect(edit.submitting.value).toBe(false)
  })
})
