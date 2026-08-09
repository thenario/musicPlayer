<template>
  <div class="page">
    <!-- 1. 顶部搜索栏 -->
    <div class="page__header">
      <h1 class="page__title">歌曲库</h1>
      <div class="page__search">
        <el-input v-model="searchKeyword" placeholder="搜索歌名、歌手或专辑..." :prefix-icon="Search" clearable
          class="custom-search-input" @input="debouncedSearch" />
      </div>
    </div>

    <div class="page__table">
      <SongTable
        :songs="songs"
        :loading="loading"
        :current-song-id="currentSong?.song_id"
        @play-now="handlePlayNow"
        @play-next="handlePlayNext"
      />
    </div>

    <div class="page__pagination">
      <AppPagination
        :current="pagination.state.current"
        :page-size="pagination.state.pageSize"
        :total="pagination.state.total"
        :page-sizes="[15, 30, 50]"
        @page-change="changePage"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SongListPage' })
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { debounce } from 'lodash-es'
import { Search } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/player'
import { AppPagination } from '@/common'
import SongTable from './components/SongTable.vue'
import { useSongList } from './composables/useSongList'
import type { ISong } from '@/types'

const playerStore = usePlayerStore()
const { currentSong } = storeToRefs(playerStore)

const { songs, searchKeyword, loading, pagination, changePage, load } = useSongList()

const handlePlayNow = async (song: ISong) => {
  await playerStore.playSong(song, "now")
}

const handlePlayNext = async (song: ISong) => {
  const res = await playerStore.playSong(song, "next")
  if (res.success) {
    ElMessage.success(`已将《${song.song_title}》添加到下一首播放`)
  }
}

const debouncedSearch = debounce(() => {
  pagination.change(1)
  load()
}, 500)

onMounted(load)
</script>

<style scoped>
@reference "../../../assets/index.css";

.page {
  @apply h-full flex flex-col bg-gray-950 text-white p-6;
}

.page__header {
  @apply flex justify-between items-center mb-6 shrink-0;
}

.page__title {
  @apply text-3xl font-black tracking-tight;
}

.page__search {
  @apply relative;
}

.page__table {
  @apply flex-1 overflow-hidden;
}

.page__pagination {
  @apply py-6 flex justify-center shrink-0;
}
</style>
