import type { IAxiosRes, IGetCurrentQueue, IGetMyQueues, IQueue, IQueueState } from '@/types'
import request from './axios'

type Id = number | string
type QueueResponse = { queue: IQueue }
type QueueItemAddition = { queue_id: Id; queue_item_position: number; queue_item_id: Id }
type QueueDeletion = { was_active: boolean; new_queue_id: Id | null }

/**
 * 播放队列模块接口。
 *
 * 泛型约定：响应拦截器已把 AxiosResponse 改写为 IAxiosRes（见 axios.ts），
 * 故第二泛型统一声明「返回包装类型」IAxiosRes<响应体>；
 * 无请求体时第一泛型传 never，有请求体时借位标注请求体类型。
 */

/** 获取我的队列列表（silent：失败不集中弹错）。 */
const getMyQueues = async (): Promise<IGetMyQueues> => {
  const res = await request.get<never, IAxiosRes<{ queues: IQueue[] }>>('/queues', { silent: true })
  return { success: true, message: res.message, queues: res.data.queues }
}

/** 获取指定队列详情。 */
const getQueueById = async (queueId: Id): Promise<{ success: boolean; message: string; queue: IQueue }> => {
  const res = await request.get<never, IAxiosRes<QueueResponse>>(`/queues/${queueId}`)
  return { success: true, message: res.message, queue: res.data.queue }
}

/** 获取当前播放队列及其状态（silent：失败不集中弹错）。 */
const getCurrentQueue = async (): Promise<IGetCurrentQueue> => {
  const res = await request.get<never, IAxiosRes<{ queue: IQueue; queue_state: IQueueState }>>('/queues/current', { silent: true })
  return { success: true, message: res.message, queue: res.data.queue, queue_state: res.data.queue_state }
}

/** 把指定队列切换为当前播放队列。 */
const alterQueueToCurrent = async (queueId: Id) => {
  const res = await request.put<{ queue_id: Id }, IAxiosRes<null>>('/queues/player/current-queue', { queue_id: queueId })
  return { success: true, message: res.message }
}

/** 删除队列；返回是否删掉了正在播放的队列及回退后的新队列。 */
const deleteQueue = async (queueId: Id) => {
  const res = await request.delete<never, IAxiosRes<QueueDeletion>>(`/queues/${queueId}`)
  return {
    success: true,
    message: res.message,
    data: res.data ?? { was_active: false, new_queue_id: null },
  }
}

/** 添加歌曲到队列（mode 区分「立即播放/下一首播放」）。 */
const addSongToQueue = async (songId: Id, queueId: Id, mode: boolean) => {
  const res = await request.post<{ song_id: Id; mode: boolean }, IAxiosRes<QueueItemAddition>>(`/queues/${queueId || 0}/songs`, { song_id: songId, mode })
  return { success: res.success, message: res.message, data: res.data }
}

/** 从队列移除歌曲。 */
const removeSongFromQueue = async (queueId: Id, itemId: Id) => {
  const res = await request.delete<never, IAxiosRes<null>>(`/queues/${queueId}/songs/${itemId}`)
  return { success: true, message: res.message }
}

/** 设置播放模式。 */
const setPlayMode = async (queueId: Id, playMode: string) => {
  const res = await request.patch<{ play_mode: string }, IAxiosRes<null>>(`/queues/${queueId}/play-mode`, { play_mode: playMode })
  return { success: true, message: res.message }
}

/** 重排队列（按给定 song_id 顺序）。 */
const reorderQueue = async (songIds: Id[], queueId: Id) => {
  const res = await request.patch<{ song_ids: Id[] }, IAxiosRes<null>>(`/queues/${queueId}/reorder`, { song_ids: songIds })
  return { success: true, message: res.message }
}

/** 从歌单创建队列。 */
const createQueueFromPlaylist = async (playlistId: Id) => {
  const res = await request.post<{ source: 'playlist'; playlist_id: Id }, IAxiosRes<{ queue_id: Id; song_count: number }>>('/queues', { source: 'playlist', playlist_id: playlistId })
  return { success: true, message: res.message, ...res.data }
}

/** 更新当前队列播放状态（进度/播放中）。 */
const updateCurrentQueueState = async (stateData: IQueueState) => {
  const res = await request.patch<{ stateData: IQueueState }, IAxiosRes<null>>('/queues/current/state', { stateData })
  return { success: true, message: res.message }
}

/** 清空队列中的所有歌曲。 */
const clearQueue = async (queueId: Id) => {
  const res = await request.delete<never, IAxiosRes<null>>(`/queues/${queueId}/songs`)
  return { success: true, message: res.message }
}

export const queueApi = { getCurrentQueue, getMyQueues, getQueueById, deleteQueue, createQueueFromPlaylist, addSongToQueue, updateCurrentQueueState, reorderQueue, setPlayMode, alterQueueToCurrent, removeSongFromQueue, clearQueue }
