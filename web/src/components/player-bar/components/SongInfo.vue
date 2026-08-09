<template>
  <div class="w-[30%] flex items-center gap-4 min-w-0">
    <div class="relative group cursor-pointer shrink-0" @click="emit('expand')">
      <div
        class="w-16 h-16 rounded-lg shadow-2xl overflow-hidden transition-transform group-hover:scale-105 bg-gray-800">
        <img v-if="cover" :src="cover" alt="封面" class="w-full h-full object-cover" />
        <div v-else class="w-full h-full flex items-center justify-center">
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3" />
          </svg>
        </div>
      </div>
      <div
        class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center rounded-lg text-white">
        <el-icon :size="20">
          <ArrowUpBold />
        </el-icon>
      </div>
    </div>

    <div class="min-w-0 flex-1">
      <h4 class="text-white text-base font-semibold truncate">{{ title }}</h4>
      <p class="text-gray-400 text-xs truncate mt-1">{{ artist }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'SongInfo' })
import { ArrowUpBold } from '@element-plus/icons-vue'

defineProps<{
  cover: string
  title: string
  artist: string
}>()

const emit = defineEmits<{
  (e: 'expand'): void
}>()
</script>
