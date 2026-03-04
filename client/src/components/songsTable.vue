<template>
    <div class="bg-gray-800 rounded-lg flex-1 overflow-hidden p-4">
        <el-table :data="songs" style="width: 100%" row-class-name="group transition-colors"
            header-row-class-name="bg-gray-700" class="custom-el-table">
            <el-table-column prop="title" label="歌曲" />

            <el-table-column prop="artist" label="艺术家" />

            <el-table-column label="时长">
                <template #default="scope">
                    <span class="text-gray-400">
                        {{ formatDuration(scope.row.duration) }}
                    </span>
                </template>
            </el-table-column>

            <el-table-column label="操作">
                <template #default="scope">
                    <slot name="actions" :song="scope.row"></slot>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script setup lang="ts">
import { ISong } from "../../type"
const props = defineProps<{
    songs: ISong[] | null
}>()

defineSlots<{
    actions(props: { song: ISong }): any
}>()

const formatDuration = (seconds: number) => {
    if (!seconds) return '0:00'
    const mins = Math.floor(seconds / 60)
    const secs = Math.floor(seconds % 60)
    return `${mins}:${secs.toString().padStart(2, '0')}`
}
</script>

<style scoped>
:deep(.el-table) {
    --el-table-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: #374151;
    --el-table-row-hover-bg-color: #374151;
    color: white;
    border: none;
}

:deep(.el-table__inner-wrapper::before) {
    display: none;
}
</style>