import type { IAxiosRes, IGetMyPlaylists, IGetPlaylistById } from '@/types'
import request from './axios'

//创建歌单
const createPlaylist = async (formData: any) => {
  const res = await request.post<any, IAxiosRes<any>>('/playlists', formData)

  return {
    success: true,
    message: res.message,
    playlist_id: res.data.playlist_id,
  }
}

//编辑歌单
const editPlaylistDetails = async (formData: any) => {
  const res = await request.patch<any, IAxiosRes<any>>('/playlists', formData)

  return {
    success: true,
    message: res.message,
  }
}

//删除歌单
const deletePlaylist = async (playlistId: number | string) => {
  const res = await request.delete<any, IAxiosRes<any>>(`/playlists/${playlistId}`)

  return {
    success: true,
    message: res.message,
  }
}

//点赞歌单
const likePlaylist = async (playlistId: number | string) => {
  const res = await request.post<any, IAxiosRes<any>>(`/playlists/${playlistId}/likes`)

  return {
    success: true,
    message: res.message,
  }
}

//取消歌单的点赞
const unlikePlaylist = async (playlistId: number | string) => {
  const res = await request.delete<any, IAxiosRes<any>>(`/playlists/${playlistId}/unlikes`)

  return {
    success: true,
    message: res.message,
  }
}

//获取用户本人的歌单列表
const getMyPlaylists = async (): Promise<IGetMyPlaylists> => {
  const res = await request.get<any, IAxiosRes<any>>(`/playlists`, {})

  return {
    success: true,
    message: res.message,
    playlists: res.data.playlists,
  }
}

//根据id获取歌单信息
const getPlaylistById = async (playlistId: number | string): Promise<IGetPlaylistById> => {
  const res = await request.get<any, IAxiosRes<any>>(`/playlists/${playlistId}`)

  return {
    success: true,
    message: res.message,
    playlist: res.data.playlist,
    songs: res.data.songs,
    is_liked: res.data.is_liked,
  }
}

//加入歌曲到歌单
const addSongToPlaylist = async (playlistId: number | string, songId: number | string) => {
  const res = await request.post<any, IAxiosRes<any>>(`/playlists/${playlistId}/songs/${songId}`)

  return {
    success: true,
    message: res.message,
    song_position: res.data.song_position,
  }
}

//从歌单移除歌曲
const removeSongFromPlaylist = async (playlistId: number | string, songId: number | string) => {
  const res = await request.delete<any, IAxiosRes<any>>(`playlists/${playlistId}/songs/${songId}`)

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
