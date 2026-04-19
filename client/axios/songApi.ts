import type { IGetSongs, IUploadSong, IAxiosRes } from '../type'
import request from './axios'

const getSongs = async (search_page: number, searchKeyword: string) => {
  const res = await request.get<any, IAxiosRes<any>>('/songs', {
    params: { page: search_page, keyword: searchKeyword },
  })

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IGetSongs
  }

  return {
    success: true,
    message: res.message,
    songs: res.data.songs,
    pagination: res.data.pagination,
  }
}

export const uploadSong = async (
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

export const getLyrics = async (songId: number) => {
  const res = await request.get<any, any>(`/songs/${songId}/lyrics`)
  if (!res.success) {
    return {
      success: false,
      message: res.message,
    }
  }

  return {
    success: true,
    lyrics: res.data.lyrics,
    t_lyrics: res.data.t_lyrics,
  }
}

export const getUserUploadSongs = async (page: number, size: number) => {
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

export const EditUserUploadSongs = async (formdata: FormData, song_id: number) => {
  const res = await request.patch<any, any>(`/songs/my-uploads/${song_id}`, formdata)

  return { success: res.success, message: res.message }
}

export const songApi = {
  getSongs,
  uploadSong,
  getLyrics,
  getUserUploadSongs,
  EditUserUploadSongs,
}
