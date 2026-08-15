import type { AxiosProgressEvent } from 'axios'
import type { IAxiosRes, IPagination, ISong, IUploadSong } from '@/types'
import request from './axios'

type SongId = number | string
type LyricsResponse = { lyrics: string; t_lyrics: string }
export type UploadedSong = Pick<ISong, 'song_id' | 'song_title' | 'artist' | 'song_cover_url' | 'song_url'> & {
  date_added: string
}
type UploadPageResponse = { records: UploadedSong[]; total: number }

const getSongs = async (page: number, keyword: string) => {
  const res = await request.get<never, IAxiosRes<{ songs: ISong[]; pagination: IPagination }>>('/songs', {
    params: { page, keyword },
  })
  return { success: true, message: res.message, ...res.data }
}

const uploadSong = async (
  formData: FormData,
  onProgress?: (progressEvent: AxiosProgressEvent) => void,
): Promise<IUploadSong> => {
  const res = await request.post<never, IAxiosRes<null>>('/songs', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: onProgress,
    timeout: 300_000,
  })
  return { success: true, message: res.message }
}

const getLyrics = async (songId: SongId) => {
  const res = await request.get<never, IAxiosRes<LyricsResponse>>(`/songs/${songId}/lyrics`, { silent: true })
  return { success: true, ...res.data }
}

const getUserUploadSongs = async (page: number, size: number) => {
  const res = await request.get<never, IAxiosRes<UploadPageResponse>>('/songs/my-uploads', {
    params: { page, size },
  })
  return { success: res.success, songs: res.data.records, message: res.message, total: res.data.total }
}

const editUserUploadSongs = async (formdata: FormData, songId: SongId) => {
  const res = await request.patch<never, IAxiosRes<null>>(`/songs/my-uploads/${songId}`, formdata)
  return { success: res.success, message: res.message }
}

export const songApi = { getSongs, uploadSong, getLyrics, getUserUploadSongs, editUserUploadSongs }