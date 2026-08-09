<template>
  <el-dialog :model-value="open" title="向歌单添加歌曲" width="500px" destroy-on-close
    @update:model-value="emit('update:open', $event)">
    <el-input v-model="songSearchQuery" placeholder="搜索歌名或歌手..." :prefix-icon="Search" clearable
      @input="debouncedSearch" />
    <div class="mt-4 max-h-[400px] overflow-y-auto custom-scrollbar">
      <div v-for="song in searchResults" :key="song.song_id"
        class="flex justify-between items-center p-3 hover:bg-white/5 rounded-lg transition-colors group">
        <div class="min-w-0 pr-4">
          <div class="text-sm font-medium">{{ song.song_title }}</div>
          <div class="text-xs text-gray-500">{{ song.artist }}</div>
        </div>
        <el-button type="primary" size="small" plain @click="emit('add', song.song_id)">添加</el-button>
      </div>
      <div v-if="searchResults.length === 0 && songSearchQuery" class="text-center py-4 text-gray-500">
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
  (e: 'add', songId: number): void
}>()

const songSearchQuery = ref('')
const searchResults = ref<ISong[]>([])

const searchSongs = async () => {
  if (!songSearchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  const res = await songApi.getSongs(1, songSearchQuery.value)
  searchResults.value = res.songs || []
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
