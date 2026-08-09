// 顶栏导航菜单项
export interface NavItem {
  path: string
  label: string
  isLogo?: boolean
}

export const NAV_ITEMS: NavItem[] = [
  { path: '/', label: '音乐播放器', isLogo: true },
  { path: '/songs', label: '歌曲库' },
  { path: '/playlists', label: '歌单' },
]
