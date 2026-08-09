<template>
  <router-link :to="`/playlists/${playlist.playlist_id}`" class="group cursor-pointer block">
    <div class="aspect-square bg-gray-800 rounded-lg overflow-hidden mb-2">
      <img :src="playlist.playlist_cover_url || '/default-cover.png'" alt="歌单封面"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform">
    </div>
    <p class="font-medium truncate text-white">{{ playlist.playlist_name || playlist.name }}</p>
  </router-link>
</template>

<script setup lang="ts">
defineProps<{ playlist: any }>()
</script>
