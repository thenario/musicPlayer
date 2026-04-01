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
  console.log('=== [DEBUG] 1. 进入 getLyrics 函数, ID:', songId)

  try {
    const res = await request.get<any, any>(`/songs/${songId}/lyrics`)

    console.log('=== [DEBUG] 2. await 请求结束, 收到 res:', res)

    if (!res.success) {
      console.log('=== [DEBUG] 3. 业务逻辑失败:', res.message)
      return { success: false, message: res.message }
    }

    return {
      success: true,
      lyrics: res.data.lyrics,
      t_lyrics: res.data.t_lyrics,
    }
  } catch (err: any) {
    console.error('=== [DEBUG] 4. 函数内部捕获到崩坏:', err)
    throw err // 继续抛出给组件
  }
}

export const songApi = {
  getSongs,
  uploadSong,
  getLyrics,
}
