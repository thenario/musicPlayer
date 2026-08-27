import { describe, expect, it } from 'vitest'
import { encryptPassword } from '@/utils/crypto'
import { formatDate, formatDuration, getImageUrl, sameId } from '@/utils/format'
import { parseLyrics } from '@/utils/lrc-parser'
import { createStorageItem } from '@/utils/storage'

describe('format utilities', () => {
  it('formats durations and handles empty values', () => {
    expect(formatDuration(0)).toBe('0:00')
    expect(formatDuration(102)).toBe('1:42')
    expect(formatDuration(undefined)).toBe('0:00')
  })

  it('formats valid and invalid dates', () => {
    expect(formatDate('2026-08-16T16:02:36')).toBe('2026-08-16')
    expect(formatDate(null)).toBe('未知时间')
    expect(formatDate('not-a-date')).toBe('未知时间')
  })

  it('builds image URLs and compares mixed ID types', () => {
    expect(getImageUrl('https://cdn.example.test/a.jpg')).toBe('https://cdn.example.test/a.jpg')
    expect(getImageUrl('/images/a.jpg')).toBe('/images/a.jpg')
    expect(getImageUrl()).toBe('')
    expect(sameId(12, '12')).toBe(true)
    expect(sameId(null, '12')).toBe(false)
  })
})

describe('lyrics parser', () => {
  it('sorts lyric lines and merges translations by timestamp', () => {
    const result = parseLyrics('[00:02.00]second\n[00:01.500]first', '[00:01.50]第一')

    expect(result).toEqual([
      { time: 1.5, content: 'first', translation: '第一' },
      { time: 2, content: 'second', translation: '' },
    ])
  })

  it('ignores malformed and empty lyric lines', () => {
    expect(parseLyrics('header\n[00:01.00]')).toEqual([])
  })
})

describe('storage utilities', () => {
  it('serializes, reads, and removes values', () => {
    const values = new Map<string, string>()
    const storage = {
      getItem: (key: string) => values.get(key) ?? null,
      setItem: (key: string, value: string) => values.set(key, value),
      removeItem: (key: string) => values.delete(key),
    } as unknown as Storage
    const item = createStorageItem<{ name: string }>('user', storage)

    item.set({ name: 'Alice' })
    expect(item.get()).toEqual({ name: 'Alice' })
    item.remove()
    expect(item.get()).toBeNull()
  })

  it('clears malformed JSON and returns null', () => {
    const values = new Map([['user', '{broken']])
    const storage = {
      getItem: (key: string) => values.get(key) ?? null,
      setItem: (key: string, value: string) => values.set(key, value),
      removeItem: (key: string) => values.delete(key),
    } as unknown as Storage
    const item = createStorageItem('user', storage)

    expect(item.get()).toBeNull()
    expect(values.has('user')).toBe(false)
  })
})

describe('crypto utilities', () => {
  it('returns a SHA-256 hexadecimal digest', async () => {
    await expect(encryptPassword('hello')).resolves.toBe(
      '2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824',
    )
  })
})
