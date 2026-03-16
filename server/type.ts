export interface IError {
  success: boolean;
  message: string;
}

export interface IUser {
  user_id: number;
  user_email: string;
  user_name: string;
}

export interface ISong {
  song_id: number;
  song_title: string;
  song_url: string;
  artist: string;
  album: string;
  duration: number;
  file_name: string;
  file_format: string;
  file_size: number;
  bitrate: number;
  play_count: number;
  date_added: Date;
  last_played: Date;
  uploader_id: number;
  uploader_name: string;
  song_cover_url: string;
  lyrics: string;
}

export interface IPagination {
  total_items: number;
  total_pages: number;
  current_page: number;
  page_limit: number;
}

export interface IPlaylist {
  playlist_id: number;
  creator_id: number;
  playlist_name: string;
  playlist_cover_url: string;
  song_count: number;
  like_count: number;
  is_public: boolean;
  created_date: Date;
  updatedd_date: Date;
  songs: ISong[];
  is_liked: boolean;
  user: IUser;
}

export interface IQueueItem {
  queue_item_id: number | string;
  queue_item_poition: number;
  queue_id: number;
  song: ISong;
  added_date: Date;
}

export interface IQueue {
  queue_id: number;
  queue_name: string;
  creator_id:string;
  song_count: number;
  is_current: boolean;
  created_date: Date;
  updated_date: Date;
  queue_items: IQueueItem[];
}

export interface IQueueState {
  user_id:string;
  current_queue_id: number;
  current_song_id: number;
  current_position: number;
  is_playing: boolean;
  updated_date: Date;
  current_progress: number;
  playmode: string;
}

export interface ILogin {
  message: string;
  success: boolean;
  user: IUser;
  token: string;
}

export interface ILogout {
  message: string;
  success: boolean;
}

export interface IRegister {
  message: string;
  success: boolean;
  user: IUser;
}

export interface IGetSongs {
  success: boolean;
  message: string;
  songs: ISong[];
  pagination: IPagination;
}

export interface IUploadSong {
  success: boolean;
  message: string;
}

export interface IGetStatics {
  success: boolean;
  message: string;
  total_songs: number;
  total_users: number;
  online_users: number;
  popular_songs: ISong[];
}

export interface IGetMyPlaylists {
  message: string;
  success: boolean;
  playlits: IPlaylist[];
}
export interface IGetPlaylistById {
  message: string;
  success: boolean;
  playlist: IPlaylist;
  songs: ISong[];
}
export interface ICreatePlaylist {
  message: string;
  success: boolean;
  playlist_id: number;
}
export interface IDeletePlaylist {
  message: string;
  success: boolean;
}
export interface IAddSongToPlaylist {
  message: string;
  success: boolean;
  song_position: number;
}
export interface IRemoveSongFromPlaylist {
  message: string;
  success: boolean;
}
export interface ILikePlaylist {
  message: string;
  success: boolean;
}
export interface IUnlikePlaylist {
  message: string;
  success: boolean;
}
export interface IGetMyQueues {
  message: string;
  success: boolean;
  queues: IQueue[];
}
export interface IGetQueueById {
  message: string;
  success: boolean;
  queue: IQueue;
  queue_items: IQueueItem[];
}
export interface IGetCurrentQueue {
  message: string;
  success: boolean;
  queue: IQueue;
  queue_state: IQueueState;
}
export interface IAlterQueueTocurrent {
  message: string;
  success: boolean;
}
export interface IDeleteQueue {
  message: string;
  success: boolean;
}
export interface IClearQueue {
  message: string;
  success: boolean;
}
export interface IAddSongToQueue {
  message: string;
  success: boolean;
  queue_id: number;
  song_id: number;
  song_position: number;
  action: string;
  queue_item: IQueueItem;
}
export interface IRemoveSongFromQueue {
  message: string;
  success: boolean;
}
export interface IReorderQueue {
  message: string;
  success: boolean;
}
export interface ICreatQueueFromPlaylist {
  message: string;
  success: boolean;
  queue_id: number;
  song_count: number;
}
export interface IUpdateCurrentQueueState {
  message: string;
  success: boolean;
}
export interface ISetPlayMode {
  message: string;
  success: boolean;
}
