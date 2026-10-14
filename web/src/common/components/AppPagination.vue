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
.app-pagination {
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  --el-pagination-text-color: #827b95;
  --el-pagination-button-color: #827b95;
  --el-pagination-button-bg-color: #fff;
  --el-pagination-hover-color: var(--music-primary);
}

:deep(.btn-prev),
:deep(.btn-next),
:deep(.el-pager li) {
  border: 1px solid #e7e9ef;
  border-radius: 8px;
  background: #fff;
}

:deep(.el-pager li.is-active) {
  border-color: var(--music-primary);
  background: var(--music-primary);
  color: #fff;
}

:deep(.el-pager li:not(.is-active):hover),
:deep(.btn-prev:not(:disabled):hover),
:deep(.btn-next:not(:disabled):hover) {
  border-color: #8d84dc;
  color: var(--music-primary);
}

@media (max-width: 600px) {
  .app-pagination {
    gap: 4px;
    --el-pagination-button-width: 28px;
  }

  :deep(.el-pagination__total),
  :deep(.el-pagination__sizes) {
    margin: 0;
  }

  :deep(.el-pagination__sizes .el-select) {
    width: 102px;
  }

  :deep(.el-pager li) {
    min-width: 28px;
  }
}
</style>
