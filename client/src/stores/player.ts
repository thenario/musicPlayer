import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../utils/api'

// 后端地址常量
const API_BASE_URL = 'http://127.0.0.1:5000'

export const usePlayerStore = defineStore('player', () => {
  // =========================================================================
  // 1. 状态 (State)
  // =========================================================================
  
  // 播放核心
  const currentSong = ref(null)
  const isPlaying = ref(false)
  const audioElement = ref(null)
  
  // 进度与音量
  const progress = ref(0)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(80)

  // 队列数据
  // currentQueue 存储的是包装对象数组: [{ id: 1, song: {...} }, ...]
  const currentQueue = ref([])      
  const currentQueueId = ref(null)  // 当前队列的数据库 ID
  const userQueues = ref([])        // 左侧侧边栏的所有歌单
  
  // 播放状态控制
  const currentIndex = ref(-1)
  const playMode = ref('sequential') // sequential, shuffle, repeat_one, repeat_all
  
  // UI 状态
  const isQueueVisible = ref(false)
  const isSongDetailVisible = ref(false)

  // 同步控制
  const SYNC_INTERVAL = 5000 
  let lastSyncTime = 0 

  // =========================================================================
  // 2. 计算属性 (Getters)
  // =========================================================================
  const hasNext = computed(() => currentQueue.value.length > 0)
  const hasPrevious = computed(() => currentQueue.value.length > 0)

  // =========================================================================
  // 3. 内部辅助函数 (Helpers)
  // =========================================================================

  // 同步当前播放状态给后端 (节流调用)
  const syncPlayStateToBackend = async () => {
    if (!currentSong.value) return
    try {
      await api.put('/api/queue/state', {
        current_song_id: currentSong.value.id,
        current_position: currentIndex.value,
        progress: currentTime.value,
        is_playing: isPlaying.value, // 使用当前真实状态
        current_queue_id: currentQueueId.value
      })
    } catch (e) {
      console.warn("同步状态失败", e)
    }
  }

  // 同步队列顺序给后端 (用于拖拽排序、随机打乱后)
  const saveQueueOrder = async () => {
    if (!currentQueueId.value) return
    // 提取纯 song_id 列表
    const songIds = currentQueue.value.map(item => item.song.id)
    try {
      await api.post('/api/queue/items/reorder', {
        song_ids: songIds,
        queue_id: currentQueueId.value
      })
      console.log('队列顺序已同步到服务器')
    } catch (e) {
      console.error('保存队列顺序失败', e)
    }
  }

  // 监听音频结束事件
  const onAudioEnded = () => {
    if (playMode.value === 'repeat_one') {
      if (audioElement.value) {
        audioElement.value.currentTime = 0
        audioElement.value.play()
      }
    } else {
      nextSong(true)
    }
  }

  // 更新 Media Session (支持耳机/锁屏控制)
  const updateMediaSession = () => {
    if ('mediaSession' in navigator && currentSong.value) {
      const song = currentSong.value
      
      // 处理封面路径
      let coverUrl = song.cover_url || ''
      if (coverUrl && !coverUrl.startsWith('http')) {
          coverUrl = `${API_BASE_URL}${coverUrl.startsWith('/') ? '' : '/'}${coverUrl}`
      }

      navigator.mediaSession.metadata = new MediaMetadata({
        title: song.title,
        artist: song.artist,
        album: song.album,
        artwork: coverUrl ? [{ src: coverUrl, sizes: '512x512', type: 'image/jpeg' }] : []
      })

      navigator.mediaSession.setActionHandler('play', () => resumeSong())
      navigator.mediaSession.setActionHandler('pause', () => pauseSong())
      navigator.mediaSession.setActionHandler('previoustrack', () => previousSong())
      navigator.mediaSession.setActionHandler('nexttrack', () => nextSong())
    }
  }

  // =========================================================================
  // 4. 音频控制动作 (Audio Actions)
  // =========================================================================

  // 初始化音频元素
  const setAudioElement = (element) => {
    audioElement.value = element
    if (!element) return

    // 1. 进度更新 (节流同步)
    element.addEventListener('timeupdate', () => {
      // UI 实时更新
      currentTime.value = element.currentTime
      if (duration.value > 0) {
        progress.value = (currentTime.value / duration.value) * 100
      }

      // 后端节流同步
      const now = Date.now()
      if (now - lastSyncTime > SYNC_INTERVAL && isPlaying.value) {
        syncPlayStateToBackend()
        lastSyncTime = now
      }
    })

    // 2. 元数据加载
    element.addEventListener('loadedmetadata', () => {
      duration.value = element.duration
    })

    // 3. 播放结束
    element.addEventListener('ended', onAudioEnded)

    // 4. 监听原生暂停 (耳机/系统触发)
    element.addEventListener('pause', () => {
      isPlaying.value = false
      syncPlayStateToBackend() // 暂停时立即保存
    })

    // 5. 监听原生播放
    element.addEventListener('play', () => {
      isPlaying.value = true
      updateMediaSession() // 更新锁屏信息
    })

    element.volume = volume.value / 100
  }

  // 核心播放：按索引播放
  const playAtIndex = async (index) => {
    if (index < 0 || index >= currentQueue.value.length) return

    const item = currentQueue.value[index]
    const song = item.song

    // 更新状态
    currentIndex.value = index
    currentSong.value = song
    isPlaying.value = true

    // 驱动 Audio
    if (audioElement.value) {
      audioElement.value.src = `${API_BASE_URL}/api/songs/${song.id}/stream`
      audioElement.value.play().catch(e => console.warn("自动播放被拦截", e))
    }

    // 更新 Media Session
    updateMediaSession()

    // 同步后端
    syncPlayStateToBackend()
  }

  // 播放控制
  const togglePlay = () => isPlaying.value ? pauseSong() : resumeSong()
  
  const pauseSong = () => {
    // 这里的 isPlaying 会通过 'pause' 事件监听器自动更新
    audioElement.value?.pause()
  }
  
  const resumeSong = () => {
    if (currentSong.value) {
      audioElement.value?.play()
    }
  }

  const seek = (time) => {
    if (audioElement.value) {
      audioElement.value.currentTime = time
      currentTime.value = time
      syncPlayStateToBackend() // 拖拽结束立即保存
    }
  }

  const setVolume = (val) => {
    volume.value = val
    if (audioElement.value) audioElement.value.volume = val / 100
  }

  // =========================================================================
  // 5. 队列逻辑动作 (Queue Logic)
  // =========================================================================

  // 播放特定歌曲 (点击列表)
  const playSong = async (song) => {
    // 如果是当前歌曲，仅切换播放状态
    if (currentSong.value?.id === song.id) {
        togglePlay()
        return
    }
    // 插入到下一首 (true) 并获取新索引
    const newIndex = await addToQueue(song, true)
    
    // 播放
    if (newIndex !== -1) {
        playAtIndex(newIndex)
    }
  }

  // 下一首
  const nextSong = (isAuto = false) => {
    if (currentQueue.value.length === 0) return
    let nextIndex = currentIndex.value + 1

    if (nextIndex >= currentQueue.value.length) {
      // 列表播完的处理
      if (playMode.value === 'repeat_all' || isAuto) {
         if (playMode.value === 'sequential' && isAuto) {
             // 顺序播放且自动结束 -> 停止
             isPlaying.value = false
             return
         }
         nextIndex = 0 
      } else {
         nextIndex = 0
      }
    }
    playAtIndex(nextIndex)
  }

  // 上一首
  const previousSong = () => {
    if (currentQueue.value.length === 0) return
    
    // 3秒原则：如果播放超过3秒，重头开始
    if (audioElement.value && audioElement.value.currentTime > 3) {
      audioElement.value.currentTime = 0
      return
    }
    
    let prevIndex = currentIndex.value - 1
    if (prevIndex < 0) prevIndex = currentQueue.value.length - 1
    playAtIndex(prevIndex)
  }

  // 设置播放模式 (含洗牌)
  const setPlayMode = async (mode) => {
    playMode.value = mode
    
    // 只有切换到随机模式时才打乱，且是破坏性打乱
    if (mode === 'shuffle') {
      await shuffleQueue()
    }
    
    try {
      await api.put('/api/queue/playmode', { play_mode: mode })
    } catch (e) { console.error(e) }
  }

  // 洗牌 (破坏性重排)
  const shuffleQueue = async () => {
    if (currentQueue.value.length <= 1) return

    const currentId = currentSong.value?.id
    let currentItem = null
    const others = []

    // 分离当前歌
    for (const item of currentQueue.value) {
        if (item.song.id === currentId && !currentItem) {
            currentItem = item
        } else {
            others.push(item)
        }
    }
    
    // 洗牌
    for (let i = others.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [others[i], others[j]] = [others[j], others[i]];
    }

    // 重组
    if (currentItem) {
        currentQueue.value = [currentItem, ...others]
        currentIndex.value = 0
    } else {
        currentQueue.value = others
        currentIndex.value = -1
    }

    await saveQueueOrder()
  }

  // 拖拽排序更新 (供组件 v-model 使用)
  const updateQueueOrder = async (newQueue) => {
    currentQueue.value = newQueue
    
    // 修正当前播放索引 (因为位置变了)
    if (currentSong.value) {
        const newIndex = currentQueue.value.findIndex(item => item.song.id === currentSong.value.id)
        if (newIndex !== -1) {
            currentIndex.value = newIndex
        }
    }
    // 同步给后端
    await saveQueueOrder()
  }

  // 添加到队列 (核心逻辑：查重 -> 移动 -> 插入 -> 同步)
  const addToQueue = async (song, insertNext = false) => {
    if (!song || !song.id) return -1

    // 1. 查重 & 移除旧的 (保持不变)
    const oldIdx = currentQueue.value.findIndex(item => item.song.id === song.id)
    if (oldIdx !== -1) {
      if (insertNext && oldIdx === currentIndex.value) return oldIdx
      currentQueue.value.splice(oldIdx, 1)
      if (oldIdx < currentIndex.value) currentIndex.value--
    }

    // 2. 计算位置 (保持不变)
    let targetIndex = insertNext ? currentIndex.value + 1 : currentQueue.value.length

    // 3. 本地插入 (临时 ID)
    const tempId = `temp-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    const newItem = { id: tempId, song: song } // 此时 song 可能封面不全
    
    currentQueue.value.splice(targetIndex, 0, newItem)
    
    // 如果不是随机模式，这里其实不用同步 originalQueue，因为我们已经放弃维护它了
    // 但为了逻辑一致性暂时保留也没事

    // 4. 【核心修改】后端同步并回填数据
    try {
        const res = await api.post(`/api/queues/0/song`, {
            song_id: song.id,
            position: targetIndex + 1 
        })

        // ★★★ 偷梁换柱：用后端返回的真实数据替换本地临时数据 ★★★
        if (res.data && res.data.action === 'added') {
            // 在队列里找到刚才那个临时 ID 的项
            const itemToUpdate = currentQueue.value.find(item => item.id === tempId)
            
            if (itemToUpdate) {
                // 1. 替换 ID：把 temp-xxx 换成数据库真实的 item_id (例如 105)
                // 这样当你点击删除时，就会发送真实的 DELETE 请求了
                if (res.data.item_id) {
                    itemToUpdate.id = res.data.item_id
                }

                // 2. 替换歌曲信息：后端返回的 song_info 包含了处理好的 cover_url
                if (res.data.song_info) {
                    itemToUpdate.song = res.data.song_info
                }
                
                // 3. 更新左侧侧边栏计数 (可选)
                if (currentQueueId.value) {
                    const targetQueue = userQueues.value.find(q => q.id === currentQueueId.value)
                    if (targetQueue) targetQueue.item_count++
                }
            }
        } else if (res.data && res.data.action === 'jump_to_existing') {
            // 如果后端发现重复了(并发情况)，可能会返回跳转指令，这里视情况处理
        }

    } catch (e) {
        console.error("添加到队列失败", e)
        // 如果失败了，应该把刚才加的临时项删掉，避免数据不一致
        const failIdx = currentQueue.value.findIndex(item => item.id === tempId)
        if (failIdx !== -1) currentQueue.value.splice(failIdx, 1)
    }

    return targetIndex
  }

  // 移除歌曲
  const removeQueueItem = async (itemId) => {
    if (!itemId) return

    try {
        // 1. 后端删除 (排除临时ID)
        const isTempId = typeof itemId === 'string' && itemId.startsWith('temp-')
        if (!isTempId) {
            api.delete(`/api/queue/items/${itemId}`).catch(e => console.error(e))
        }
        
        // 2. 本地删除
        const currentIdx = currentQueue.value.findIndex(item => item.id === itemId)
        if (currentIdx !== -1) {
            const isDeletingCurrent = currentIdx === currentIndex.value
            
            // 删数据
            currentQueue.value.splice(currentIdx, 1)
            
            // 如果删的是当前播放的，切歌
            if (isDeletingCurrent) {
                if (currentQueue.value.length > 0) {
                    const nextIdx = Math.min(currentIdx, currentQueue.value.length - 1)
                    playAtIndex(nextIdx)
                } else {
                    pauseSong()
                    currentSong.value = null
                    currentIndex.value = -1
                }
            } else if (currentIdx < currentIndex.value) {
                // 如果删的是前面的，索引减1
                currentIndex.value--
            }
        }

        // 3. 同步左侧列表计数
        if (currentQueueId.value) {
            const targetQueue = userQueues.value.find(q => q.id === currentQueueId.value)
            if (targetQueue && targetQueue.item_count > 0) {
                targetQueue.item_count--
            }
        }
    } catch (e) {
        console.error("移除歌曲失败", e)
        fetchUserQueues()
    }
  }

  // =========================================================================
  // 6. API 交互动作 (API Actions)
  // =========================================================================

  // 初始化加载当前队列
  const fetchCurrentQueue = async () => {
    try {
      const res = await api.get('/api/queue/current')
      const queueData = res.data.queue
      const stateData = res.data.state

      if (queueData && queueData.items) {
        const items = queueData.items
        // 初始化本地队列
        currentQueue.value = [...items]
        currentQueueId.value = queueData.id 
        
        if (stateData) {
          playMode.value = stateData.play_mode || 'sequential'
          
          if (stateData.current_song_id) {
             const idx = currentQueue.value.findIndex(i => i.song.id === stateData.current_song_id)
             if (idx !== -1) {
                 currentIndex.value = idx
                 currentSong.value = currentQueue.value[idx].song
                 
                 // 恢复音频源 (防止进度条直接到底)
                 if (audioElement.value) {
                     audioElement.value.src = `${API_BASE_URL}/api/songs/${currentSong.value.id}/stream`
                     
                     const savedProgress = stateData.progress || 0
                     
                     const restoreProgress = () => {
                         const d = audioElement.value.duration
                         // 如果进度正常，且没到结尾，才恢复
                         if (savedProgress > 0 && savedProgress < (d - 2)) {
                             audioElement.value.currentTime = savedProgress
                             currentTime.value = savedProgress
                         } else {
                             audioElement.value.currentTime = 0
                             currentTime.value = 0
                         }
                     }
                     // 等待元数据加载后再设置进度
                     audioElement.value.addEventListener('loadedmetadata', restoreProgress, { once: true })
                 }
             }
          }
        }
      }
    } catch (e) { console.error(e) }
  }

  // 获取用户所有队列
  const fetchUserQueues = async () => {
    try {
      const res = await api.get('/api/queues')
      userQueues.value = res.data
    } catch (e) { console.error(e) }
  }

  // 预览队列详情 (不切换)
  const fetchQueueDetails = async (queueId) => {
    try {
      const res = await api.get(`/api/queues/${queueId}`)
      return res.data
    } catch (e) {
      console.error("获取队列详情失败", e)
      return null
    }
  }

  // 切换并播放指定队列的歌
  const playSongInQueue = async (queueId, index) => {
    try {
      if (currentQueueId.value !== queueId) {
          await switchQueue(queueId)
      }
      playAtIndex(index)
    } catch (e) {
      console.error("切换播放失败", e)
    }
  }

  // 切换当前队列
  const switchQueue = async (queueId) => {
    try {
      await api.post(`/api/queues/${queueId}/load`)
      await fetchCurrentQueue()
      await fetchUserQueues()
      pauseSong() // 切换后暂停
    } catch (e) { console.error(e) }
  }

  const deleteQueue = async (queueId) => {
    try {
      await api.delete(`/api/queues/${queueId}`)
      await fetchUserQueues()
      if (currentQueueId.value === queueId) {
          currentQueue.value = []
          currentSong.value = null
          isPlaying.value = false
          currentQueueId.value = null
      }
    } catch (e) { console.error(e) }
  }

  const playPlaylist = async (playlistId, startSongId = null) => {
    try {
      console.log(`正在请求播放歌单: ${playlistId}`)
      await api.post(`/api/queues/from-playlist/${playlistId}`, {})
      await fetchCurrentQueue()
      await fetchUserQueues()
      
      let startIndex = 0
      if (startSongId) {
          const foundIndex = currentQueue.value.findIndex(item => item.song.id === startSongId)
          if (foundIndex !== -1) startIndex = foundIndex
      }
      
      if (currentQueue.value.length > 0) {
          playAtIndex(startIndex)
      }
    } catch (e) {
      console.error("播放歌单失败", e)
    }
  }

  // =========================================================================
  // 7. UI 动作
  // =========================================================================
  const toggleQueueVisibility = () => isQueueVisible.value = !isQueueVisible.value
  const closeQueue = () => isQueueVisible.value = false
  const toggleSongDetail = () => isSongDetailVisible.value = !isSongDetailVisible.value

  // =========================================================================
  // Return
  // =========================================================================
  return {
    // State
    currentSong, isPlaying, progress, currentTime, duration, volume, playMode,
    currentQueue, currentQueueId, userQueues, 
    isQueueVisible, isSongDetailVisible,
    
    // Getters
    hasNext, hasPrevious,

    // Actions
    setAudioElement, 
    playSong, playAtIndex, togglePlay, pauseSong, resumeSong,
    nextSong, previousSong, seek, setVolume, setPlayMode,
    
    addToQueue, removeQueueItem, updateQueueOrder, // 队列操作
    
    fetchCurrentQueue, fetchUserQueues, fetchQueueDetails, // 数据获取
    switchQueue, deleteQueue, playPlaylist, playSongInQueue, // 切换与播放逻辑
    
    toggleQueueVisibility, closeQueue, toggleSongDetail // UI 操作
  }
})