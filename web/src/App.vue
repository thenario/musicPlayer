<template>
  <!-- h-screen：百分百的视口高度 -->
  <!-- w-full：宽度占满父元素百分之百 -->
  <!-- mx-auto：margin x即左右边距自动 -->
  <!-- bg-gray-900：背景颜色 -->
  <!-- text-white：文字颜色 -->
  <!-- flex：开启flex布局 -->
  <!-- flex-col：垂直布局 -->
  <!-- overflow-hidden：隐藏超出部分不出现滚动条 -->
  <div id="app-container" class="app-container">

    <!-- shrink-0：不被flex的布局安排挤压，挤压率为零 -->
    <NavBar class="app-navbar" />

    <!-- main：语义化标签 -->
    <!-- flex-1：自动撑满剩余空间 -->
    <!-- relative：使得父组件变为子组件的定位基准 -->
    <main class="app-main">
      <router-view />
    </main>

    <PlayerBar />

    <QueueDrawer />

    <!-- preload有三种模式，auto页面一加载就load，metadata只load元数据，none不播放就不load -->
    <audio ref="audioElement" preload="auto" />
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, onMounted } from 'vue'
import { usePlayerStore } from './stores/player'
import NavBar from '@/components/nav-bar/NavBar.vue'
import PlayerBar from '@/components/player-bar/PlayerBar.vue'
import QueueDrawer from '@/components/queue-drawer/QueueDrawer.vue'

// 把dom节点给到value里
const audioElement = ref<HTMLAudioElement | null>(null)
const playerStore = usePlayerStore()

onMounted(() => {
  // 设置audio，把隐形的音频驱动器上报给全局大仓库
  if (audioElement.value) {
    playerStore.setAudioElement(audioElement.value)
  }
})
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