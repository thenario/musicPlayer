<template>
  <div class="shrink-0">
    <div class="p-8 bg-linear-to-b from-blue-900/40 to-gray-950 flex items-end gap-8 border-b border-white/5">
      <div class="w-56 h-56 rounded-xl shadow-2xl overflow-hidden shrink-0 group relative">
        <img v-if="playlist.playlist_cover_url" :src="getImageUrl(playlist.playlist_cover_url)" alt="歌曲封面"
          class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
        <div v-else class="w-full h-full bg-gray-800 flex flex-col items-center justify-center text-gray-500">
          <el-icon :size="64">
            <Picture />
          </el-icon>
        </div>
      </div>

      <div class="flex-1 min-w-0">
        <p class="text-xs font-bold uppercase tracking-wider mb-2 text-blue-400">歌单</p>
        <h1 class="text-6xl font-black mb-6 truncate">{{ playlist.playlist_name }}</h1>

        <div class="flex items-center gap-2 text-sm text-gray-300">
          <el-avatar :size="24" class="bg-blue-600 font-bold">
            {{ userName.charAt(0).toUpperCase() }}
          </el-avatar>
          <span class="font-bold text-white">{{ userName }}</span>
          <span class="opacity-50">•</span>
          <span>{{ playlist.song_count || 0 }} 首歌曲</span>
          <span class="opacity-50">•</span>
          <span>{{ playlist.play_count || 0 }} 次播放</span>
        </div>
      </div>
    </div>

    <div class="p-6 flex items-center justify-between">
      <div class="flex items-center gap-6">
        <button @click="emit('play-all')"
          class="w-14 h-14 bg-green-500 rounded-full flex items-center justify-center shadow-lg hover:scale-105 transition-transform active:scale-95 group">
          <el-icon :size="30" color="black" class="ml-1">
            <CaretRight />
          </el-icon>
        </button>

        <el-tooltip :content="isLiked ? '取消点赞' : '点赞'">
          <el-icon :size="32" class="cursor-pointer transition-colors"
            :class="isLiked ? 'text-red-500' : 'text-gray-400 hover:text-white'" @click="emit('toggle-like')">
            <StarFilled v-if="isLiked" />
            <Star v-else />
          </el-icon>
        </el-tooltip>

        <el-dropdown v-if="isOwner" trigger="click">
          <el-icon :size="28" class="text-gray-400 hover:text-white cursor-pointer">
            <MoreFilled />
          </el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="emit('add-song')" :icon="Plus">添加歌曲到歌单</el-dropdown-item>
              <el-dropdown-item @click="emit('delete')" :icon="Delete" divided class="text-red-500">删除歌单</el-dropdown-item>
              <router-link :to="`/playlists/${playlist.playlist_id}/edit`" class="no-underline">
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
