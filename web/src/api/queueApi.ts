import type { IQueueState, IAxiosRes, IGetCurrentQueue, IGetMyQueues, IQueue } from '@/types'
import request from './axios'

//获取用户的播放队列
const getMyQueues = async (): Promise<IGetMyQueues> => {
  const res = await request.get<any, IAxiosRes<any>>('/queues', { silent: true })

  return {
    success: true,
    message: res.message,
    queues: res.data.queues,
  }
}

//依据id获取队列信息
const getQueueById = async (
  queueId: number | string,
): Promise<{ success: boolean; message: string; queue: IQueue }> => {
  const res = await request.get<any, IAxiosRes<any>>(`/queues/${queueId}`)

  return {
    success: true,
    message: res.message,
    queue: res.data.queue,
  }
}

//获取用户当前的队列信息
const getCurrentQueue = async (): Promise<IGetCurrentQueue> => {
  const res = await request.get<any, IAxiosRes<any>>('/queues/current', { silent: true })

  return {
    success: true,
    message: res.message,
    queue: res.data.queue,
    queue_state: res.data.queue_state,
  }
}

//更换当前的队列
const alterQueueToCurrent = async (queueId: number | string) => {
  const res = await request.put<any, IAxiosRes<any>>('/queues/player/current-queue', {
    queue_id: queueId,
  })

  return {
    success: true,
    message: res.message,
  }
}

//删除队列
const deleteQueue = async (queueId: number | string) => {
  const res = await request.delete<any, IAxiosRes<any>>(`/queues/${queueId}`)

  return {
    success: true,
    message: res.message,
    data: res.data,
  }
}

//添加歌曲到队列
const addSongToQueue = async (song_id: number | string, queue_id: number | string, mode: boolean) => {
  const res = await request.post<any, IAxiosRes<any>>(`/queues/${queue_id || 0}/songs`, {
    song_id,
    mode, //true下一首播放，false直接播放
  })

  return {
    success: res.success,
    message: res.message,
    data: res.data,
  }
}

//从队列移除歌曲
const removeSongFromQueue = async (queueId: number | string, itemId: number | string) => {
  const res = await request.delete<any, IAxiosRes<any>>(`/queues/${queueId}/songs/${itemId}`)

  return {
    success: true,
    message: res.message,
  }
}

//设置播放模式
const setPlayMode = async (queueId: number | string, play_mode: string) => {
  const res = await request.patch<any, IAxiosRes<any>>(`/queues/${queueId}/play-mode`, {
    play_mode: play_mode,
  })

  return {
    success: true,
    message: res.message,
  }
}

//重排队列
const reorderQueue = async (song_ids: (number | string)[], queue_id: number | string) => {
  const res = await request.patch<any, IAxiosRes<any>>(`/queues/${queue_id}/reorder`, {
    song_ids,
  })

  return {
    success: true,
    message: res.message,
  }
}

//从歌单创建队列，类似于播放全部
const createQueueFromPlaylist = async (playlist_id: number | string) => {
  const res = await request.post<any, IAxiosRes<any>>('/queues', {
    source: 'playlist',
    playlist_id,
  })

  return {
    success: true,
    message: res.message,
    queue_id: res.data.queue_id,
    song_count: res.data.song_count,
  }
}

//更新播放状态
const updateCurrentQueueState = async (stateData: IQueueState) => {
  const res = await request.patch<any, IAxiosRes<any>>('/queues/current/state', {
    stateData,
  })

  return {
    success: true,
    message: res.message,
  }
}
//清空队列
const clearQueue = async (queueId: number | string) => {
  const res = await request.delete<any, IAxiosRes<any>>(`/queues/${queueId}/songs`)

  return {
    success: true,
    message: res.message,
  }
}

export const queueApi = {
  getCurrentQueue,
  getMyQueues,
  getQueueById,
  deleteQueue,
  createQueueFromPlaylist,
  addSongToQueue,
  updateCurrentQueueState,
  reorderQueue,
  setPlayMode,
  alterQueueToCurrent,
  removeSongFromQueue,
  clearQueue,
}
