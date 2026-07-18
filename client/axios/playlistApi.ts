import type {
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

//创建歌单
const createPlaylist = async (formData: any) => {
  const res = await request.post<any, IAxiosRes<any>>('/playlists', formData)

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

//编辑歌单
const editPlaylistDetails = async (formData: any) => {
  const res = await request.patch<any, IAxiosRes<any>>('/playlists', formData)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as ICreatePlaylist
  }

  return {
    success: true,
    message: res.message,
  }
}

//删除歌单
const deletePlaylist = async (playlistId: number) => {
  const res = await request.delete<any, IAxiosRes<any>>(`/playlists/${playlistId}`)

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

//点赞歌单
const likePlaylist = async (playlistId: number) => {
  const res = await request.post<any, IAxiosRes<any>>(`/playlists/${playlistId}/likes`)

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

//取消歌单的点赞
const unlikePlaylist = async (playlistId: number) => {
  const res = await request.delete<any, IAxiosRes<any>>(`/playlists/${playlistId}/unlikes`)

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

//获取用户本人的歌单列表
const getMyPlaylists = async () => {
  const res = await request.get<any, IAxiosRes<any>>(`/playlists`, {})

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

//根据id获取歌单信息
const getPlaylistById = async (playlistId: number) => {
  const res = await request.get<any, IAxiosRes<any>>(`/playlists/${playlistId}`)

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
    is_liked: res.data.is_liked,
  }
}

//加入歌曲到歌单
const addSongToPlaylist = async (playlistId: number, songId: number) => {
  const res = await request.post<any, IAxiosRes<any>>(`/playlists/${playlistId}/songs/${songId}`)

  if (!res.success) {
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

//从歌单移除歌曲
const removeSongFromPlaylist = async (playlistId: number, songId: number) => {
  const res = await request.delete<any, IAxiosRes<any>>(`playlists/${playlistId}/songs/${songId}`)

  if (!res.success) {
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
  editPlaylistDetails,
}
