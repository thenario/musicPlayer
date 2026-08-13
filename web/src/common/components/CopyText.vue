<template>
  <span class="copy-text" @click="handleCopy">
    <slot />
    <el-icon class="copy-text__icon">
      <Check v-if="copied" />
      <CopyDocument v-else />
    </el-icon>
  </span>
</template>

<script setup lang="ts">
import { Check, CopyDocument } from '@element-plus/icons-vue'
import { useClipboard } from '../composables/use-clipboard'

const props = defineProps<{ value: string }>()
const { copied, copy } = useClipboard()

const handleCopy = async () => {
  const success = await copy(props.value)
  if (success) ElMessage.success('已复制')
  else ElMessage.error('复制失败')
}
</script>

<style scoped>
.copy-text {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}
.copy-text__icon {
  font-size: 14px;
}
</style>
