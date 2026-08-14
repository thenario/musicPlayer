<template>
  <el-pagination
    :current-page="current"
    :page-size="pageSize"
    :total="total"
    :page-sizes="pageSizes"
    :disabled="disabled"
    layout="total, sizes, prev, pager, next"
    background
    @current-change="handleCurrentChange"
    @size-change="handleSizeChange"
  />
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
