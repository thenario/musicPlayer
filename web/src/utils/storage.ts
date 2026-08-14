import type { IUser } from '@/types'

export interface StorageItem<T> {
  get(): T | null
  set(value: T): void
  remove(): void
}

/** 带 JSON 序列化的 localStorage 读写封装。key 保持原有名称，避免破坏已登录会话。 */
export function createStorageItem<T>(key: string, storage: Storage = localStorage): StorageItem<T> {
  return {
    get() {
      const value = storage.getItem(key)
      if (value === null) return null
      try {
        return JSON.parse(value) as T
      } catch {
        storage.removeItem(key)
        return null
      }
    },
    set(value) {
      storage.setItem(key, JSON.stringify(value))
    },
    remove() {
      storage.removeItem(key)
    },
  }
}

export const tokenStorage = createStorageItem<string>('token')
export const userStorage = createStorageItem<IUser>('user')
