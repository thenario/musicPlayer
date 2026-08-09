<template>
  <router-link :to="`/playlists/${playlist.playlist_id}`" class="playlist-card">
    <div class="playlist-card__cover">
      <img :src="playlist.playlist_cover_url || '/default-cover.png'" alt="歌单封面"
        class="playlist-card__img">
    </div>
    <p class="playlist-card__name">{{ playlist.playlist_name }}</p>
  </router-link>
</template>

<script setup lang="ts">
import type { IPlaylist } from '@/types'

defineProps<{ playlist: IPlaylist }>()
</script>

<style scoped>
@reference "../../../../assets/index.css";

.playlist-card {
  @apply block cursor-pointer;
}

.playlist-card__cover {
  @apply mb-2 aspect-square overflow-hidden rounded-lg bg-gray-800;
}

.playlist-card__img {
  @apply h-full w-full object-cover transition-transform;
}

.playlist-card:hover .playlist-card__img {
  @apply scale-105;
}

.playlist-card__name {
  @apply truncate font-medium text-white;
}
</style>
