<template>
  <el-table :data="songs" style="width: 100%" row-class-name="song-row" @row-dblclick="onRowDblClick"
    class="playlist-table">

    <el-table-column label="标题" min-width="24%">
      <template #default="{ row }">
        <div class="playlist-song-table__title-wrap">
          <span class="playlist-song-table__title"
            :class="{ 'is-current': sameId(currentSongId, row.song_id) }">
            {{ row.song_title }}
          </span>
          <el-icon v-if="sameId(currentSongId, row.song_id) && isPlaying" class="playlist-song-table__playing">
            <VideoPlay />
          </el-icon>
        </div>
      </template>
    </el-table-column>

    <el-table-column prop="artist" label="歌手" min-width="24%" />
    <el-table-column prop="album" label="专辑" min-width="20%" />

    <el-table-column label="时长" width="100" align="right">
      <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
    </el-table-column>

    <el-table-column label="操作" width="180" align="right">
      <template #default="{ row }">
        <div class="playlist-song-table__actions">
          <el-tooltip content="立即播放">
            <el-icon class="playlist-song-table__action-play" :size="20"
              @click="emit('play-song', row as ISong)">
              <VideoPlay />
            </el-icon>
          </el-tooltip>

          <el-tooltip content="下一首播放">
            <el-icon class="playlist-song-table__action-next" :size="20"
              @click="emit('play-next', row as ISong)">
              <List />
            </el-icon>
          </el-tooltip>

          <el-tooltip content="添加到队列">
            <el-icon class="playlist-song-table__action-queue" :size="20"
              @click="emit('add-to-queue', row as ISong)">
              <CirclePlus />
            </el-icon>
          </el-tooltip>

          <el-tooltip v-if="isOwner" content="从歌单移除">
            <el-icon class="playlist-song-table__action-remove" :size="18"
              @click="emit('remove-song', row.song_id)">
              <Delete />
            </el-icon>
          </el-tooltip>
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { Delete, VideoPlay, List, CirclePlus } from '@element-plus/icons-vue'
import type { ISong } from '@/types'
import { formatDuration, sameId } from '@/utils/format'

defineProps<{
  songs: ISong[]
  isOwner: boolean
  currentSongId?: number | string
  isPlaying: boolean
}>()

const emit = defineEmits<{
  (e: 'play-song', song: ISong): void
  (e: 'play-next', song: ISong): void
  (e: 'add-to-queue', song: ISong): void
  (e: 'remove-song', songId: number | string): void
}>()

const onRowDblClick = (row: ISong) => emit('play-song', row)
</script>

<style scoped>
.playlist-song-table__title-wrap {
  display: flex;
  gap: 12px;
  align-items: center;
}

.playlist-song-table__title.is-current {
  color: #67c23a;
  font-weight: 700;
}

.playlist-song-table__playing {
  color: #67c23a;
  animation: playlist-song-table-bounce 1s infinite;
}

.playlist-song-table__actions {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
  opacity: 0;
  transition: opacity .2s ease;
}

:deep(.song-row):hover .playlist-song-table__actions {
  opacity: 1;
}

.playlist-song-table__action-play {
  color: #409eff;
  cursor: pointer;
}

.playlist-song-table__action-play:hover {
  color: #337ecc;
}

.playlist-song-table__action-next {
  color: #909399;
  cursor: pointer;
}

.playlist-song-table__action-next:hover {
  color: #303133;
}

.playlist-song-table__action-queue {
  color: #909399;
  cursor: pointer;
}

.playlist-song-table__action-queue:hover {
  color: #303133;
}

.playlist-song-table__action-remove {
  color: #909399;
  cursor: pointer;
}

.playlist-song-table__action-remove:hover {
  color: #f56c6c;
}

@keyframes playlist-song-table-bounce {
  50% { transform: translateY(-2px); }
}
</style>
