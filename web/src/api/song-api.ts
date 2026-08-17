import type { AxiosProgressEvent } from 'axios'
import type { IAxiosRes, IPagination, ISong, IUploadSong } from '@/types'
import request from './axios'

type SongId = number | string
type LyricsResponse = { lyrics: string; t_lyrics: string }
export type UploadedSong = Pick<ISong, 'song_id' | 'song_title' | 'artist' | 'song_cover_url' | 'song_url'> & {
  date_added: string
}
type UploadPageResponse = { records: UploadedSong[]; total: number }

/**
 * 歌曲模块接口。
 *
 * 泛型约定：响应拦截器已把 AxiosResponse 改写为 IAxiosRes（见 axios.ts），
 * 故第二泛型统一声明「返回包装类型」IAxiosRes<响应体>；无请求体时第一泛型传 never。
 */

/** 分页搜索歌曲（keyword 匹配歌名/歌手）。 */
const getSongs = async (page: number, keyword: string) => {
  const res = await request.get<never, IAxiosRes<{ songs: ISong[]; pagination: IPagination }>>('/songs', {
    params: { page, keyword },
  })
  return { success: true, message: res.message, ...res.data }
}

/** 上传歌曲：multipart 表单，带可选上传进度回调（超时 5 分钟）。 */
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

/** 获取歌词（silent：失败不集中弹错）。 */
const getLyrics = async (songId: SongId) => {
  const res = await request.get<never, IAxiosRes<LyricsResponse>>(`/songs/${songId}/lyrics`, { silent: true })
  return { success: true, ...res.data }
}

/** 分页获取我上传的歌曲。 */
const getUserUploadSongs = async (page: number, size: number) => {
  const res = await request.get<never, IAxiosRes<UploadPageResponse>>('/songs/my-uploads', {
    params: { page, size },
  })
  return { success: res.success, songs: res.data.records, message: res.message, total: res.data.total }
}

/** 获取我上传的单个歌曲详情。 */
const getUserUploadSong = async (songId: SongId) => {
  const res = await request.get<never, IAxiosRes<UploadedSong>>(`/songs/my-uploads/${songId}`)
  return { success: res.success, song: res.data, message: res.message }
}

/** 编辑我上传的歌曲（multipart，超时 5 分钟）。 */
const editUserUploadSongs = async (formdata: FormData, songId: SongId) => {
  const res = await request.patch<never, IAxiosRes<null>>(`/songs/my-uploads/${songId}`, formdata, { timeout: 300_000 })
  return { success: res.success, message: res.message }
}

export const songApi = { getSongs, uploadSong, getLyrics, getUserUploadSongs, getUserUploadSong, editUserUploadSongs }
