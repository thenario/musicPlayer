<template>
  <Teleport to="body">
    <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
      <div class="bg-gray-800 w-full max-w-md rounded-2xl p-6 shadow-2xl">
        <h2 class="text-xl text-white font-bold mb-4">创建新歌单</h2>

        <div class="space-y-4">
          <div>
            <label class="block text-sm text-gray-400 mb-2">封面</label>
            <div @click="fileInput?.click()"
              class="relative w-32 h-32 bg-gray-700 rounded-lg overflow-hidden cursor-pointer hover:ring-2 ring-blue-500">
              <img v-if="previewUrl" :src="previewUrl" alt="歌单封面" class="w-full h-full object-cover">
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
          <button @click="emit('update:open', false)" class="px-6 py-2 text-gray-400 hover:text-white">取消</button>
          <button @click="handleSubmit" :disabled="loading || !form.name"
            class="px-6 py-2 bg-blue-600 rounded-lg hover:bg-blue-500 disabled:opacity-50">
            {{ loading ? '上传中...' : '立即创建' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'

const props = defineProps<{ open: boolean; loading: boolean }>()
const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'create', payload: { name: string; description: string; coverFile: File | null }): void
}>()

const form = reactive({ name: '', description: '' })
const previewUrl = ref('')
const selectedFile = ref<File | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

// 每次打开时重置表单
watch(() => props.open, (val) => {
  if (!val) return
  form.name = ''
  form.description = ''
  selectedFile.value = null
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
})

const handleFileChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  selectedFile.value = file
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = URL.createObjectURL(file)
}

const handleSubmit = () => {
  if (!form.name || props.loading) return
  emit('create', {
    name: form.name,
    description: form.description,
    coverFile: selectedFile.value,
  })
}
</script>
