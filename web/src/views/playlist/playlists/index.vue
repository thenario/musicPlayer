<template>
  <div class="playlists-page">
    <div class="playlists-page__header">
      <h1 class="playlists-page__title">我的歌单</h1>
    </div>

    <div class="playlists-page__grid">
      <div v-if="userStore.isAuthenticated" @click="showModal = true"
        class="playlists-page__create">
        <span class="playlists-page__create-icon">+</span>
        <span class="playlists-page__create-text">新建歌单</span>
      </div>

      <PlaylistCard v-for="item in playlists" :key="item.playlist_id" :playlist="item" />
    </div>

    <CreatePlaylistModal v-model:open="showModal" :loading="creating" @create="handleCreate" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'PlaylistsPage' })
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import PlaylistCard from './components/PlaylistCard.vue'
import CreatePlaylistModal from './components/CreatePlaylistModal.vue'
import { usePlaylistList } from './composables/usePlaylistList'

const userStore = useUserStore()
const showModal = ref(false)
const { playlists, creating, load, create } = usePlaylistList()

const handleCreate = async (payload: { name: string; description: string; coverFile: File | null }) => {
  const ok = await create({ ...payload, creatorId: userStore.user?.user_id })
  if (ok) showModal.value = false
}

onMounted(load)
</script>

<style scoped>
@reference "../../../assets/index.css";

.playlists-page {
  @apply p-6 bg-gray-900 min-h-screen text-white;
}

.playlists-page__header {
  @apply flex justify-between items-center mb-6;
}

.playlists-page__title {
  @apply text-2xl font-bold;
}

.playlists-page__grid {
  @apply grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-6;
}

.playlists-page__create {
  @apply aspect-square bg-gray-800 border-2 border-dashed border-gray-700 rounded-xl flex flex-col items-center justify-center cursor-pointer transition-all;
}

.playlists-page__create:hover {
  @apply border-blue-500 bg-gray-700;
}

.playlists-page__create-icon {
  @apply text-4xl text-gray-500 mb-2;
}

.playlists-page__create:hover .playlists-page__create-icon {
  @apply text-blue-500;
}

.playlists-page__create-text {
  @apply text-sm text-gray-400;
}

.playlists-page__create:hover .playlists-page__create-text {
  @apply text-white;
}
</style>
