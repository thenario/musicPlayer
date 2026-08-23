<template>
    <!-- 1. 父容器设为 h-full flex flex-col，确保高度填满父级 -->
    <div class="page">

        <!-- 2. 顶部导航：设为 shrink-0，防止被压缩 -->
        <div class="page__header">
            <div class="page__header-left">
                <el-button @click="router.push('/user-profile')" circle>
                    <el-icon>
                        <Back />
                    </el-icon>
                </el-button>
                <h1 class="page__title">我的上传</h1>
            </div>
            <el-button type="primary" class="upload-btn" @click="router.push('/upload')">
                <el-icon class="page__upload-icon">
                    <Upload />
                </el-icon>
                上传新歌曲
            </el-button>
        </div>

        <div class="page__table">
            <el-table v-loading="loading" :data="songs" height="100%" class="uploads-table"
                element-loading-background="rgba(255, 255, 255, 0.72)" ref="uploadsTabelRef">
                <el-table-column label="歌曲" min-width="36%" show-overflow-tooltip>
                    <template #default="{ row }">
                        <div class="uploads-table__song">
                            <el-image v-if="row.song_cover_url" :src="getImageUrl(row.song_cover_url)"
                                class="uploads-table__cover" fit="cover" />
                            <div v-else class="uploads-table__cover uploads-table__cover--fallback">
                                <el-icon>
                                    <Mic />
                                </el-icon>
                            </div>
                            <span class="uploads-table__title">{{ row.song_title }}</span>
                        </div>
                    </template>
                </el-table-column>

                <el-table-column prop="artist" label="歌手" min-width="22%" show-overflow-tooltip />

                <el-table-column label="上传时间" min-width="20%">
                    <template #default="{ row }">
                        <span class="uploads-table__date">{{ formatDate(row.date_added) }}</span>
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="168" align="right">
                    <template #default="{ row }">
                        <el-tooltip content="编辑歌曲" placement="top">
                            <el-button link type="primary" class="uploads-table__edit"
                                @click="goToSongEdit(row as EditableUploadSong)">
                                <el-icon>
                                    <EditPen />
                                </el-icon>
                            </el-button>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <template #empty>
                    <el-empty v-if="!loading" description="你还没有上传过任何歌曲" :image-size="150">
                        <el-button type="primary" plain @click="router.push('/upload')">立即去上传</el-button>
                    </el-empty>
                </template>
            </el-table>
        </div>

        <div class="page__pagination">
            <AppPagination :current="pagination.state.current" :page-size="pagination.state.pageSize"
                :total="pagination.state.total" :page-sizes="[10, 20, 50]" @page-change="handleChangePage" />
        </div>

    </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'MyUploadsPage' })
import { useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import { useSongStore, type EditableUploadSong } from '@/stores/song';
import { AppPagination } from '@/common';
import { useMyUploads } from './composables/use-my-uploads';
import { Back, EditPen, Mic, Upload } from '@element-plus/icons-vue';
import { formatDate, getImageUrl } from '@/utils/format';

const router = useRouter();
const songStore = useSongStore();

const { songs, loading, pagination, changePage, load } = useMyUploads();

const uploadsTabelRef = ref<HTMLElement | null>(null)

const goToSongEdit = (song: EditableUploadSong) => {
    songStore.setEditingSong(song);
    router.push(`/user-uploads/${song.song_id}/edit`);
};

const handleChangePage = async (page: number, pageSize: number) => {
    await changePage(page, pageSize)
    await load()

    uploadsTabelRef.value?.scrollTo({
        top: 0,
        behavior: "smooth"
    })
}

onMounted(load);
</script>

<style scoped>
.page {
    box-sizing: border-box;
    display: grid;
    grid-template-rows: auto minmax(0, 1fr) auto;
    height: 100%;
    min-height: 0;
    overflow: hidden;
    padding: 24px;
    color: #20232d;
    background: #f7f8fa;
}

.page__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 20px;
}

.page__header-left {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 12px;
}

.page__title {
    margin: 0;
    overflow: hidden;
    font-size: 24px;
    font-weight: 700;
    letter-spacing: -0.02em;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.page__upload-icon {
    margin-right: 4px;
}

.page__table {
    min-height: 0;
    overflow: hidden;
    border: 1px solid #e8eaef;
    border-radius: 12px;
    background: #fff;
}

.uploads-table {
    --el-table-border-color: #eef0f4;
    --el-table-header-bg-color: #fafbfc;
    --el-table-header-text-color: #7b8190;
    --el-table-row-hover-bg-color: #f6f5ff;
    --el-table-text-color: #4f5563;
}

:deep(.uploads-table .el-table__header-wrapper th.el-table__cell) {
    height: 46px;
    font-size: 12px;
    font-weight: 600;
}

:deep(.uploads-table .el-table__body-wrapper td.el-table__cell) {
    height: 64px;
    border-bottom-color: #f0f1f4;
}

.uploads-table__song {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 12px;
}

.uploads-table__cover {
    width: 40px;
    height: 40px;
    flex: 0 0 auto;
    overflow: hidden;
    border: 1px solid #eceef3;
    border-radius: 8px;
}

.uploads-table__cover--fallback {
    display: grid;
    place-items: center;
    color: #969cab;
    background: #f2f3f6;
}

.uploads-table__title {
    overflow: hidden;
    color: #303542;
    font-size: 14px;
    font-weight: 600;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.uploads-table__date {
    color: #9097a7;
    font-size: 13px;
}

.uploads-table__edit {
    font-weight: 500;
}

.page__pagination {
    display: flex;
    min-height: 48px;
    align-items: center;
    justify-content: center;
    padding: 16px 0;
}

.upload-btn {
    border: 0;
    border-radius: 8px;
    height: 40px;
    background: #6256c5;
    font-weight: 600;
}

.upload-btn:hover {
    background: #5549b7;
}

@media (max-width: 640px) {
    .page {
        padding: 16px;
    }

    .page__header {
        align-items: flex-start;
        flex-direction: column;
        gap: 12px;
    }

    .page__header-left,
    .upload-btn {
        width: 100%;
    }

    .page__table {
        border-radius: 10px;
    }
}
</style>
