<template>
  <div class="home-page">
    <h1 class="home-page__title">欢迎使用音乐播放器</h1>
    <p class="home-page__subtitle">发现和播放您喜爱的音乐</p>

    <div class="home-page__grid">
      <div class="home-page__card">
        <svg class="home-page__icon home-page__icon--blue" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z"
            clip-rule="evenodd" />
        </svg>
        <h3 class="home-page__card-title">海量歌曲</h3>
        <p class="home-page__card-desc">探索数千首高质量音乐</p>
      </div>

      <div class="home-page__card">
        <svg class="home-page__icon home-page__icon--green" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd"
            d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
            clip-rule="evenodd" />
        </svg>
        <h3 class="home-page__card-title">创建歌单</h3>
        <p class="home-page__card-desc">制作属于您自己的音乐收藏</p>
      </div>

      <div class="home-page__card">
        <svg class="home-page__icon home-page__icon--purple" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2.586L7.707 7.293a1 1 0 00-1.414 1.414L8.586 11H6a1 1 0 100 2h2.586l-2.293 2.293a1 1 0 101.414 1.414L10 12.414V15a1 1 0 102 0v-2.586l2.293 2.293a1 1 0 001.414-1.414L11.414 11H14a1 1 0 100-2h-2.586l2.293-2.293a1 1 0 00-1.414-1.414L10 8.586V6z"
            clip-rule="evenodd" />
        </svg>
        <h3 class="home-page__card-title">智能队列</h3>
        <p class="home-page__card-desc">多种播放模式，随心所欲</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'HomePage' })
</script>

<style scoped>
@reference "../../assets/index.css";

.home-page {
  @apply text-center;
}

.home-page__title {
  @apply text-4xl font-bold mb-8;
}

.home-page__subtitle {
  @apply text-xl text-gray-300 mb-12;
}

.home-page__grid {
  @apply grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl mx-auto;
}

.home-page__card {
  @apply bg-gray-800 p-6 rounded-lg;
}

.home-page__icon {
  @apply w-12 h-12 mx-auto mb-4;
}

.home-page__icon--blue {
  @apply text-blue-400;
}

.home-page__icon--green {
  @apply text-green-400;
}

.home-page__icon--purple {
  @apply text-purple-400;
}

.home-page__card-title {
  @apply text-xl font-semibold mb-2;
}

.home-page__card-desc {
  @apply text-gray-400;
}
</style>