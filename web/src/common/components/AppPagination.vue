<template>
  <el-pagination class="app-pagination" :current-page="current" :page-size="pageSize" :total="total" :page-sizes="pageSizes"
    :disabled="disabled" layout="total, sizes, prev, pager, next" @update:current-page="handleCurrentChange"
    @update:page-size="handleSizeChange" />
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    current: number
    pageSize: number
    total: number
    disabled?: boolean
    pageSizes?: number[]
  }>(),
  { disabled: false, pageSizes: () => [10, 20, 50] },
)

const emit = defineEmits<{
  (e: 'page-change', page: number, pageSize: number): void
}>()

const handleCurrentChange = (page: number) => {
  emit('page-change', page, props.pageSize)
}

const handleSizeChange = (size: number) => {
  emit('page-change', 1, size)
}
</script>
<style scoped>
:deep(.app-pagination) {
  --el-pagination-bg-color: #ec0f0f;
  --el-pagination-text-color: #606266;
  --el-pagination-button-color: #606266;
  --el-pagination-button-bg-color: #ffffff;
  --el-pagination-hover-color: #d90843;
}

:deep(.app-pagination .btn-prev),
:deep(.app-pagination .btn-next),
:deep(.app-pagination .el-pager li) {
  border: 1px solid #e7e9ef;
  border-radius: 8px;
  background: #fff;
}

:deep(.app-pagination .el-pager li.is-active) {
  border-color: #6256c5;
  background: #6256c5;
  color: #fff;
}

:deep(.app-pagination .el-pager li:hover),
:deep(.app-pagination .btn-prev:hover),
:deep(.app-pagination .btn-next:hover) {
  border-color: #8d84dc;
  color: #6256c5;
}
</style>
