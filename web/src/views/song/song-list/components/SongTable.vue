<template>
  <el-table ref="tableRef" v-loading="loading" element-loading-background="rgba(255, 255, 255, 0.72)" :data="songs" style="width: 100%"
    height="100%" row-class-name="song-row" @row-dblclick="onRowDblClick" class="all-songs-table">
    <el-table-column label="标题" min-width="32%" show-overflow-tooltip>
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

    <el-table-column prop="album" label="专辑" min-width="20%" class-name="song-table__album" show-overflow-tooltip />

    <el-table-column label="时长" min-width="20%" align="right">
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
import { ref } from 'vue'
import type { TableInstance } from 'element-plus'
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

const tableRef = ref<TableInstance>()

const onRowDblClick = (row: ISong) => emit('play-now', row)

const scrollToTop = () => tableRef.value?.setScrollTop(0)

defineExpose({ scrollToTop })
</script>

<style scoped>
.all-songs-table {
  --el-table-border-color: #eef0f4;
  --el-table-header-bg-color: #fafbfc;
  --el-table-header-text-color: #7b8190;
  --el-table-row-hover-bg-color: #e4ebf7;
  --el-table-text-color: #4f5563;
}

:deep(.all-songs-table .el-table__header-wrapper th.el-table__cell) {
  height: 46px;
  font-size: 12px;
  font-weight: 600;
}

:deep(.all-songs-table .el-table__body-wrapper td.el-table__cell) {
  height: 64px;
  border-bottom-color: #f0f1f4;
}

:deep(.all-songs-table .song-table__album) {
  color: #8b91a0;
}

.song-table__title-cell {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.song-table__cover {
  width: 40px;
  height: 40px;
  display: grid;
  flex-shrink: 0;
  overflow: hidden;
  place-items: center;
  border: 1px solid #eceef3;
  border-radius: 9px;
  background: #f2f3f6;
}

.song-table__cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-table__cover-icon {
  color: #9aa0ad;
}

.song-table__title-wrap {
  min-width: 0;
}

.song-table__title {
  overflow: hidden;
  color: #2d3340;
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-table__title.is-active {
  color: #6256c5;
}

.song-table__artist {
  overflow: hidden;
  margin-top: 3px;
  color: #969cab;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-table__duration {
  color: #969cab;
  font-size: 13px;
}

.song-table__action-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding-right: 4px;
  opacity: 0.35;
  transition: opacity 160ms ease;
}

:deep(.song-row:hover .song-table__action-bar) {
  opacity: 1;
}

.action-icon {
  cursor: pointer;
  transition: transform 160ms ease, color 160ms ease;
}

.action-icon:hover {
  transform: scale(1.08);
}

.song-table__play-icon:hover {
  color: #6256c5;
}

.song-table__next-icon {
  color: #9097a7;
}

.song-table__next-icon:hover {
  color: #4f5563;
}
</style>
