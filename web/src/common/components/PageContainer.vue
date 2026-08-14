<template>
  <div class="page-container">
    <div class="page-container__header">
      <div>
        <h1 class="page-container__title">{{ title }}</h1>
        <p v-if="description" class="page-container__description">{{ description }}</p>
      </div>
      <div v-if="$slots.actions" class="page-container__actions">
        <slot name="actions" />
      </div>
    </div>
    <div class="page-container__content">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    description?: string
  }>(),
  { description: '' },
)
</script>

<style scoped>
.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}
.page-container__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}
.page-container__title {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.3;
}
.page-container__description {
  margin-top: 4px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}
.page-container__actions {
  flex-shrink: 0;
}
</style>
