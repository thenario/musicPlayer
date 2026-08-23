<template>
  <div class="playlist-header">
    <div class="playlist-header__banner">
      <div class="playlist-header__cover">
        <img v-if="playlist.playlist_cover_url" :src="getImageUrl(playlist.playlist_cover_url)" alt="歌曲封面"
          class="playlist-header__cover-img" />
        <div v-else class="playlist-header__cover-placeholder">
          <el-icon :size="64">
            <Picture />
          </el-icon>
        </div>
      </div>

      <div class="playlist-header__info">
        <p class="playlist-header__label">歌单</p>
        <h1 class="playlist-header__title">{{ playlist.playlist_name }}</h1>

        <div class="playlist-header__meta">
          <el-avatar :size="24" class="playlist-header__avatar">
            {{ userName.charAt(0).toUpperCase() }}
          </el-avatar>
          <span class="playlist-header__username">{{ userName }}</span>
          <span class="playlist-header__sep">•</span>
          <span>{{ playlist.song_count || 0 }} 首歌曲</span>
          <span class="playlist-header__sep">•</span>
          <span>{{ playlist.play_count || 0 }} 次播放</span>
        </div>
      </div>
    </div>

    <div class="playlist-header__actions">
      <div class="playlist-header__actions-group">
        <button @click="emit('play-all')"
          class="playlist-header__play">
          <el-icon :size="30" color="black" class="playlist-header__play-icon">
            <CaretRight />
          </el-icon>
        </button>

        <el-tooltip :content="isLiked ? '取消点赞' : '点赞'">
          <el-icon :size="32" class="playlist-header__like"
            :class="isLiked ? 'is-active' : 'is-idle'" @click="emit('toggle-like')">
            <StarFilled v-if="isLiked" />
            <Star v-else />
          </el-icon>
        </el-tooltip>

        <el-dropdown v-if="isOwner" trigger="click">
          <el-icon :size="28" class="playlist-header__more">
            <MoreFilled />
          </el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="emit('add-song')" :icon="Plus">添加歌曲到歌单</el-dropdown-item>
              <el-dropdown-item @click="emit('delete')" :icon="Delete" divided class="playlist-header__delete">删除歌单</el-dropdown-item>
              <router-link :to="`/playlists/${playlist.playlist_id}/edit`" class="playlist-header__edit-link">
                <el-dropdown-item :icon="Edit">编辑歌单信息</el-dropdown-item>
              </router-link>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { IPlaylist } from '@/types'
import { Picture, CaretRight, StarFilled, Star, MoreFilled, Plus, Delete, Edit } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/format'

defineProps<{
  playlist: IPlaylist
  isOwner: boolean
  isLiked: boolean
  userName: string
}>()

const emit = defineEmits<{
  (e: 'play-all'): void
  (e: 'toggle-like'): void
  (e: 'add-song'): void
  (e: 'delete'): void
}>()
</script>

<style scoped>
.playlist-header {
  flex-shrink: 0;
}

.playlist-header__banner {
  display: flex;
  gap: 28px;
  align-items: flex-end;
  padding: 28px 32px;
  background: linear-gradient(135deg, #eaf4ff, #f8fbff 60%, #fff);
  border-bottom: 1px solid #e5e7eb;
}

.playlist-header__cover {
  position: relative;
  flex-shrink: 0;
  width: 192px;
  height: 192px;
  overflow: hidden;
  border-radius: 14px;
  box-shadow: 0 10px 24px rgb(15 23 42 / 16%);
}

.playlist-header__cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .5s ease;
}

.playlist-header__cover:hover .playlist-header__cover-img {
  transform: scale(1.06);
}

.playlist-header__cover-placeholder {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #eef2f6;
}

.playlist-header__info {
  flex: 1;
  min-width: 0;
}

.playlist-header__label {
  margin: 0 0 8px;
  color: #409eff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: .08em;
}

.playlist-header__title {
  margin: 0 0 18px;
  overflow: hidden;
  color: #303133;
  font-size: clamp(28px, 4vw, 46px);
  font-weight: 800;
  line-height: 1.15;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-header__meta {
  display: flex;
  gap: 8px;
  align-items: center;
  color: #606266;
  font-size: 14px;
}

.playlist-header__avatar {
  background: #409eff;
  font-weight: 700;
}

.playlist-header__username {
  color: #303133;
  font-weight: 700;
}

.playlist-header__sep {
  opacity: .5;
}

.playlist-header__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 32px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.playlist-header__actions-group {
  display: flex;
  gap: 20px;
  align-items: center;
}

.playlist-header__play {
  display: flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #67c23a;
  border-radius: 50%;
  box-shadow: 0 6px 14px rgb(103 194 58 / 25%);
  transition: transform .2s ease, background-color .2s ease;
}

.playlist-header__play:hover {
  background: #529b2e;
  transform: scale(1.05);
}

.playlist-header__play:active {
  transform: scale(.95);
}

.playlist-header__play-icon {
  margin-left: 2px;
}

.playlist-header__like {
  cursor: pointer;
  transition: color .2s ease;
}

.playlist-header__like.is-active {
  color: #f56c6c;
}

.playlist-header__like.is-idle {
  color: #909399;
}

.playlist-header__like.is-idle:hover {
  color: #303133;
}

.playlist-header__more {
  color: #909399;
  cursor: pointer;
}

.playlist-header__more:hover {
  color: #303133;
}

.playlist-header__delete {
  color: #f56c6c;
}

.playlist-header__edit-link {
  text-decoration: none;
}

@media (max-width: 640px) {
  .playlist-header__banner {
    gap: 18px;
    align-items: center;
    padding: 22px 20px;
  }

  .playlist-header__cover {
    width: 112px;
    height: 112px;
  }

  .playlist-header__title {
    margin-bottom: 10px;
    font-size: 27px;
  }

  .playlist-header__actions {
    padding: 16px 20px;
  }
}
</style>
