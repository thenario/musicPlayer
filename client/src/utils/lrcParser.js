export function parseLyrics(lrcString) {
  if (!lrcString) return []
  
  const lines = lrcString.split('\n')
  const result = []
  
  // 匹配时间戳的正则 [mm:ss.xx]
  const timeReg = /\[(\d{2}):(\d{2})(\.\d{2,3})?\]/

  for (const line of lines) {
    const match = timeReg.exec(line)
    if (match) {
      // 计算秒数: 分 * 60 + 秒
      const minutes = parseInt(match[1])
      const seconds = parseFloat(match[2] + (match[3] || ''))
      const time = minutes * 60 + seconds
      
      const text = line.replace(timeReg, '').trim()
      
      if (text) {
        result.push({ time, text })
      }
    }
  }
  
  return result
}