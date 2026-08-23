<template>
  <div class="page">
    <!-- 顶部搜索栏 -->
    <div class="page__header">
      <h1 class="page__title">歌曲库</h1>
      <div class="page__search">
        <el-input v-model="searchKeyword" placeholder="搜索歌名、歌手或专辑..." :prefix-icon="Search" clearable
          class="custom-search-input" @input="debouncedSearch" />
      </div>
    </div>

    <div class="page__table">
      <SongTable ref="songTableRef" :songs="songs" :loading="loading" :current-song-id="currentSong?.song_id" @play-now="handlePlayNow"
        @play-next="handlePlayNext" />
    </div>

    <div class="page__pagination">
      <AppPagination :current="pagination.state.current" :page-size="pagination.state.pageSize"
        :total="pagination.state.total" :page-sizes="[15, 30, 50]" @page-change="handlePageChange" />
    </div>

  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SongListPage' })
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { debounce } from 'lodash-es'
import { Search } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/player'
import { AppPagination } from '@/common'
import SongTable from './components/SongTable.vue'
import { useSongList } from './composables/use-song-list'
import type { ISong } from '@/types'

const playerStore = usePlayerStore()
const { currentSong } = storeToRefs(playerStore)

const { songs, searchKeyword, loading, pagination, changePage, load } = useSongList()

const songTableRef = ref<InstanceType<typeof SongTable> | null>(null)

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

const handlePageChange = async (page: number, pageSize: number) => {
  await changePage(page, pageSize)
  await nextTick()

  songTableRef.value?.scrollToTop()
}

onMounted(load)

onBeforeUnmount(() => {
  debouncedSearch.cancel()
})
</script>

<style scoped>
.page {
  box-sizing: border-box;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 24px;
  color: #20232d;
  background: #f7f8fa;
}

.page__header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
}

.page__title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.page__search {
  width: min(360px, 100%);
}

.page__table {
  flex: 1;
  min-height: 0;
  overflow: auto;
  border: 1px solid #e8eaef;
  border-radius: 12px;
  background: #ffffff;
}

.page__pagination {
  display: flex;
  flex-shrink: 0;
  justify-content: center;
  padding-top: 16px;
}

@media (max-width: 640px) {
  .page {
    padding: 16px;
  }

  .page__header {
    align-items: stretch;
    flex-direction: column;
    gap: 12px;
  }

  .page__search {
    width: 100%;
  }
}
</style>
