<template>
  <div class="p-6 bg-gray-900 min-h-screen text-white">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">我的歌单</h1>
    </div>

    <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-6">
      <div v-if="userStore.isAuthenticated" @click="showModal = true"
        class="aspect-square bg-gray-800 border-2 border-dashed border-gray-700 rounded-xl flex flex-col items-center justify-center cursor-pointer hover:border-blue-500 hover:bg-gray-700 transition-all group">
        <span class="text-4xl text-gray-500 group-hover:text-blue-500 mb-2">+</span>
        <span class="text-sm text-gray-400 group-hover:text-white">新建歌单</span>
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
  await create({ ...payload, creatorId: userStore.user?.user_id })
  showModal.value = false
}

onMounted(load)
</script>
