<template>
  <Teleport to="body">
    <div v-if="open" class="create-playlist-modal">
      <div class="create-playlist-modal__dialog">
        <h2 class="create-playlist-modal__title">创建新歌单</h2>

        <div class="create-playlist-modal__body">
          <div>
            <label class="create-playlist-modal__label">封面</label>
            <div @click="fileInput?.click()"
              class="create-playlist-modal__cover">
              <img v-if="previewUrl" :src="previewUrl" alt="歌单封面" class="create-playlist-modal__cover-img">
              <div v-else class="create-playlist-modal__cover-placeholder">+</div>
              <input type="file" ref="fileInput" hidden @change="handleFileChange" accept="image/*">
            </div>
          </div>

          <div>
            <label class="create-playlist-modal__label--compact">名称</label>
            <input v-model="form.name" type="text"
              class="create-playlist-modal__input">
          </div>

          <div>
            <label class="create-playlist-modal__label--compact">描述</label>
            <textarea v-model="form.description" rows="3"
              class="create-playlist-modal__textarea"></textarea>
          </div>
        </div>

        <div class="create-playlist-modal__footer">
          <button @click="emit('update:open', false)" class="create-playlist-modal__cancel">取消</button>
          <button @click="handleSubmit" :disabled="loading || !form.name"
            class="create-playlist-modal__submit">
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

<style scoped>
@reference "../../../../assets/index.css";

.create-playlist-modal {
  @apply fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm;
}

.create-playlist-modal__dialog {
  @apply bg-gray-800 w-full max-w-md rounded-2xl p-6 shadow-2xl;
}

.create-playlist-modal__title {
  @apply text-xl text-white font-bold mb-4;
}

.create-playlist-modal__body {
  @apply space-y-4;
}

.create-playlist-modal__label {
  @apply block text-sm text-gray-400 mb-2;
}

.create-playlist-modal__label--compact {
  @apply block text-sm text-gray-400 mb-1;
}

.create-playlist-modal__cover {
  @apply relative w-32 h-32 bg-gray-700 rounded-lg overflow-hidden cursor-pointer;
}

.create-playlist-modal__cover:hover {
  @apply ring-2 ring-blue-500;
}

.create-playlist-modal__cover-img {
  @apply w-full h-full object-cover;
}

.create-playlist-modal__cover-placeholder {
  @apply flex items-center justify-center h-full text-gray-500 text-2xl;
}

.create-playlist-modal__input {
  @apply w-full bg-gray-900 border text-white border-gray-700 rounded-lg px-4 py-2 outline-none;
}

.create-playlist-modal__input:focus {
  @apply border-blue-500;
}

.create-playlist-modal__textarea {
  @apply w-full bg-gray-900 border text-white border-gray-700 rounded-lg px-4 py-2 outline-none resize-none;
}

.create-playlist-modal__textarea:focus {
  @apply border-blue-500;
}

.create-playlist-modal__footer {
  @apply flex justify-end space-x-3 mt-8;
}

.create-playlist-modal__cancel {
  @apply px-6 py-2 text-gray-400;
}

.create-playlist-modal__cancel:hover {
  @apply text-white;
}

.create-playlist-modal__submit {
  @apply px-6 py-2 bg-blue-600 rounded-lg;
}

.create-playlist-modal__submit:hover {
  @apply bg-blue-500;
}

.create-playlist-modal__submit:disabled {
  @apply opacity-50;
}
</style>
