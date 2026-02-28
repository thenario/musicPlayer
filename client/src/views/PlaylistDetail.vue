<template>
  <div class="h-full flex flex-col">
    <!-- 加载状态 -->
    <div v-if="loading" class="flex-1 flex items-center justify-center text-gray-500">
      <div class="animate-pulse flex flex-col items-center">
        <div class="h-4 w-32 bg-gray-700 rounded mb-4"></div>
        <div>加载歌单信息...</div>
      </div>
    </div>

    <div v-else-if="playlist" class="h-full flex flex-col overflow-hidden">
      <!-- 1. 歌单头部信息 -->
      <div class="shrink-0 p-8 bg-gradient-to-b from-gray-800 to-gray-900/50 flex items-start gap-8">
        <!-- 封面图 -->
        <div
          class="w-48 h-48 bg-gray-700 rounded-lg shadow-xl overflow-hidden flex-shrink-0 flex items-center justify-center relative group">
          <img v-if="playlist.cover_image" :src="getImageUrl(playlist.cover_image)" class="w-full h-full object-cover"
            alt="封面">
          <div v-else class="text-gray-500 flex flex-col items-center">
            <svg class="w-16 h-16 mb-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd"
                d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
                clip-rule="evenodd" />
            </svg>
            <span class="text-sm">暂无封面</span>
          </div>
        </div>

        <!-- 文字信息 -->
        <div class="flex-1 min-w-0 pt-2">
          <div class="flex items-center space-x-2 mb-3">
            <span
              class="px-2 py-0.5 border border-blue-500 text-blue-400 text-xs rounded uppercase tracking-wide">歌单</span>
            <span v-if="playlist.is_public" class="text-xs text-gray-400">公开</span>
            <span v-else class="text-xs text-gray-500 flex items-center gap-1">
              <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z">
                </path>
              </svg>
              私有
            </span>
          </div>

          <h1 class="text-4xl font-bold mb-4 text-white truncate" :title="playlist.name">{{ playlist.name }}</h1>
          <p class="text-gray-400 mb-4 line-clamp-2 text-sm">{{ playlist.description || '暂无描述' }}</p>

          <div class="flex items-center space-x-4 text-sm text-gray-400 mb-6">
            <div class="flex items-center gap-2">
              <div
                class="w-6 h-6 bg-gray-600 rounded-full flex items-center justify-center text-xs text-white font-bold">
                {{ playlist.user.user_name.charAt(0).toUpperCase() }}
              </div>
              <span class="text-white hover:underline cursor-pointer">{{ playlist.user.user_name }}</span>
            </div>
            <span>•</span>
            <span>{{ playlist.song_count }} 首歌曲</span>
            <span>•</span>
            <span>{{ playlist.play_count }} 次播放</span>
          </div>

          <!-- 操作按钮组 -->
          <div class="flex items-center flex-wrap gap-3">
            <button @click="playAll"
              class="px-6 py-2.5 bg-green-500 text-black font-bold rounded-full hover:scale-105 hover:bg-green-400 transition-all flex items-center gap-2">
              <svg class="w-5 h-5 fill-current" viewBox="0 0 24 24">
                <path d="M8 5v14l11-7z" />
              </svg>
              <span>播放全部</span>
            </button>

            <button @click="toggleLike"
              class="px-6 py-2 rounded-full border flex items-center space-x-2 transition-all duration-200" :class="playlist.is_liked
                ? 'bg-red-500/10 border-red-500 text-red-500'
                : 'bg-gray-700 border-transparent text-white hover:bg-gray-600'">
              <!-- 爱心图标 -->
              <svg class="w-5 h-5 transition-transform duration-200" :class="{ 'scale-110': playlist.is_liked }"
                :fill="playlist.is_liked ? 'currentColor' : 'none'" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z">
                </path>
              </svg>

              <!-- 文字 -->
              <span>{{ playlist.is_liked ? '已点赞' : '点赞' }} ({{ playlist.like_count }})</span>
            </button>

            <!-- 更多操作 (仅拥有者) -->
            <template v-if="isOwner">
              <div class="w-px h-6 bg-gray-700 mx-1"></div>

              <button @click="showAddSongModal = true"
                class="px-4 py-2 text-sm text-gray-300 hover:text-white hover:bg-white/10 rounded-full transition-colors">
                添加歌曲
              </button>

              <button @click="deletePlaylist"
                class="px-4 py-2 text-sm text-red-400 hover:text-white hover:bg-red-600 rounded-full transition-colors">
                删除歌单
              </button>
            </template>
          </div>
        </div>
      </div>

      <!-- 2. 歌曲列表区域 -->
      <div class="flex-1 overflow-hidden flex flex-col bg-black/20 min-h-0">
        <!-- 列表头 -->
        <div
          class="grid grid-cols-[50px_4fr_3fr_3fr_100px_100px] px-6 py-3 border-b border-gray-800 text-xs text-gray-400 uppercase font-medium shrink-0">
          <div>#</div>
          <div>标题</div>
          <div>歌手</div>
          <div>专辑</div>
          <div class="text-right">时长</div>
          <div class="text-center">操作</div>
        </div>

        <!-- 列表内容 (滚动) -->
        <div class="overflow-y-auto flex-1 custom-scrollbar pb-24">
          <div v-if="playlist.songs.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-500">
            <p>歌单里还没有歌曲</p>
            <button v-if="isOwner" @click="showAddSongModal = true"
              class="mt-4 text-blue-400 hover:text-blue-300 hover:underline">去添加</button>
          </div>

          <div v-else v-for="(song, index) in playlist.songs" :key="song.id"
            class="group grid grid-cols-[50px_4fr_3fr_3fr_100px_100px] px-6 py-3 hover:bg-white/10 rounded-md mx-2 transition-colors items-center text-sm cursor-default"
            @dblclick="playSong(song)">
            <!-- 序号/播放状态 -->
            <div class="text-gray-500 w-8 flex justify-center">
              <!-- 情况 1: 是当前播放的歌 -->
              <div v-if="currentSong?.id === song.id" class="text-green-500">
                <svg v-if="isPlaying" class="w-4 h-4 animate-pulse fill-current" viewBox="0 0 24 24">
                  <path d="M8 5v14l11-7z" />
                </svg>
                <svg v-else class="w-4 h-4 fill-current" viewBox="0 0 24 24">
                  <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" />
                </svg>
              </div>

              <!-- 情况 2: 不是当前歌 (使用 template v-else 包裹下面两个元素) -->
              <template v-else>
                <!-- 默认显示序号 (Hover时隐藏) -->
                <span class="group-hover:hidden font-mono">{{ index + 1 }}</span>

                <!-- Hover时显示播放按钮 (默认隐藏) -->
                <button @click="playSong(song)"
                  class="hidden group-hover:block text-white hover:text-green-500 transition-colors">
                  <svg class="w-4 h-4 fill-current" viewBox="0 0 24 24">
                    <path d="M8 5v14l11-7z" />
                  </svg>
                </button>
              </template>
            </div>

            <!-- 标题 -->
            <div class="text-white font-medium truncate pr-4"
              :class="{ 'text-green-500': currentSong?.id === song.id }">
              {{ song.title }}
            </div>

            <!-- 歌手 -->
            <div class="text-gray-400 truncate pr-4 hover:text-white cursor-pointer">{{ song.artist }}</div>

            <!-- 专辑 -->
            <div class="text-gray-400 truncate pr-4 hover:text-white cursor-pointer">{{ song.album }}</div>

            <!-- 时长 -->
            <div class="text-gray-500 font-mono text-right">{{ formatDuration(song.duration) }}</div>

            <!-- 操作 -->
            <div class="flex justify-center items-center gap-3 opacity-0 group-hover:opacity-100 transition-opacity">
              <button @click="addToQueue(song)" class="text-gray-400 hover:text-white active:scale-90" title="加入队列">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                </svg>
              </button>
              <button v-if="isOwner" @click="removeSong(song.id)" class="text-gray-400 hover:text-red-500"
                title="从歌单移除">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16">
                  </path>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加歌曲模态框 -->
    <div v-if="showAddSongModal"
      class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[60]">
      <div class="bg-gray-800 p-6 rounded-xl w-[500px] shadow-2xl border border-gray-700 flex flex-col max-h-[80vh]">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-xl font-bold text-white">添加歌曲</h3>
          <button @click="showAddSongModal = false" class="text-gray-400 hover:text-white">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <input v-model="songSearchQuery" type="text" placeholder="搜索歌曲标题、歌手..."
          class="w-full px-4 py-3 bg-gray-900 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500 border border-gray-700 mb-4"
          autofocus>

        <div class="flex-1 overflow-y-auto custom-scrollbar min-h-[200px]">
          <div v-if="searchResults.length === 0 && songSearchQuery" class="text-center py-8 text-gray-500">
            未找到相关歌曲
          </div>
          <div v-for="song in searchResults" :key="song.id"
            class="flex items-center justify-between p-3 hover:bg-gray-700 rounded-lg group transition-colors">
            <div class="min-w-0 pr-4">
              <div class="text-white font-medium truncate">{{ song.title }}</div>
              <div class="text-gray-400 text-xs truncate">{{ song.artist }} - {{ song.album }}</div>
            </div>
            <button @click="addSongToPlaylist(song.id)"
              class="px-3 py-1 bg-gray-600 hover:bg-blue-600 text-white text-xs rounded transition-colors">
              添加
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../utils/api'
import { usePlayerStore } from '../stores/player'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()
const { currentSong, isPlaying } = storeToRefs(playerStore)

const playlist = ref(null)
const loading = ref(true)
const showAddSongModal = ref(false)
const songSearchQuery = ref('')
const searchResults = ref([])

// 判断当前用户是否是歌单所有者
const isOwner = computed(() => {
  // 1. 确保已登录且歌单数据已加载
  if (!userStore.isAuthenticated || !userStore.user || !playlist.value) {
    return false
  }

  // 2. 获取双方 ID
  const currentUserId = userStore.user.user_id
  const playlistCreatorId = playlist.value.user.id

  // 3. 打印调试日志（按 F12 看控制台，修好后可删除）
  console.log(`[权限检查] 当前用户ID: ${currentUserId} (${typeof currentUserId})`)
  console.log(`[权限检查] 歌单作者ID: ${playlistCreatorId} (${typeof playlistCreatorId})`)

  // 4. 【核心修复】使用 String() 转为字符串后再比较，或者使用 == (双等号)
  // 推荐转字符串比较，最稳健
  return String(currentUserId) === String(playlistCreatorId)
})

// 处理图片路径
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const API_BASE = 'http://127.0.0.1:5000'
  return `${API_BASE}${url.startsWith('/') ? '' : '/'}${url}`
}

// 加载歌单详情
const loadPlaylist = async () => {
  loading.value = true
  try {
    const response = await api.get(`/api/playlists/${route.params.id}`)
    playlist.value = response.data
  } catch (error) {
    console.error('Failed to load playlist:', error)
    router.push('/playlists') // 出错跳回列表
  } finally {
    loading.value = false
  }
}

// 播放全部：直接替换队列（需要后端支持 create_queue_from_playlist）
// 或者我们在前端实现：清空 -> 批量添加 -> 播放第一首
const playAll = async () => {
  if (!playlist.value?.songs?.length) {
    alert("歌单是空的")
    return
  }

  // 直接调用 store 的新方法
  await playerStore.playPlaylist(playlist.value.id)
}

// 播放单曲：不改变队列，只插入播放
const playSong = (song) => {
  playerStore.playSong(song)
}

// 添加到队列
const addToQueue = async (song) => {
  console.log('点击添加:', song.title)
  await playerStore.addToQueue(song, true)
  // 可以加个 toast 提示
}

// 点赞/取消
const toggleLike = async () => {
  if (!userStore.isAuthenticated) {
    router.push('/login')
    return
  }
  try {
    if (playlist.value.is_liked) {
      await api.post(`/api/playlists/${route.params.id}/unlike`)
      playlist.value.is_liked = false
      playlist.value.like_count--
    } else {
      await api.post(`/api/playlists/${route.params.id}/like`)
      playlist.value.is_liked = true
      playlist.value.like_count++
    }
  } catch (error) {
    console.error('Failed to toggle like:', error)
  }
}

// 删除歌单
const deletePlaylist = async () => {
  if (!confirm('确定要删除这个歌单吗？此操作不可撤销。')) return
  try {
    await api.delete(`/api/playlists/${route.params.id}`)
    router.push('/playlists')
  } catch (error) {
    console.error('Failed to delete playlist:', error)
  }
}

// 从歌单移除歌曲
const removeSong = async (songId) => {
  try {
    await api.delete(`/api/playlists/${route.params.id}/songs/${songId}`)
    // 本地移除，避免重新加载整个页面
    const idx = playlist.value.songs.findIndex(s => s.id === songId)
    if (idx !== -1) playlist.value.songs.splice(idx, 1)
    playlist.value.song_count--
  } catch (error) {
    console.error('Failed to remove song:', error)
  }
}

// 搜索歌曲（防抖建议在生产环境加上）
const searchSongs = async () => {
  if (!songSearchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    const response = await api.get('/api/songs', {
      params: { search: songSearchQuery.value, per_page: 20 }
    })
    searchResults.value = response.data.songs
  } catch (error) {
    console.error('Failed to search songs:', error)
  }
}

// 添加歌曲到歌单
const addSongToPlaylist = async (songId) => {
  try {
    await api.post(`/api/playlists/${route.params.id}/songs`, { song_id: songId })
    // 不关闭模态框，允许连续添加
    // 刷新歌单数据以显示新歌
    await loadPlaylist()
  } catch (error) {
    alert(error.response?.data?.error || '添加失败')
  }
}

const formatDuration = (seconds) => {
  if (!seconds) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

watch(songSearchQuery, searchSongs)

onMounted(loadPlaylist)
</script>

<style scoped>
/* 隐藏滚动条但保留功能 */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: transparent;
  border-radius: 4px;
}

.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.2);
}
</style>