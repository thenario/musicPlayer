import type {
  IAddSongToQueue,
  IAlterQueueTocurrent,
  IClearQueue,
  ICreatQueueFromPlaylist,
  IDeleteQueue,
  IGetCurrentQueue,
  IGetMyQueues,
  IGetQueueById,
  IQueueState,
  IReorderQueue,
  ISetPlayMode,
  IUpdateCurrentQueueState,
  IRemoveSongFromQueue,
  IAxiosRes,
} from '../type'
import request from './axios'

const getMyQueues = async () => {
  const res = await request.get<any, IAxiosRes<any>>('/queues')

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IGetMyQueues
  }

  return {
    success: true,
    message: res.message,
    queues: res.data.queues,
  }
}

const getQueueById = async (queueId: number) => {
  const res = await request.get<any, IAxiosRes<any>>(`/queues/${queueId}`)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IGetQueueById
  }

  return {
    success: true,
    message: res.message,
    queue: res.data.queue,
  }
}

const getCurrentQueue = async () => {
  const res = await request.get<any, IAxiosRes<any>>('/queues/current')

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IGetCurrentQueue
  }

  return {
    success: true,
    message: res.message,
    queue: res.data.queue,
    queue_state: res.data.queue_state,
  }
}

const alterQueueToCurrent = async (queueId: number) => {
  const res = await request.put<any, IAxiosRes<any>>('/queues/player/current-queue', {
    queue_id: queueId,
  })

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IAlterQueueTocurrent
  }

  return {
    success: true,
    message: res.message,
  }
}

const deletQueue = async (queueId: number) => {
  const res = await request.delete<any, IAxiosRes<any>>(`/queues/${queueId}`)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IDeleteQueue
  }

  return {
    success: true,
    message: res.message,
    data: res.data,
  }
}

const addSongToQueue = async (song_id: number, queue_id: number, mode: boolean) => {
  const res = await request.post<any, IAxiosRes<any>>(`/queues/${queue_id || 0}/songs`, {
    song_id,
    mode, //true下一首播放，false直接播放
  })

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IAddSongToQueue
  }

  return {
    success: res.success,
    message: res.message,
    data: res.data,
  }
}

const removeSongFromQueue = async (queueId: number, itemId: number) => {
  const res = await request.delete<any, IAxiosRes<any>>(`/queues/${queueId}/songs/${itemId}`)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IRemoveSongFromQueue
  }

  return {
    success: true,
    message: res.message,
  }
}

const setPlayMode = async (queueId: number, play_mode: string) => {
  const res = await request.patch<any, IAxiosRes<any>>(`/queues/${queueId}/play-mode`, {
    play_mode: play_mode,
  })

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as ISetPlayMode
  }

  return {
    success: true,
    message: res.message,
  }
}

const reorderQueue = async (song_ids: number[], queue_id: number) => {
  const res = await request.patch<any, IAxiosRes<any>>(`/queues/${queue_id}/reorder`, {
    song_ids,
  })

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IReorderQueue
  }

  return {
    success: true,
    message: res.message,
  }
}

const createQueueFromPlaylist = async (playlist_id: number) => {
  const res = await request.post<any, IAxiosRes<any>>('/queues', {
    source: 'playlist',
    playlist_id,
  })

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as ICreatQueueFromPlaylist
  }

  return {
    success: true,
    message: res.message,
    queue_id: res.data.queue_id,
    song_count: res.data.song_count,
  }
}

const updateCurrentQueueState = async (stateData: IQueueState) => {
  const res = await request.patch<any, IAxiosRes<any>>('/queues/current/state', {
    stateData,
  })

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IUpdateCurrentQueueState
  }

  return {
    success: true,
    message: res.message,
  }
}

const clearQueue = async (queueId: number) => {
  const res = await request.delete<any, IAxiosRes<any>>(`/queues/${queueId}/songs`)

  if (!res.success) {
    return {
      success: false,
      message: res.message,
    } as IClearQueue
  }

  return {
    success: true,
    message: res.message,
  }
}

export const queueApi = {
  getCurrentQueue,
  getMyQueues,
  getQueueById,
  deletQueue,
  createQueueFromPlaylist,
  addSongToQueue,
  updateCurrentQueueState,
  reorderQueue,
  setPlayMode,
  alterQueueToCurrent,
  removeSongFromQueue,
  clearQueue,
}
