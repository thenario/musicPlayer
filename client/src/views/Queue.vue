<template>
  <div class="flex h-full gap-6">
    <!-- 左侧：队列列表 (Sidebar) -->
    <div class="w-64 flex-shrink-0 bg-gray-800 rounded-lg p-4 flex flex-col">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-bold text-white">我的队列</h2>
        <span class="text-xs text-gray-400">{{ userQueues.length }}/5</span>
      </div>

      <div class="flex-1 overflow-y-auto space-y-2">
        <div 
          v-for="q in userQueues" 
          :key="q.id"
          @click="handleSwitchQueue(q.id)"
          class="p-3 rounded-lg cursor-pointer transition-colors group relative"
          :class="q.id === currentQueueId ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-300 hover:bg-gray-600'"
        >
          <div class="font-medium truncate pr-6">{{ q.name }}</div>
          <!-- 注意：这里的 item_count 可能滞后，但没关系 -->
          <div class="text-xs opacity-70 mt-1">{{ q.item_count }} 首歌曲</div>
          <div class="text-xs opacity-50 mt-1">{{ formatDate(q.updated_date) }}</div>

          <!-- 删除按钮 -->
          <button 
            @click.stop="handleDeleteQueue(q.id)"
            class="absolute top-2 right-2 p-1 rounded hover:bg-red-500 hover:text-white opacity-0 group-hover:opacity-100 transition-opacity"
            title="删除队列"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>
      </div>
      
      <div class="mt-4 pt-4 border-t border-gray-700 text-xs text-gray-500 text-center">
        添加歌曲时会自动创建新队列
      </div>
    </div>

    <!-- 右侧：当前队列详情 (Main Content) -->
    <div class="flex-1 min-w-0 flex flex-col">
      <div class="flex justify-between items-center mb-6">
        <div>
           <h1 class="text-3xl font-bold">{{ currentQueueName }}</h1>
           <p class="text-gray-400 text-sm mt-1">
             当前播放队列 
             <span v-if="playMode === 'shuffle'" class="text-green-400 ml-2 text-xs border border-green-500 rounded px-1">随机模式生效中</span>
           </p>
        </div>
        
        <div class="flex space-x-4">
          <select 
            v-model="currentPlayMode"
            @change="setPlayMode"
            class="px-4 py-2 bg-gray-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="sequential">顺序播放</option>
            <option value="shuffle">随机播放</option>
            <option value="repeat_one">单曲循环</option>
            <option value="repeat_all">列表循环</option>
          </select>
          <button 
            @click="clearQueue"
            class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
          >
            清空当前歌曲
          </button>
        </div>
      </div>

      <!-- 歌曲列表表格 -->
      <div class="bg-gray-800 rounded-lg overflow-hidden flex-1 flex flex-col">
        <div class="overflow-y-auto">
          <table class="w-full">
            <thead class="bg-gray-700 sticky top-0">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider w-12">#</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">歌曲</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">艺术家</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">时长</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-700">
              <!-- 这里直接遍历 currentQueue，它是歌曲数组 -->
              <!-- 注意：这里 item 就是 song 对象了，不再是 {song: ...} 结构 -->
              <tr 
                v-for="(item, index) in currentQueue" 
                :key="item.song.id"
                class="hover:bg-gray-700 group"
                :class="{ 'bg-blue-900 bg-opacity-20': item.song.id === currentSong?.id }"
              >
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-300">
                  <div class="flex items-center">
                    <!-- 显示序号或动画 -->
                    <span v-if="item.song.id !== currentSong?.id || !isPlaying">{{ index + 1 }}</span>
                    <span v-else class="flex space-x-1">
                      <span class="w-1 h-3 bg-green-500 animate-pulse"></span>
                      <span class="w-1 h-3 bg-green-500 animate-pulse delay-75"></span>
                      <span class="w-1 h-3 bg-green-500 animate-pulse delay-150"></span>
                    </span>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-medium text-white" :class="{'text-green-400': item.song.id === currentSong?.id}">
                    {{ item.song.title }}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{{ item.song.artist }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{{ formatDuration(item.song.duration) }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <!-- 播放按钮：现在直接用 index 播放 -->
                  <button @click="playFromQueue(index)" class="text-blue-400 hover:text-blue-300 mr-3 hidden group-hover:inline-block">播放</button>
                  <!-- 移除按钮：需要获取 队列项ID(queue_item_id)，但现在的 currentQueue 只是纯歌曲数组 -->
                  <!-- 这是一个难点：如果 currentQueue 仅是 Song[], 我们不知道它在数据库里的 item_id -->
                  <!-- 临时方案：我们暂时禁用单个移除，或者需要后端返回数据时带上 item_id -->
                  <button @click="removeFromQueue(item.id)" class="text-red-400 hover:text-red-300 hidden group-hover:inline-block">移除</button>
                </td>
              </tr>
              
              <!-- 空状态 -->
              <tr v-if="currentQueue.length === 0">
                 <td colspan="5" class="px-6 py-12 text-center text-gray-500">
                    此队列为空，去添加一些歌曲吧
                 </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'
import api from '../utils/api'

const playerStore = usePlayerStore()
const { currentQueue, userQueues, currentSong, isPlaying, playMode } = storeToRefs(playerStore)

// 计算当前队列的 ID (我们之前 Hack 把它挂在数组上了)
const currentQueueId = computed(() => currentQueue.value?.id)

// 计算队列名称
const currentQueueName = computed(() => {
  // 尝试从 userQueues 列表里找当前队列的名字
  if (currentQueueId.value) {
      const q = userQueues.value.find(q => q.id === currentQueueId.value)
      if (q) return q.name
  }
  return '当前播放列表'
})

const currentPlayMode = computed({
  get: () => playMode.value,
  set: (val) => playerStore.setPlayMode(val)
})

const handleSwitchQueue = async (queueId) => {
  await playerStore.switchQueue(queueId)
}

const handleDeleteQueue = async (queueId) => {
  if (confirm('确定要删除这个队列吗？')) {
    await playerStore.deleteQueue(queueId)
  }
}

const clearQueue = async () => {
  if (confirm('确定要清空当前队列的所有歌曲吗？')) {
    // 1. 调用后端清空
    await api.post('/api/queue/clear', {})
    // 2. 重新加载队列状态 (这会清空前端的 currentQueue)
    await playerStore.fetchCurrentQueue()
  }
}

// 【修改点】现在直接按索引播放，这是前端驱动的核心优势
const playFromQueue = (index) => {
  playerStore.playAtIndex(index)
}

// 【修改点】移除逻辑比较麻烦，因为 currentQueue 现在是纯歌曲
// 我们需要知道这首歌对应的 PlayQueueItem ID 才能删
// 临时解决方案：先不传 item_id，让后端写一个新接口：按 song_id 从当前队列删除
// 或者，我们在 store.fetchCurrentQueue 里，把 structure 改回包含 item_id 的对象
const removeFromQueue = async (itemId) => {
  // 直接调用 store 的新方法
  await playerStore.removeQueueItem(itemId)
}

const formatDuration = (seconds) => {
  if (!seconds) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const formatDate = (dateStr) => {
    if(!dateStr) return ''
    const date = new Date(dateStr)
    return `${date.getMonth()+1}月${date.getDate()}日`
}

onMounted(async () => {
  await Promise.all([
    playerStore.fetchCurrentQueue(),
    playerStore.fetchUserQueues()
  ])
})
</script>