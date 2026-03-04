import {
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
} from '../type'
import request from './axios'

const getMyQueues = async () => {
  try {
    const res = await request.get<any, IGetMyQueues>('api/queues')
    return {
      success: res.success,
      message: res.message,
      queues: res.queues,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.messssage,
    }
  }
}

const getQueueById = async (queueId: number) => {
  try {
    const res = await request.get<any, IGetQueueById>('api/queues/id', { params: queueId })
    return {
      success: res.success,
      message: res.message,
      queue: res.queue,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.messssage,
    }
  }
}

const getCurrentQueue = async () => {
  try {
    const res = await request.get<any, IGetCurrentQueue>('api/queues/id')
    return {
      success: res.success,
      message: res.message,
      queue: res.queue,
      queue_state: res.queue_state,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.messssage,
    }
  }
}

const alterQueueToCurrent = async (queueId: number) => {
  try {
    const res = await request.post<any, IAlterQueueTocurrent>('api/queues/id/altertocurrent', {
      queueId,
    })
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.messssage,
    }
  }
}

const deletQueue = async (queueId: number) => {
  try {
    const res = await request.post<any, IDeleteQueue>('api/queues/id', { queueId })
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.messssage,
    }
  }
}

const addSongToQueue = async (song_id: number, queue_id: number, song_position: number) => {
  try {
    const res = await request.post<any, IAddSongToQueue>('api/queues/id/addsong', {
      song_id,
      queue_id,
      song_position,
    })
    return {
      success: res.success,
      message: res.message,
      queue_id: res.queue_id,
      song_id: res.song_id,
      song_position: res.song_position,
      action: res.action,
      queue_item: res.queue_item,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.messssage,
    }
  }
}

const removeSongFromQueue = async (itemId: number) => {
  try {
    const res = await request.put<any, ISetPlayMode>('api/queues/id/remove', {
      itemId,
    })
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.message,
    }
  }
}

const setPlayMode = async (play_mode: string) => {
  try {
    const res = await request.put<any, ISetPlayMode>('api/queues/id/playmode', {
      play_mode,
    })
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.message,
    }
  }
}

const reorderQueue = async (song_ids: number[], queue_id: number) => {
  try {
    const res = await request.post<any, IReorderQueue>('api/queues/id/reorder', {
      song_ids,
      queue_id,
    })
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.messssage,
    }
  }
}

const createQueueFromPlaylist = async (playlistIId: number) => {
  try {
    const res = await request.post<any, ICreatQueueFromPlaylist>('api/queues/createfromlist/id', {
      playlistIId,
    })
    return {
      success: res.success,
      message: res.message,
      queue_id: res.queue_id,
      song_count: res.song_count,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.messssage,
    }
  }
}

const updateCurrentQueueState = async (stateData: IQueueState) => {
  try {
    const res = await request.post<any, IUpdateCurrentQueueState>('api/queues/state', { stateData })
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.messssage,
    }
  }
}

const clearQueue = async (queueId: number) => {
  try {
    const res = await request.post<any, IClearQueue>('api/queues/state', { queueId })
    return {
      success: res.success,
      message: res.message,
    }
  } catch (error: any) {
    return {
      success: error.response?.data?.success,
      message: error.response?.ata?.messssage,
    }
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
