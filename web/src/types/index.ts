export interface IAxiosRes<T = unknown> {
  success: boolean
  data: T
  message: string
  code: number
}

export interface IError {
  success: boolean
  message: string
}

export interface IUser {
  user_id: number | string
  user_email: string
  user_name: string
  user_cover?: string
}

export interface ISong {
  song_id: number | string
  song_title: string
  song_url: string
  artist: string
  album: string
  duration: number
  file_name: string
  file_format: string
  file_size: number
  bitrate: number
  play_count: number
  date_added: Date
  last_played: Date
  uploader_id: number | string
  uploader_name: string
  song_cover_url: string
  lyrics: string
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

export interface ILogin {
  message: string
  success: boolean
  user: IUser
  token: string
}

export interface ILogout {
  message: string
  success: boolean
}

export interface IRegister {
  message: string
  success: boolean
  user: IUser
}

export interface IGetSongs {
  success: boolean
  message: string
  songs: ISong[]
  pagination: IPagination
}

export interface IUploadSong {
  success: boolean
  message: string
}

export interface IGetStatics {
  success: boolean
  message: string
  total_songs: number
  total_users: number
  online_users: number
  popular_songs: ISong[]
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
export interface ICreatePlaylist {
  message: string
  success: boolean
  playlist_id: number | string
}
export interface IDeletePlaylist {
  message: string
  success: boolean
}
export interface IAddSongToPlaylist {
  message: string
  success: boolean
  song_position: number
}
export interface IRemoveSongFromPlaylist {
  message: string
  success: boolean
}
export interface ILikePlaylist {
  message: string
  success: boolean
}
export interface IUnlikePlaylist {
  message: string
  success: boolean
}
export interface IGetMyQueues {
  message: string
  success: boolean
  queues: IQueue[]
}
export interface IGetQueueById {
  message: string
  success: boolean
  queue: IQueue
  queue_items: IQueueItem[]
}
export interface IGetCurrentQueue {
  message: string
  success: boolean
  queue: IQueue
  queue_state: IQueueState
}
export interface IAlterQueueTocurrent {
  message: string
  success: boolean
}
export interface IDeleteQueue {
  message: string
  success: boolean
  data?: unknown
}
export interface IClearQueue {
  message: string
  success: boolean
}
export interface IAddSongToQueue {
  message: string
  success: boolean
  queue_id: number | string
  song_id: number | string
  song_position: number
  action: string
  queue_item: IQueueItem
  data?: unknown
}
export interface IRemoveSongFromQueue {
  message: string
  success: boolean
}
export interface IReorderQueue {
  message: string
  success: boolean
}
export interface ICreatQueueFromPlaylist {
  message: string
  success: boolean
  queue_id: number | string
  song_count: number
}
export interface IUpdateCurrentQueueState {
  message: string
  success: boolean
}
export interface ISetPlayMode {
  message: string
  success: boolean
}

export interface LyricLine {
  time: number
  content: string
  translation?: string
}
