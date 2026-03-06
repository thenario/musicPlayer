import { IGetSongs, IUploadSong, IAxiosRes } from '../type'
import request from './axios'

const getSongs = async (search_page: number, searchKeyword: string) => {
  const res = await request.get<any, IAxiosRes<any>>('api/songs', {
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

const uploadSong = async (uploaFormdata: any) => {
  const res = await request.post<any, IAxiosRes<any>>('api/songs', uploaFormdata)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IUploadSong
  }

  return {
    success: true,
    message: res.message,
  }
}

export const songApi = {
  getSongs,
  uploadSong,
}
