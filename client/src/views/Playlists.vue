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

      <router-link v-for="item in playlists" :key="item.playlist_id" :to="`/playlists/${item.playlist_id}`"
        class="group cursor-pointer block">
        <div class="aspect-square bg-gray-800 rounded-lg overflow-hidden mb-2">
          <img :src="item.playlist_cover_url || '/default-cover.png'"
            class="w-full h-full object-cover group-hover:scale-105 transition-transform">
        </div>
        <p class="font-medium truncate text-white">{{ item.playlist_name || item.name }}</p>
      </router-link>
    </div>

    <Teleport to="body">
      <div v-if="showModal"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
        <div class="bg-gray-800 w-full max-w-md rounded-2xl p-6 shadow-2xl">
          <h2 class="text-xl text-white font-bold mb-4">创建新歌单</h2>

          <div class="space-y-4">
            <div>
              <label class="block text-sm text-gray-400 mb-2">封面</label>
              <div @click="fileInput?.click()"
                class="relative w-32 h-32 bg-gray-700 rounded-lg overflow-hidden cursor-pointer hover:ring-2 ring-blue-500">
                <img v-if="previewUrl" :src="previewUrl" class="w-full h-full object-cover">
                <div v-else class="flex items-center justify-center h-full text-gray-500 text-2xl">+</div>
                <input type="file" ref="fileInput" hidden @change="handleFileChange" accept="image/*">
              </div>
            </div>

            <div>
              <label class="block text-sm text-gray-400 mb-1">名称</label>
              <input v-model="form.name" type="text"
                class="w-full bg-gray-900 border text-white border-gray-700 rounded-lg px-4 py-2 focus:border-blue-500 outline-none">
            </div>

            <div>
              <label class="block text-sm text-gray-400 mb-1">描述</label>
              <textarea v-model="form.description" rows="3"
                class="w-full bg-gray-900 border text-white border-gray-700 rounded-lg px-4 py-2 focus:border-blue-500 outline-none resize-none"></textarea>
            </div>
          </div>

          <div class="flex justify-end space-x-3 mt-8">
            <button @click="closeModal" class="px-6 py-2 text-gray-400 hover:text-white">取消</button>
            <button @click="submitForm" :disabled="isSubmitting || !form.name"
              class="px-6 py-2 bg-blue-600 rounded-lg hover:bg-blue-500 disabled:opacity-50">
              {{ isSubmitting ? '上传中...' : '立即创建' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { playlistApi } from '../../axios/playlistApi'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const playlists = ref<any[]>([])
const showModal = ref(false)
const isSubmitting = ref(false)
const previewUrl = ref('')
const selectedFile = ref<File | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

const form = reactive({ name: '', description: '' })

const handleFileChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) {
    selectedFile.value = file
    if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = URL.createObjectURL(file)
  }
}

const closeModal = () => {
  showModal.value = false
  form.name = ''; form.description = ''
  selectedFile.value = null
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
}

const submitForm = async () => {
  if (!form.name || isSubmitting.value) return
  isSubmitting.value = true

  try {
    const formData = new FormData()
    formData.append('name', form.name)
    formData.append('description', form.description)
    formData.append('creator_id', String(userStore.user?.user_id))
    if (selectedFile.value) formData.append('cover_image', selectedFile.value)

    const res = await playlistApi.createPlaylist(formData)
    if (res.success) {
      ElMessage.success('创建成功')
      closeModal()
      loadPlaylists()
    }
    else {
      ElMessage.error("创建失败")
    }
  } catch (error) {
    console.log(error)
    ElMessage.error("创建失败")
  } finally {
    isSubmitting.value = false
  }
}

const loadPlaylists = async () => {
  if (userStore.user) {
    const res = await playlistApi.getMyPlaylists()
    if (res.success) playlists.value = res.playlists || []
  }
}

onMounted(loadPlaylists)
onUnmounted(() => {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
})
</script>