import type { IAxiosRes, IGetCurrentQueue, IGetMyQueues, IQueue, IQueueState } from '@/types'
import request from './axios'

type Id = number | string
type QueueResponse = { queue: IQueue }
type QueueItemAddition = { queue_id: Id; queue_item_position: number; queue_item_id: Id }
type QueueDeletion = { was_active: boolean; new_queue_id: Id | null }

const getMyQueues = async (): Promise<IGetMyQueues> => {
  const res = await request.get<never, IAxiosRes<{ queues: IQueue[] }>>('/queues', { silent: true })
  return { success: true, message: res.message, queues: res.data.queues }
}

const getQueueById = async (queueId: Id): Promise<{ success: boolean; message: string; queue: IQueue }> => {
  const res = await request.get<never, IAxiosRes<QueueResponse>>(`/queues/${queueId}`)
  return { success: true, message: res.message, queue: res.data.queue }
}

const getCurrentQueue = async (): Promise<IGetCurrentQueue> => {
  const res = await request.get<never, IAxiosRes<{ queue: IQueue; queue_state: IQueueState }>>('/queues/current', { silent: true })
  return { success: true, message: res.message, queue: res.data.queue, queue_state: res.data.queue_state }
}

const alterQueueToCurrent = async (queueId: Id) => {
  const res = await request.put<{ queue_id: Id }, IAxiosRes<null>>('/queues/player/current-queue', { queue_id: queueId })
  return { success: true, message: res.message }
}

const deleteQueue = async (queueId: Id) => {
  const res = await request.delete<never, IAxiosRes<QueueDeletion>>(`/queues/${queueId}`)
  return {
    success: true,
    message: res.message,
    data: res.data ?? { was_active: false, new_queue_id: null },
  }
}

const addSongToQueue = async (songId: Id, queueId: Id, mode: boolean) => {
  const res = await request.post<{ song_id: Id; mode: boolean }, IAxiosRes<QueueItemAddition>>(`/queues/${queueId || 0}/songs`, { song_id: songId, mode })
  return { success: res.success, message: res.message, data: res.data }
}

const removeSongFromQueue = async (queueId: Id, itemId: Id) => {
  const res = await request.delete<never, IAxiosRes<null>>(`/queues/${queueId}/songs/${itemId}`)
  return { success: true, message: res.message }
}

const setPlayMode = async (queueId: Id, playMode: string) => {
  const res = await request.patch<{ play_mode: string }, IAxiosRes<null>>(`/queues/${queueId}/play-mode`, { play_mode: playMode })
  return { success: true, message: res.message }
}

const reorderQueue = async (songIds: Id[], queueId: Id) => {
  const res = await request.patch<{ song_ids: Id[] }, IAxiosRes<null>>(`/queues/${queueId}/reorder`, { song_ids: songIds })
  return { success: true, message: res.message }
}

const createQueueFromPlaylist = async (playlistId: Id) => {
  const res = await request.post<{ source: 'playlist'; playlist_id: Id }, IAxiosRes<{ queue_id: Id; song_count: number }>>('/queues', { source: 'playlist', playlist_id: playlistId })
  return { success: true, message: res.message, ...res.data }
}

const updateCurrentQueueState = async (stateData: IQueueState) => {
  const res = await request.patch<{ stateData: IQueueState }, IAxiosRes<null>>('/queues/current/state', { stateData })
  return { success: true, message: res.message }
}

const clearQueue = async (queueId: Id) => {
  const res = await request.delete<never, IAxiosRes<null>>(`/queues/${queueId}/songs`)
  return { success: true, message: res.message }
}

export const queueApi = { getCurrentQueue, getMyQueues, getQueueById, deleteQueue, createQueueFromPlaylist, addSongToQueue, updateCurrentQueueState, reorderQueue, setPlayMode, alterQueueToCurrent, removeSongFromQueue, clearQueue }