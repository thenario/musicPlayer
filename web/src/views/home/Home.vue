<template>
  <div class="home-page">
    <p class="home-page__eyebrow">LISTEN · YOUR MUSIC SPACE</p>
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
.home-page {
  box-sizing: border-box;
  height: 100%;
  overflow: auto;
  padding: clamp(40px, 8vh, 80px) 32px;
  color: #20232d;
  text-align: center;
  background: radial-gradient(ellipse at 50% 0%, #f0edfc 0%, transparent 55%), var(--music-canvas);
}

.home-page__eyebrow {
  margin: 0 0 16px;
  color: #9a91bb;
  font-size: 11px;
  font-weight: 650;
  letter-spacing: 0.2em;
}

.home-page__title {
  margin: 0;
  font-size: clamp(26px, 3.3vw, 38px);
  font-weight: 700;
  letter-spacing: -0.03em;
}

.home-page__subtitle {
  margin: 14px 0 44px;
  color: #8d879e;
  font-size: 14px;
}

.home-page__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  max-width: 960px;
  margin: 0 auto;
}

.home-page__card {
  padding: 32px 24px;
  border: 1px solid var(--music-border);
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 4px 16px rgb(39 35 76 / 3%);
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.home-page__card:hover {
  transform: translateY(-4px);
  border-color: #d8d1f2;
  box-shadow: 0 12px 28px rgb(66 49 121 / 8%);
}

.home-page__icon {
  box-sizing: content-box;
  width: 28px;
  height: 28px;
  padding: 16px;
  border-radius: 18px;
  margin: 0 auto 18px;
}

.home-page__icon--blue {
  color: #4d78d6;
  background: #edf3fe;
}

.home-page__icon--green {
  color: #3a9a77;
  background: #eaf6f0;
}

.home-page__icon--purple {
  color: #7a68c7;
  background: #f1edfc;
}

.home-page__card-title {
  margin: 0 0 8px;
  color: #303542;
  font-size: 16px;
  font-weight: 650;
}

.home-page__card-desc {
  margin: 0;
  color: #858c9a;
  font-size: 14px;
  line-height: 1.6;
}

@media (max-width: 720px) {
  .home-page {
    padding: 40px 16px;
  }

  .home-page__subtitle {
    margin-bottom: 28px;
  }

  .home-page__grid {
    grid-template-columns: 1fr;
    gap: 14px;
    max-width: 440px;
  }

  .home-page__card {
    padding: 22px 20px;
  }
}
</style>
