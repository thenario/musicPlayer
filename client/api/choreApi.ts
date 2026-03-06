import { IGetStatics, IAxiosRes } from '../type'
import request from './axios'

const getStatics = async () => {
  const res = await request.get<any, IAxiosRes<any>>('api/stats')

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IGetStatics
  }

  return {
    success: true,
    message: res.message,
    total_songs: res.data.total_songs,
    total_users: res.data.total_users,
    online_users: res.data.online_users,
    popular_songs: res.data.popular_songs,
  }
}

export const choreApi = { getStatics }
