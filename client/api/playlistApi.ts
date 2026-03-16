import { ElMessage } from 'element-plus'
import {
  IAddSongToPlaylist,
  ICreatePlaylist,
  IDeletePlaylist,
  IGetMyPlaylists,
  IGetPlaylistById,
  ILikePlaylist,
  IRemoveSongFromPlaylist,
  IUnlikePlaylist,
  IAxiosRes,
} from '../type'
import request from './axios'

const createPlaylist = async (formData: any) => {
  const res = await request.post<any, IAxiosRes<any>>('api/playlists', formData)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as ICreatePlaylist
  }

  return {
    success: true,
    message: res.message,
    playlist_id: res.data.playlist_id,
  }
}

const deletePlaylist = async (playlistId: number) => {
  const res = await request.delete<any, IAxiosRes<any>>(`api/playlists/${playlistId}`)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IDeletePlaylist
  }

  return {
    success: true,
    message: res.message,
  }
}

const likePlaylist = async (playlistId: number) => {
  const res = await request.post<any, IAxiosRes<any>>(`api/playlists/${playlistId}/likes`)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as ILikePlaylist
  }

  return {
    success: true,
    message: res.message,
  }
}

const unlikePlaylist = async (playlistId: number) => {
  const res = await request.delete<any, IAxiosRes<any>>(`api/playlists/${playlistId}/likes`)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IUnlikePlaylist
  }

  return {
    success: true,
    message: res.message,
  }
}

const getMyPlaylists = async (userId: number) => {
  const res = await request.get<any, IAxiosRes<any>>('api/playlists', {
    params: { userId },
  })

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IGetMyPlaylists
  }

  return {
    success: true,
    message: res.message,
    playlists: res.data.playlists,
  }
}

const getPlaylistById = async (playlistId: number) => {
  const res = await request.get<any, IAxiosRes<any>>(`api/playlists/${playlistId}`)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IGetPlaylistById
  }

  return {
    success: true,
    message: res.message,
    playlist: res.data.playlist,
    songs: res.data.songs,
  }
}

const addSongToPlaylist = async (playlistId: number, songId: number) => {
  const res = await request.post<any, IAxiosRes<any>>(`api/playlists/${playlistId}/songs/${songId}`)

  if (!res.success) {
    ElMessage.error(res.message || '添加歌曲到歌单时出错')
    return {
      success: false,
      message: res.message,
    } as IAddSongToPlaylist
  }

  return {
    success: true,
    message: res.message,
    song_position: res.data.song_position,
  }
}

const removeSongFromPlaylist = async (playlistId: number, songId: number) => {
  const res = await request.delete<any, IAxiosRes<any>>(
    `api/playlists/${playlistId}/songs/${songId}`,
  )

  if (!res.success) {
    ElMessage.error(res.message || '从歌单删除歌曲时出错')
    return {
      success: false,
      message: res.message,
    } as IRemoveSongFromPlaylist
  }

  return {
    success: true,
    message: res.message,
  }
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
}
