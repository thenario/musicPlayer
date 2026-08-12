<template>
  <el-table v-loading="loading" element-loading-background="rgba(0, 0, 0, 0.5)" :data="songs" style="width: 100%"
    height="100%" row-class-name="song-row group" @row-dblclick="onRowDblClick" class="all-songs-table">
    <el-table-column label="标题" min-width="250">
      <template #default="{ row }">
        <div class="song-table__title-cell">
          <div class="song-table__cover">
            <img v-if="row.song_cover_url" :src="getImageUrl(row.song_cover_url)" alt="歌曲封面"
              class="song-table__cover-img" />
            <el-icon v-else class="song-table__cover-icon">
              <Headset />
            </el-icon>
          </div>
          <div class="song-table__title-wrap">
            <div :class="['song-table__title', sameId(currentSongId, row.song_id) ? 'is-active' : 'is-idle']">
              {{ row.song_title }}
            </div>
            <div class="song-table__artist">{{ row.artist }}</div>
          </div>
        </div>
      </template>
    </el-table-column>

    <el-table-column prop="album" label="专辑" min-width="150" class-name="text-gray-400" />

    <el-table-column label="时长" width="100" align="right">
      <template #default="{ row }">
        <span class="song-table__duration">{{ formatDuration(row.duration) }}</span>
      </template>
    </el-table-column>

    <el-table-column label="操作" width="160" align="right">
      <template #default="{ row }">
        <div class="song-table__action-bar">
          <el-tooltip content="立即播放" placement="top">
            <el-icon class="song-table__play-icon action-icon" :size="20" @click="emit('play-now', row as ISong)">
              <VideoPlay />
            </el-icon>
          </el-tooltip>
          <el-tooltip content="下一首播放" placement="top">
            <el-icon class="song-table__next-icon action-icon" :size="20" @click="emit('play-next', row as ISong)">
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
import { formatDuration, getImageUrl, sameId } from '@/utils/format'

defineProps<{
  songs: ISong[]
  loading: boolean
  currentSongId?: number | string
}>()

const emit = defineEmits<{
  (e: 'play-now', song: ISong): void
  (e: 'play-next', song: ISong): void
}>()

const onRowDblClick = (row: ISong) => emit('play-now', row)
</script>

<style scoped>
@reference "../../../../assets/index.css";

.song-table__title-cell {
  @apply flex items-center gap-3;
}

.song-table__cover {
  @apply w-10 h-10 rounded overflow-hidden shrink-0 bg-gray-800;
}

.song-table__cover-img {
  @apply w-full h-full object-cover;
}

.song-table__cover-icon {
  @apply w-full h-full flex items-center justify-center text-gray-600;
}

.song-table__title-wrap {
  @apply truncate;
}

.song-table__title {
  @apply font-medium truncate;
}

.song-table__title.is-active {
  @apply text-green-500;
}

.song-table__title.is-idle {
  @apply text-white;
}

.song-table__artist {
  @apply text-xs text-gray-500 truncate;
}

.song-table__duration {
  @apply text-gray-500 text-sm;
}

.song-table__action-bar {
  @apply flex justify-end items-center gap-3 opacity-0 transition-opacity pr-2;
}

:deep(.song-row:hover .song-table__action-bar) {
  @apply opacity-100;
}

.song-table__play-icon {
  @apply text-blue-400;
}

.song-table__play-icon:hover {
  @apply text-blue-300;
}

.song-table__next-icon {
  @apply text-gray-400;
}

.song-table__next-icon:hover {
  @apply text-white;
}
</style>
