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
@reference "../../../../assets/index.css";

.playlist-header {
  @apply shrink-0;
}

.playlist-header__banner {
  @apply p-8 bg-linear-to-b from-blue-900/40 to-gray-950 flex items-end gap-8 border-b border-white/5;
}

.playlist-header__cover {
  @apply w-56 h-56 rounded-xl shadow-2xl overflow-hidden shrink-0 relative;
}

.playlist-header__cover-img {
  @apply w-full h-full object-cover transition-transform duration-700;
}

.playlist-header__cover:hover .playlist-header__cover-img {
  @apply scale-110;
}

.playlist-header__cover-placeholder {
  @apply w-full h-full bg-gray-800 flex flex-col items-center justify-center text-gray-500;
}

.playlist-header__info {
  @apply flex-1 min-w-0;
}

.playlist-header__label {
  @apply text-xs font-bold uppercase tracking-wider mb-2 text-blue-400;
}

.playlist-header__title {
  @apply text-6xl font-black mb-6 truncate;
}

.playlist-header__meta {
  @apply flex items-center gap-2 text-sm text-gray-300;
}

.playlist-header__avatar {
  @apply bg-blue-600 font-bold;
}

.playlist-header__username {
  @apply font-bold text-white;
}

.playlist-header__sep {
  @apply opacity-50;
}

.playlist-header__actions {
  @apply p-6 flex items-center justify-between;
}

.playlist-header__actions-group {
  @apply flex items-center gap-6;
}

.playlist-header__play {
  @apply w-14 h-14 bg-green-500 rounded-full flex items-center justify-center shadow-lg transition-transform;
}

.playlist-header__play:hover {
  @apply scale-105;
}

.playlist-header__play:active {
  @apply scale-95;
}

.playlist-header__play-icon {
  @apply ml-1;
}

.playlist-header__like {
  @apply cursor-pointer transition-colors;
}

.playlist-header__like.is-active {
  @apply text-red-500;
}

.playlist-header__like.is-idle {
  @apply text-gray-400;
}

.playlist-header__like.is-idle:hover {
  @apply text-white;
}

.playlist-header__more {
  @apply text-gray-400 cursor-pointer;
}

.playlist-header__more:hover {
  @apply text-white;
}

.playlist-header__delete {
  @apply text-red-500;
}

.playlist-header__edit-link {
  @apply no-underline;
}
</style>
