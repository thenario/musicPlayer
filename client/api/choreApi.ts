import { IGetStatics } from '../type'
import request from './axios'

const getStatics = async () => {
  try {
    const res = await request.get<any, IGetStatics>('api/statics')
    return {
      success: res.success,
      message: res.message,
      total_songs: res.total_songs,
      total_users: res.total_users,
      online_user: res.online_users,
      popular_songs: res.popular_songs,
    }
  } catch (error: any) {
    return {
      success: false,
      message: error.response?.data?.message,
    }
  }
}

export const choreApi = {
  getStatics,
}
