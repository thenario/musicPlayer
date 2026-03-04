import { IGetSongs, IUploadSong } from '../type'
import request from './axios'

const getSongs = async (search_page: number, searchKeyword: string) => {
  try {
    const res = await request.get<any, IGetSongs>('api/songs', {
      params: { search_page: search_page, searchKeyword: searchKeyword },
    })
    return {
      success: res.success,
      messsage: res.message,
      songs: res.songs,
      pagination: res.pagination,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.data?.message,
    }
  }
}
const uploadSong = async (uploaFormdata: any) => {
  try {
    const res = await request.post<any, IUploadSong>('api/songs/upload', uploaFormdata)
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.ressponse?.data?.message,
    }
  }
}

export const songApi = {
  getSongs,
  uploadSong,
}
