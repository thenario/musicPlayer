<template>
  <router-link :to="`/playlists/${playlist.playlist_id}`" class="playlist-card">
    <div class="playlist-card__cover">
      <img :src="playlist.playlist_cover_url || '/default-cover.svg'" alt="歌单封面"
        class="playlist-card__img">
    </div>
    <p class="playlist-card__name">{{ playlist.playlist_name }}</p>
    <p class="playlist-card__meta">{{ playlist.song_count ?? 0 }} 首歌曲 <span aria-hidden="true">·</span> 私人唱片架</p>
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
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  border: 1px solid var(--music-border);
  border-radius: 6px;
  background: var(--music-raised);
  box-shadow: 0 4px 12px rgb(31 35 45 / 6%);
  transition: border-color 180ms ease, box-shadow 180ms ease, transform 180ms ease;
}

.playlist-card__cover::after {
  position: absolute;
  right: 14px;
  bottom: 14px;
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 50%;
  background: var(--music-primary);
  color: var(--music-on-primary);
  font-size: 23px;
  opacity: 0;
  transform: translateY(8px);
  transition: opacity 180ms ease, transform 180ms ease;
  content: '↗';
}

.playlist-card:hover .playlist-card__cover::after,
.playlist-card:focus-visible .playlist-card__cover::after {
  opacity: 1;
  transform: translateY(0);
}

.playlist-card__meta {
  margin: 6px 3px 0;
  color: var(--music-muted);
  font-size: 11px;
}

.playlist-card__meta span { margin-inline: 4px; }

.playlist-card__img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 220ms ease;
}

.playlist-card:hover .playlist-card__cover {
  border-color: var(--music-primary-hover);
  transform: translateY(-3px);
  box-shadow: 0 8px 18px rgb(31 35 45 / 10%);
}

.playlist-card:hover .playlist-card__img {
  transform: scale(1.04);
}

.playlist-card__name {
  margin: 12px 3px 0;
  overflow: hidden;
  color: var(--music-secondary);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 160ms ease;
}

.playlist-card:hover .playlist-card__name {
  color: var(--music-primary);
}
</style>
