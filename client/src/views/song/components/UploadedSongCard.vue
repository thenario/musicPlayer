<template>
  <div class="song-item-card group">
    <div class="flex items-center p-4">
      <!-- 歌曲封面 -->
      <el-image :src="getImageUrl(song.song_cover_url)" class="w-16 h-16 rounded-lg shadow-sm shrink-0" fit="cover">
        <template #error>
          <div class="w-full h-full bg-gray-100 flex items-center justify-center text-gray-400">
            <el-icon :size="24">
              <Mic />
            </el-icon>
          </div>
        </template>
      </el-image>

      <!-- 歌曲详情 -->
      <div class="ml-4 grow overflow-hidden">
        <h3 class="text-lg font-semibold text-gray-800 truncate group-hover:text-indigo-600 transition-colors">
          {{ song.song_title }}
        </h3>
        <p class="text-sm text-gray-500 truncate">{{ song.artist || '未知艺术家' }}</p>
        <div class="flex items-center mt-1 text-xs text-gray-400">
          <el-icon class="mr-1">
            <Calendar />
          </el-icon>
          {{ formatDate(song.date_added) }}
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="flex gap-2 ml-4">
        <el-tooltip content="编辑详情" placement="top">
          <el-button circle @click="emit('edit', song)"
            class="hover:bg-indigo-50 hover:text-indigo-600 border-none">
            <el-icon>
              <EditPen />
            </el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Mic, Calendar, EditPen } from '@element-plus/icons-vue'
import { getImageUrl, formatDate } from '@/utils/format'

defineProps<{ song: any }>()
const emit = defineEmits<{
  (e: 'edit', song: any): void
}>()
</script>

<style scoped>
.song-item-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #f3f4f6;
  transition: all 0.3s ease;
}

.song-item-card:hover {
  transform: translateX(4px);
  box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.05);
  border-color: #e5e7eb;
}
</style>
