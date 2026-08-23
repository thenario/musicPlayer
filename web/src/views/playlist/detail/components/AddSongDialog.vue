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
import { songApi } from '@/api/song-api'

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
.add-song-dialog__list {
  max-height: 400px;
  margin-top: 16px;
  overflow-y: auto;
}

.add-song-dialog__song {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 8px;
  transition: background-color .2s ease;
}

.add-song-dialog__song:hover {
  background: #f5f7fa;
}

.add-song-dialog__song-info {
  min-width: 0;
  padding-right: 16px;
}

.add-song-dialog__song-title {
  margin: 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.add-song-dialog__song-artist {
  margin: 4px 0 0;
  color: #909399;
  font-size: 12px;
}

.add-song-dialog__empty {
  padding: 16px 0;
  color: #909399;
  text-align: center;
}
</style>
