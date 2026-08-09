import type { IUploadSong, IAxiosRes } from '@/types'
import request from './axios'

//获取歌曲，涵盖查询歌曲的功能
const getSongs = async (search_page: number, searchKeyword: string) => {
  const res = await request.get<any, IAxiosRes<any>>('/songs', {
    params: { page: search_page, keyword: searchKeyword },
  })

  return {
    success: true,
    message: res.message,
    songs: res.data.songs,
    pagination: res.data.pagination,
  }
}

//上传歌曲
const uploadSong = async (
  formData: FormData,
  onProgress?: (progressEvent: any) => void,
): Promise<IUploadSong> => {
  const res = await request.post<any, IAxiosRes<any>>('/songs', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: onProgress,
  })

  return {
    success: true,
    message: res.message,
  }
}

//获取歌词
const getLyrics = async (songId: number) => {
  const res = await request.get<any, any>(`/songs/${songId}/lyrics`)

  return {
    success: true,
    lyrics: res.data.lyrics,
    t_lyrics: res.data.t_lyrics,
  }
}

//获取用户的上传歌曲
const getUserUploadSongs = async (page: number, size: number) => {
  const res = await request.get<any, any>(`/songs/my-uploads`, {
    params: {
      page: page,
      size: size,
    },
  })

  return {
    success: res.success,
    songs: res.data.records,
    message: res.message,
    total: res.data.total,
  }
}

//编辑上传的歌曲的信息
const editUserUploadSongs = async (formdata: FormData, song_id: number) => {
  const res = await request.patch<any, any>(`/songs/my-uploads/${song_id}`, formdata)

  return { success: res.success, message: res.message }
}

export const songApi = {
  getSongs,
  uploadSong,
  getLyrics,
  getUserUploadSongs,
  editUserUploadSongs,
}
