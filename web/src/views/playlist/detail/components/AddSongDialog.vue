<template>
  <el-dialog :model-value="open" title="向歌单添加歌曲" width="500px" destroy-on-close
    @update:model-value="emit('update:open', $event)">
    <el-input v-model="songSearchQuery" placeholder="搜索歌名或歌手..." :prefix-icon="Search" clearable
      @input="debouncedSearch" />
    <div class="add-song-dialog__list custom-scrollbar">
      <div v-for="song in searchResults" :key="song.song_id"
        class="add-song-dialog__song">
        <div class="add-song-dialog__song-info">
          <div class="add-song-dialog__song-title">{{ song.song_title }}</div>
          <div class="add-song-dialog__song-artist">{{ song.artist }}</div>
        </div>
        <el-button type="primary" size="small" plain @click="emit('add', song.song_id)">添加</el-button>
      </div>
      <div v-if="searchResults.length === 0 && songSearchQuery" class="add-song-dialog__empty">
        未找到相关歌曲
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { debounce } from 'lodash-es'
import { Search } from '@element-plus/icons-vue'
import type { ISong } from '@/types'
import { songApi } from '@/api/songApi'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'add', songId: number | string): void
}>()

const songSearchQuery = ref('')
const searchResults = ref<ISong[]>([])

const searchSongs = async () => {
  if (!songSearchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    const res = await songApi.getSongs(1, songSearchQuery.value)
    searchResults.value = res.songs || []
  } catch (err) {
    // 错误已由拦截器统一提示，这里只记录日志
    console.error(err)
  }
}

const debouncedSearch = debounce(searchSongs, 500)

// 每次打开弹窗时清空上一次的搜索结果
watch(() => props.open, (val) => {
  if (val) {
    songSearchQuery.value = ''
    searchResults.value = []
  }
})
</script>

<style scoped>
@reference "../../../../assets/index.css";

.add-song-dialog__list {
  @apply mt-4 max-h-[400px] overflow-y-auto;
}

.add-song-dialog__song {
  @apply flex justify-between items-center p-3 rounded-lg transition-colors;
}

.add-song-dialog__song:hover {
  @apply bg-white/5;
}

.add-song-dialog__song-info {
  @apply min-w-0 pr-4;
}

.add-song-dialog__song-title {
  @apply text-sm font-medium;
}

.add-song-dialog__song-artist {
  @apply text-xs text-gray-500;
}

.add-song-dialog__empty {
  @apply text-center py-4 text-gray-500;
}
</style>
