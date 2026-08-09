<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden" :class="bgClass">
    <div class="absolute inset-0" :class="gradientClass"></div>
    <div class="absolute inset-0 opacity-10"
      style="background-image: linear-gradient(to right, #ffffff0a 1px, transparent 1px), linear-gradient(to bottom, #ffffff0a 1px, transparent 1px); background-size: 50px 50px;">
    </div>
    <div
      class="absolute top-20 left-10 w-64 h-64 bg-linear-to-r from-cyan-500/20 to-blue-500/20 rounded-full blur-3xl animate-pulse-slow">
    </div>
    <div
      class="absolute bottom-20 right-10 w-80 h-80 bg-linear-to-r from-purple-500/20 to-pink-500/20 rounded-full blur-3xl animate-pulse-slow">
    </div>

    <div class="w-full max-w-md z-10 animate-fade-in">
      <div v-if="$slots.header" class="text-center mb-8">
        <slot name="header" />
      </div>
      <div class="bg-gray-900/70 backdrop-blur-xl rounded-2xl shadow-2xl border border-gray-800/50 p-8">
        <slot />
      </div>
      <div v-if="$slots.footer" class="mt-8 text-center">
        <slot name="footer" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    bgClass?: string
    gradientClass?: string
  }>(),
  {
    bgClass: 'bg-gray-950 text-white',
    gradientClass: 'bg-linear-to-br from-gray-900 via-purple-900 to-blue-900',
  },
)
</script>

<style scoped>
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in {
  animation: fade-in 0.5s ease-out;
}
</style>
