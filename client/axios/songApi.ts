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

export const songApi = {
  getSongs,
  uploadSong,
}
