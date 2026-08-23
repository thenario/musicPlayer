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
.create-playlist-modal {
  position: fixed;
  z-index: 50;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(31 35 45 / 38%);
  backdrop-filter: blur(3px);
}

.create-playlist-modal__dialog {
  width: 100%;
  max-width: 480px;
  padding: 28px;
  border: 1px solid #e4e6ed;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 16px 40px rgb(31 35 45 / 18%);
}

.create-playlist-modal__title {
  margin: 0 0 22px;
  color: #2d3340;
  font-size: 20px;
  font-weight: 700;
}

.create-playlist-modal__body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.create-playlist-modal__label,
.create-playlist-modal__label--compact {
  display: block;
  margin-bottom: 7px;
  color: #636b79;
  font-size: 13px;
  font-weight: 600;
}

.create-playlist-modal__cover {
  position: relative;
  width: 112px;
  height: 112px;
  overflow: hidden;
  border: 1px dashed #c9cdd7;
  border-radius: 10px;
  cursor: pointer;
  background: #f7f8fa;
  transition: border-color 160ms ease, background 160ms ease;
}

.create-playlist-modal__cover:hover {
  border-color: #8176d1;
  background: #faf9ff;
}

.create-playlist-modal__cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.create-playlist-modal__cover-placeholder {
  display: flex;
  height: 100%;
  align-items: center;
  justify-content: center;
  color: #9299a7;
  font-size: 26px;
  font-weight: 300;
}

.create-playlist-modal__input,
.create-playlist-modal__textarea {
  box-sizing: border-box;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dfe2e9;
  border-radius: 8px;
  outline: none;
  color: #343a47;
  font: inherit;
  background: #fff;
  transition: border-color 160ms ease, box-shadow 160ms ease;
}

.create-playlist-modal__textarea {
  resize: vertical;
}

.create-playlist-modal__input:focus,
.create-playlist-modal__textarea:focus {
  border-color: #8176d1;
  box-shadow: 0 0 0 3px rgb(98 86 197 / 12%);
}

.create-playlist-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 26px;
}

.create-playlist-modal__cancel {
  padding: 9px 16px;
  border: 1px solid transparent;
  border-radius: 8px;
  color: #707887;
  font: inherit;
  cursor: pointer;
  background: transparent;
}

.create-playlist-modal__cancel:hover {
  color: #343a47;
  background: #f3f4f7;
}

.create-playlist-modal__submit {
  padding: 9px 16px;
  border: 0;
  border-radius: 8px;
  color: #fff;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  background: #6256c5;
  transition: background 160ms ease;
}

.create-playlist-modal__submit:hover {
  background: #5549b7;
}

.create-playlist-modal__submit:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

@media (max-width: 640px) {
  .create-playlist-modal {
    padding: 16px;
  }

  .create-playlist-modal__dialog {
    padding: 22px;
  }
}
</style>
