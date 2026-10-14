<template>
  <div class="page">
    <!-- 顶部搜索栏 -->
    <div class="page__header">
      <div class="page__heading">
        <h1 class="page__title">歌曲库</h1>
        <p class="page__subtitle">找到喜欢的旋律，双击歌曲即可播放</p>
      </div>
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
import { reportError } from '@/utils/feedback'

const playerStore = usePlayerStore()
const { currentSong } = storeToRefs(playerStore)

const { songs, searchKeyword, loading, pagination, changePage, load } = useSongList()

const songTableRef = ref<InstanceType<typeof SongTable> | null>(null)

const handlePlayNow = async (song: ISong) => {
  const result = await playerStore.playSong(song, 'now')
  if (!result.success) reportError(result)
}

const handlePlayNext = async (song: ISong) => {
  const res = await playerStore.playSong(song, "next")
  if (res.success) {
    ElMessage.success(`已将《${song.song_title}》添加到下一首播放`)
  } else {
    reportError(res)
  }
}

const debouncedSearch = debounce(() => {
  pagination.change(1)
  void load().then((result) => { if (!result.success) reportError(result) })
}, 500)

const handlePageChange = async (page: number, pageSize: number) => {
  const result = await changePage(page, pageSize)
  if (!result.success) return reportError(result)
  await nextTick()

  songTableRef.value?.scrollToTop()
}

onMounted(async () => {
  const result = await load()
  if (!result.success) reportError(result)
})

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
  padding: clamp(20px, 2.8vw, 36px);
  color: #20232d;
  background: var(--music-canvas);
}

.page__header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.page__subtitle {
  margin: 7px 0 0;
  color: var(--music-muted);
  font-size: 12px;
  line-height: 1.6;
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

.custom-search-input :deep(.el-input__wrapper) {
  min-height: 42px;
  padding-inline: 14px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 0 0 1px var(--music-border) inset, 0 3px 12px rgb(39 35 76 / 3%);
  transition: box-shadow 160ms ease;
}

.custom-search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--music-primary) inset, 0 0 0 4px rgb(109 91 208 / 8%);
}

.page__table {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  border: 1px solid var(--music-border);
  border-radius: 16px;
  background: #ffffff;
  box-shadow: var(--music-shadow);
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
