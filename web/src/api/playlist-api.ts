import type {
  IAxiosRes,
  IGetMyPlaylists,
  IGetPlaylistById,
} from '@/types'
import request from './axios'

type PlaylistId = number | string

/**
 * 歌单模块接口。
 *
 * 泛型约定：响应拦截器已把 AxiosResponse 改写为 IAxiosRes（见 axios.ts），
 * 故第二泛型统一声明「返回包装类型」IAxiosRes<响应体>；无请求体时第一泛型传 never。
 */

/** 创建歌单（multipart，含封面）。 */
const createPlaylist = async (formData: FormData) => {
  const res = await request.post<never, IAxiosRes<{ playlist_id: PlaylistId }>>('/playlists', formData)
  return { success: true, message: res.message, playlist_id: res.data.playlist_id }
}

/** 编辑歌单详情（multipart，含封面）。 */
const editPlaylistDetails = async (formData: FormData) => {
  const res = await request.patch<never, IAxiosRes<null>>('/playlists', formData)
  return { success: true, message: res.message }
}

/** 删除歌单。 */
const deletePlaylist = async (playlistId: PlaylistId) => {
  const res = await request.delete<never, IAxiosRes<null>>(`/playlists/${playlistId}`)
  return { success: true, message: res.message }
}

/** 收藏歌单。 */
const likePlaylist = async (playlistId: PlaylistId) => {
  const res = await request.post<never, IAxiosRes<null>>(`/playlists/${playlistId}/likes`)
  return { success: true, message: res.message }
}

/** 取消收藏歌单。 */
const unlikePlaylist = async (playlistId: PlaylistId) => {
  const res = await request.delete<never, IAxiosRes<null>>(`/playlists/${playlistId}/unlikes`)
  return { success: true, message: res.message }
}

/** 获取我的歌单列表。 */
const getMyPlaylists = async (): Promise<IGetMyPlaylists> => {
  const res = await request.get<never, IAxiosRes<{ playlists: IGetMyPlaylists['playlists'] }>>('/playlists')
  return { success: true, message: res.message, playlists: res.data.playlists }
}

/** 获取歌单详情（含歌曲列表与是否已收藏）。 */
const getPlaylistById = async (playlistId: PlaylistId): Promise<IGetPlaylistById> => {
  const res = await request.get<never, IAxiosRes<{
    playlist: IGetPlaylistById['playlist']
    songs: IGetPlaylistById['songs']
    is_liked: boolean
  }>>(`/playlists/${playlistId}`)
  return { success: true, message: res.message, ...res.data }
}

/** 向歌单添加歌曲，返回歌曲在歌单中的排序位置。 */
const addSongToPlaylist = async (playlistId: PlaylistId, songId: PlaylistId) => {
  const res = await request.post<never, IAxiosRes<{ song_position: number }>>(`/playlists/${playlistId}/songs/${songId}`)
  return { success: true, message: res.message, song_position: res.data.song_position }
}

/** 从歌单移除歌曲。 */
const removeSongFromPlaylist = async (playlistId: PlaylistId, songId: PlaylistId) => {
  const res = await request.delete<never, IAxiosRes<null>>(`/playlists/${playlistId}/songs/${songId}`)
  return { success: true, message: res.message }
}

export const playlistApi = {
  getMyPlaylists,
  getPlaylistById,
  createPlaylist,
  deletePlaylist,
  likePlaylist,
  unlikePlaylist,
  removeSongFromPlaylist,
  addSongToPlaylist,
  editPlaylistDetails,
}
