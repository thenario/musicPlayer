export interface IAxiosRes<T = unknown> {
  success: boolean
  data: T
  message: string
  code: number
}

export interface IUser {
  user_id: number | string
  user_email: string
  user_name: string
  user_cover_url?: string
}

export interface ISong {
  song_id: number | string
  song_title: string
  song_url: string
  artist: string
  album: string
  duration: number
  file_format: string
  file_size: number | null
  bitrate: number
  date_added: string
  uploader_id: number | string
  uploader_name: string
  song_cover_url: string | null
  lyrics?: string
  t_lyrics?: string
}

export interface IPagination {
  total_items: number
  total_pages: number
  current_page: number
  page_limit: number
}

export interface IPlaylist {
  playlist_id: number | string
  creator_id: number | string
  playlist_name: string
  playlist_cover_url: string
  song_count: number
  like_count: number
  play_count: number
  is_public: boolean
  created_date: Date
  updated_date: Date
  description: string
}

export interface IQueueItem {
  queue_item_id: number | string
  queue_item_position: number
  queue_id: number | string
  song: ISong
  added_date: Date
}

export interface IQueue {
  queue_id: number | string
  queue_name: string
  song_count: number
  is_current: boolean
  created_date: Date
  updated_date: Date
  queue_items: IQueueItem[]
}

export interface IQueueState {
  current_queue_id: number | string
  current_song_id: number | string
  current_position: number
  is_playing: boolean
  updated_date: Date
  current_progress: number
  playmode: string
}

export interface IUploadSong {
  success: boolean
  message: string
}

/** 歌单中的歌曲：附带在歌单内的排序位置 */
export type PlaylistSong = ISong & { song_playlist_position: number }

export interface IGetMyPlaylists {
  message: string
  success: boolean
  playlists: IPlaylist[]
}
export interface IGetPlaylistById {
  message: string
  success: boolean
  playlist: IPlaylist
  songs: PlaylistSong[]
  is_liked: boolean
}
export interface IGetMyQueues {
  message: string
  success: boolean
  queues: IQueue[]
}
export interface IGetCurrentQueue {
  message: string
  success: boolean
  queue: IQueue
  queue_state: IQueueState
}
export interface LyricLine {
  time: number
  content: string
  translation?: string
}
