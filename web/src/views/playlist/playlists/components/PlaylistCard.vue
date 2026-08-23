<template>
  <router-link :to="`/playlists/${playlist.playlist_id}`" class="playlist-card">
    <div class="playlist-card__cover">
      <img :src="playlist.playlist_cover_url || '/default-cover.svg'" alt="歌单封面"
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
.playlist-card {
  display: block;
  min-width: 0;
  color: inherit;
  text-decoration: none;
  cursor: pointer;
}

.playlist-card__cover {
  aspect-ratio: 1;
  overflow: hidden;
  border: 1px solid #e8eaef;
  border-radius: 12px;
  background: #eef0f4;
  box-shadow: 0 2px 6px rgb(31 35 45 / 5%);
  transition: border-color 160ms ease, box-shadow 160ms ease;
}

.playlist-card__img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 220ms ease;
}

.playlist-card:hover .playlist-card__cover {
  border-color: #c9c3ea;
  box-shadow: 0 8px 18px rgb(31 35 45 / 10%);
}

.playlist-card:hover .playlist-card__img {
  transform: scale(1.04);
}

.playlist-card__name {
  margin: 10px 2px 0;
  overflow: hidden;
  color: #434957;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 160ms ease;
}

.playlist-card:hover .playlist-card__name {
  color: #6256c5;
}
</style>
