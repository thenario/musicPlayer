// 播放模式常量
export const PLAY_MODES = ['repeat_all', 'repeat_one', 'shuffle'] as const

export const PLAY_MODE_TITLES: Record<string, string> = {
  repeat_all: '列表循环',
  sequential: '顺序播放',
  repeat_one: '单曲循环',
  shuffle: '随机播放',
}
