import type {
  IAxiosRes,
  IGetMyPlaylists,
  IGetPlaylistById,
} from '@/types'
import request from './axios'

type PlaylistId = number | string

const createPlaylist = async (formData: FormData) => {
  const res = await request.post<never, IAxiosRes<{ playlist_id: PlaylistId }>>('/playlists', formData)
  return { success: true, message: res.message, playlist_id: res.data.playlist_id }
}

const editPlaylistDetails = async (formData: FormData) => {
  const res = await request.patch<never, IAxiosRes<null>>('/playlists', formData)
  return { success: true, message: res.message }
}

const deletePlaylist = async (playlistId: PlaylistId) => {
  const res = await request.delete<never, IAxiosRes<null>>(`/playlists/${playlistId}`)
  return { success: true, message: res.message }
}

const likePlaylist = async (playlistId: PlaylistId) => {
  const res = await request.post<never, IAxiosRes<null>>(`/playlists/${playlistId}/likes`)
  return { success: true, message: res.message }
}

const unlikePlaylist = async (playlistId: PlaylistId) => {
  const res = await request.delete<never, IAxiosRes<null>>(`/playlists/${playlistId}/unlikes`)
  return { success: true, message: res.message }
}

const getMyPlaylists = async (): Promise<IGetMyPlaylists> => {
  const res = await request.get<never, IAxiosRes<{ playlists: IGetMyPlaylists['playlists'] }>>('/playlists')
  return { success: true, message: res.message, playlists: res.data.playlists }
}

const getPlaylistById = async (playlistId: PlaylistId): Promise<IGetPlaylistById> => {
  const res = await request.get<never, IAxiosRes<{
    playlist: IGetPlaylistById['playlist']
    songs: IGetPlaylistById['songs']
    is_liked: boolean
  }>>(`/playlists/${playlistId}`)
  return { success: true, message: res.message, ...res.data }
}

const addSongToPlaylist = async (playlistId: PlaylistId, songId: PlaylistId) => {
  const res = await request.post<never, IAxiosRes<{ song_position: number }>>(`/playlists/${playlistId}/songs/${songId}`)
  return { success: true, message: res.message, song_position: res.data.song_position }
}

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