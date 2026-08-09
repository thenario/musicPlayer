<template>
  <el-table v-loading="loading" element-loading-background="rgba(0, 0, 0, 0.5)" :data="songs" style="width: 100%"
    height="100%" row-class-name="song-row group" @row-dblclick="onRowDblClick" class="all-songs-table">
    <el-table-column label="标题" min-width="250">
      <template #default="{ row }">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded overflow-hidden shrink-0 bg-gray-800">
            <img v-if="row.song_cover_url" :src="getImageUrl(row.song_cover_url)" alt="歌曲封面"
              class="w-full h-full object-cover" />
            <el-icon v-else class="w-full h-full flex items-center justify-center text-gray-600">
              <Headset />
            </el-icon>
          </div>
          <div class="truncate">
            <div :class="['font-medium truncate', currentSongId === row.song_id ? 'text-green-500' : 'text-white']">
              {{ row.song_title }}
            </div>
            <div class="text-xs text-gray-500 truncate">{{ row.artist }}</div>
          </div>
        </div>
      </template>
    </el-table-column>

    <el-table-column prop="album" label="专辑" min-width="150" class-name="text-gray-400" />

    <el-table-column label="时长" width="100" align="right">
      <template #default="{ row }">
        <span class="text-gray-500 text-sm">{{ formatDuration(row.duration) }}</span>
      </template>
    </el-table-column>

    <el-table-column label="操作" width="160" align="right">
      <template #default="{ row }">
        <div class="flex justify-end items-center gap-3 opacity-0 group-hover:opacity-100 transition-opacity pr-2">
          <el-tooltip content="立即播放" placement="top">
            <el-icon class="action-icon text-blue-400 hover:text-blue-300" :size="20" @click="emit('play-now', row)">
              <VideoPlay />
            </el-icon>
          </el-tooltip>
          <el-tooltip content="下一首播放" placement="top">
            <el-icon class="action-icon text-gray-400 hover:text-white" :size="20" @click="emit('play-next', row)">
              <List />
            </el-icon>
          </el-tooltip>
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { Headset, VideoPlay, List } from '@element-plus/icons-vue'
import type { ISong } from '@/types'
import { formatDuration, getImageUrl } from '@/utils/format'

defineProps<{
  songs: ISong[]
  loading: boolean
  currentSongId?: number
}>()

const emit = defineEmits<{
  (e: 'play-now', song: ISong): void
  (e: 'play-next', song: ISong): void
}>()

const onRowDblClick = (row: ISong) => emit('play-now', row)
</script>
