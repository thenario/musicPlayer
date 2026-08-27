import * as format from '../../src/utils/format'
import { describe, it, expect } from 'vitest'

describe('测试格式化函数', () => {
  it('时长格式化', () => {
    expect(format.formatDuration(0)).toBe("0:00")
    expect(format.formatDuration(102)).toBe("1:42")
    expect(format.formatDuration(60)).toBe("1:00")
    expect(format.formatDuration(undefined)).toBe("0:00")
  }),
  it('日期格式化',()=>{
    expect(format.formatDate("2026-08-16 16:02:36")).toBe("2026-08-16")
    expect(format.formatDate("2026-07-16 16:02:36")).toBe("2026-07-16")
    expect(format.formatDate("2025-07-16 16:02:36")).toBe("2025-07-16")
  })
})
