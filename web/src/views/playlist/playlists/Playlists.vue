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
import { usePlaylistList } from './composables/use-playlist-list'

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
.playlists-page {
  box-sizing: border-box;
  min-height: 100%;
  overflow: auto;
  padding: 24px;
  color: #20232d;
  background: #f7f8fa;
}

.playlists-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
}

.playlists-page__title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.playlists-page__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(156px, 1fr));
  gap: 20px;
  width: 100%;
  max-width: 1200px;
}

.playlists-page__create {
  display: flex;
  aspect-ratio: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px dashed #c9cdd7;
  border-radius: 12px;
  color: #7c8493;
  cursor: pointer;
  background: #fff;
  transition: border-color 160ms ease, background 160ms ease, color 160ms ease;
}

.playlists-page__create:hover {
  border-color: #8176d1;
  color: #6256c5;
  background: #faf9ff;
}

.playlists-page__create-icon {
  margin-bottom: 8px;
  font-size: 34px;
  font-weight: 300;
  line-height: 1;
}

.playlists-page__create-text {
  font-size: 14px;
  font-weight: 500;
}

@media (max-width: 640px) {
  .playlists-page {
    padding: 16px;
  }

  .playlists-page__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
  }
}
</style>
