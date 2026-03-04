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
} from '../type'
import request from './axios'

const createPlaylist = async (formData: any) => {
  try {
    const res = await request.post<any, ICreatePlaylist>('api/playlists/create')
    return {
      success: res.success,
      message: res.message,
      playlist_id: res.playlist_id,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.reponse?.data?.message,
    }
  }
}

const deletePlaylist = async (playlistId: number) => {
  try {
    const res = await request.delete<any, IDeletePlaylist>('api/playlists/id/delete', {
      params: { playlistId },
    })
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.reponse?.data?.message,
    }
  }
}

const likePlaylist = async (playlistId: number) => {
  try {
    const res = await request.post<any, ILikePlaylist>('api/playlists/id/like', {
      playlistId,
    })
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.reponse?.data?.message,
    }
  }
}

const unlikePlaylist = async (playlistId: number) => {
  try {
    const res = await request.post<any, IUnlikePlaylist>('api/playlists/id/unlike', {
      playlistId,
    })
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.reponse?.data?.message,
    }
  }
}

const getMyPlaylists = async (userId: number) => {
  try {
    const res = await request.post<any, IGetMyPlaylists>('api/playlists', {
      userId,
    })
    return {
      success: res.success,
      message: res.message,
      playlists: res.playlits,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.reponse?.data?.message,
    }
  }
}

const getPlaylistById = async (playlistId: number) => {
  try {
    const res = await request.post<any, IGetPlaylistById>('api/playlists/id', {
      playlistId,
    })
    return {
      success: res.success,
      message: res.message,
      playlist: res.playlist,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.reponse?.data?.message,
    }
  }
}

const addSongToPlaylist = async (playlistId: number, songId: number) => {
  const res = await request.post<any, IAddSongToPlaylist>('api/playlists/id/addSong', {
    playlistId,
    songId,
  })
  if (!res.success) {
    ElMessage.error('添加歌曲到歌单时出错')
  }
  return {
    success: res.success,
    message: res.message,
    song_position: res.song_position,
  }
}

const removeSongFromPlaylist = async (playlistId: number, songId: number) => {
  const res = await request.post<any, IRemoveSongFromPlaylist>('api/playlists/id/addSong', {
    playlistId,
    songId,
  })
  if (!res.success) {
    ElMessage.error('从歌单删除歌曲时出错')
  }
  return {
    success: res.success,
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
