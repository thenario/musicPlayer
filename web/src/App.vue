<template>
  <div id="app-container" class="app-container">
    <NavBar class="app-navbar" />
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import NavBar from '@/components/nav-bar/NavBar.vue'
</script>

<style>
@reference "./assets/index.css";

/* 声明全局字体 */
body {
  font-family: 'Inter', sans-serif;
}

.app-container {
  @apply h-screen bg-gray-900 text-white w-full mx-auto flex flex-col overflow-hidden;
}

.app-navbar {
  @apply shrink-0;
}

.app-main {
  @apply flex-1 overflow-hidden relative flex flex-col;
}
</style>
