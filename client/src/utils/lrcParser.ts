import type { LyricLine } from 'type'

export const parseLyrics = (lrc: string, t_lrc: string = ''): LyricLine[] => {
  const timeReg = /\[(\d{2}):(\d{2})\.(\d{2,3})\]/

  const createLrcMap = (str: string) => {
    const map = new Map<number, string>()
    if (!str) return map

    str.split('\n').forEach((line) => {
      const match = timeReg.exec(line)
      if (match) {
        const min = parseInt(match[1]!)
        const sec = parseInt(match[2]!)
        const ms = parseInt(match[3]!)
        const time = Number(
          (min * 60 + sec + ms / (match[3]!.length === 3 ? 1000 : 100)).toFixed(3),
        )

        const content = line.replace(timeReg, '').trim()
        if (content) map.set(time, content)
      }
    })
    return map
  }

  const lrcMap = createLrcMap(lrc)
  const tlrcMap = createLrcMap(t_lrc)

  const result: LyricLine[] = []
  lrcMap.forEach((content, time) => {
    result.push({
      time,
      content,
      translation: tlrcMap.get(time) || '',
    })
  })

  return result.sort((a, b) => a.time - b.time)
}
