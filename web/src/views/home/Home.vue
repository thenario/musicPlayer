<template>
  <div class="home-page">
    <header class="home-page__masthead">
      <span>聆听 / LISTENING ROOM</span>
      <span class="home-page__edition">为每一次认真聆听</span>
    </header>

    <section class="home-page__hero">
      <div class="home-page__intro">
        <p class="home-page__eyebrow"><span></span> THE LATE NIGHT COLLECTION</p>
        <h1 class="home-page__title">把世界调小，<br /><em>把音乐打开。</em></h1>
        <p class="home-page__subtitle">熟悉的旋律，新的心情。<br />这里是只属于你的唱片室。</p>
        <div class="home-page__actions">
          <router-link to="/songs" class="home-page__listen">探索歌曲库 <span aria-hidden="true">↗</span></router-link>
          <router-link to="/playlists" class="home-page__collection">我的收藏 <span aria-hidden="true">→</span></router-link>
        </div>
      </div>
      <div class="home-page__art" aria-hidden="true">
        <div class="home-page__sleeve">
          <span class="home-page__sleeve-top">LISTEN / VOL. 01</span>
          <div class="home-page__sleeve-orbit"></div>
          <span class="home-page__sleeve-title">AFTER<br />HOURS.</span>
          <span class="home-page__sleeve-bottom">SOUNDTRACK TO YOUR EVERYDAY</span>
        </div>
        <div class="home-page__record"><div class="home-page__record-label">聆<br /><small>SIDE A · 33⅓ RPM</small></div></div>
        <span class="home-page__art-caption">GOOD MUSIC. NO RUSH.</span>
      </div>
    </section>

    <div class="home-page__section-heading"><span>你的音乐，随心安排</span><span>MAKE IT YOURS</span></div>

    <div class="home-page__grid">
      <div class="home-page__card">
        <svg class="home-page__icon home-page__icon--blue" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z"
            clip-rule="evenodd" />
        </svg>
        <h3 class="home-page__card-title">发现好声音</h3>
        <p class="home-page__card-desc">从一首喜欢的歌，开启今天的聆听。</p>
      </div>

      <div class="home-page__card">
        <svg class="home-page__icon home-page__icon--green" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd"
            d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
            clip-rule="evenodd" />
        </svg>
        <h3 class="home-page__card-title">收藏你的品味</h3>
        <p class="home-page__card-desc">把不同的心情，放进不同的歌单。</p>
      </div>

      <div class="home-page__card">
        <svg class="home-page__icon home-page__icon--purple" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2.586L7.707 7.293a1 1 0 00-1.414 1.414L8.586 11H6a1 1 0 100 2h2.586l-2.293 2.293a1 1 0 101.414 1.414L10 12.414V15a1 1 0 102 0v-2.586l2.293 2.293a1 1 0 001.414-1.414L11.414 11H14a1 1 0 100-2h-2.586l2.293-2.293a1 1 0 00-1.414-1.414L10 8.586V6z"
            clip-rule="evenodd" />
        </svg>
        <h3 class="home-page__card-title">让旋律继续</h3>
        <p class="home-page__card-desc">安排下一首，让喜欢的音乐自然流动。</p>
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
  padding: 28px clamp(24px, 4vw, 64px) 40px;
  color: var(--music-text);
  background: var(--music-canvas);
}

.home-page__masthead,
.home-page__section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: var(--music-muted);
  font-size: 10px;
  letter-spacing: 0.16em;
}

.home-page__masthead {
  padding-bottom: 22px;
  border-bottom: 1px solid var(--music-border);
}

.home-page__hero {
  display: grid;
  grid-template-columns: 1fr 0.95fr;
  align-items: center;
  min-height: 420px;
  gap: 28px;
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 0;
}

.home-page__eyebrow {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 24px;
  color: var(--music-primary);
  font-size: 10px;
  letter-spacing: 0.17em;
}

.home-page__eyebrow span {
  width: 6px;
  height: 6px;
  background: currentColor;
  border-radius: 50%;
}

.home-page__title {
  margin: 0;
  font-size: clamp(34px, 4.2vw, 62px);
  font-weight: 600;
  line-height: 1.4;
  letter-spacing: -0.04em;
}

.home-page__title em {
  color: var(--music-primary);
  font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  font-style: normal;
  font-weight: 400;
}

.home-page__subtitle {
  margin: 22px 0 28px;
  color: var(--music-muted);
  font-size: 13px;
  line-height: 1.9;
}

.home-page__actions {
  display: flex;
  align-items: center;
  gap: 24px;
}

.home-page__listen,
.home-page__collection {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  font-size: 12px;
  text-decoration: none;
  transition: background 180ms ease, color 180ms ease, transform 180ms ease;
}

.home-page__listen {
  padding: 14px 20px;
  border-radius: 5px;
  background: var(--music-primary);
  color: var(--music-on-primary);
  font-weight: 650;
}

.home-page__listen:hover {
  background: var(--music-primary-hover);
  transform: translateY(-2px);
}

.home-page__collection { color: var(--music-secondary); }
.home-page__collection:hover { color: var(--music-primary); }

.home-page__art {
  position: relative;
  isolation: isolate;
  width: min(100%, 470px);
  aspect-ratio: 1.2;
  justify-self: center;
}

.home-page__sleeve {
  position: absolute;
  z-index: 1;
  inset: 8% 25% 8% 2%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  padding: 20px;
  color: #30251b;
  background: #c99d76;
  border: 1px solid #e5bd96;
  box-shadow: 8px 16px 36px rgb(0 0 0 / 35%);
  transform: rotate(-7deg);
}

.home-page__sleeve-top,
.home-page__sleeve-bottom {
  position: relative;
  font-size: 8px;
  font-weight: 600;
  letter-spacing: 0.14em;
}

.home-page__sleeve-title {
  position: relative;
  margin-top: auto;
  margin-bottom: 16px;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(28px, 4.5vw, 58px);
  line-height: 0.96;
  letter-spacing: -0.06em;
}

.home-page__sleeve-orbit {
  position: absolute;
  top: -20%;
  right: -45%;
  width: 110%;
  aspect-ratio: 1;
  border: 1px solid rgb(48 37 27 / 40%);
  border-radius: 50%;
  box-shadow: 0 0 0 22px rgb(48 37 27 / 5%), 0 0 0 44px rgb(48 37 27 / 6%), 0 0 0 66px rgb(48 37 27 / 5%);
}

.home-page__record {
  position: absolute;
  top: 12%;
  right: 0;
  display: grid;
  place-items: center;
  width: 67%;
  aspect-ratio: 1;
  border: 1px solid #403930;
  border-radius: 50%;
  background: repeating-radial-gradient(circle, #1c1a18 0 2px, #2a2723 3px, #151413 4px 5px);
  box-shadow: 14px 22px 40px rgb(0 0 0 / 40%);
  transform: rotate(12deg);
}

.home-page__record-label {
  display: grid;
  width: 34%;
  aspect-ratio: 1;
  align-content: center;
  gap: 8px;
  border-radius: 50%;
  background: #ac7653;
  color: #241c16;
  text-align: center;
  font-family: SimSun, serif;
  font-size: 29px;
  box-shadow: 0 0 0 6px #191715;
}

.home-page__record-label small {
  font-family: sans-serif;
  font-size: 6px;
  letter-spacing: 0.04em;
}

.home-page__art-caption {
  position: absolute;
  bottom: 0;
  right: 8px;
  color: var(--music-muted);
  font-size: 9px;
  letter-spacing: 0.2em;
}

.home-page__section-heading {
  max-width: 1280px;
  margin: 0 auto 18px;
  padding-top: 24px;
  border-top: 1px solid var(--music-border);
}

.home-page__section-heading > :first-child {
  color: var(--music-secondary);
  font-size: 13px;
  letter-spacing: 0;
}

.home-page__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  max-width: 1280px;
  margin: 0 auto;
}

.home-page__card {
  padding: 22px;
  border: 1px solid var(--music-border);
  border-radius: 8px;
  background: var(--music-surface);
  transition: border-color 180ms ease, transform 180ms ease;
}

.home-page__card:hover { transform: translateY(-3px); border-color: #776049; }
.home-page__icon { width: 22px; height: 22px; margin-bottom: 16px; color: var(--music-primary); }
.home-page__card-title { margin: 0 0 8px; font-size: 14px; font-weight: 600; }
.home-page__card-desc { margin: 0; color: var(--music-muted); font-size: 12px; line-height: 1.7; }

@media (max-width: 900px) {
  .home-page__hero { min-height: 360px; }
  .home-page__title { font-size: 36px; }
  .home-page__art { max-width: 350px; }
  .home-page__sleeve { padding: 14px; }
}

@media (max-width: 700px) {
  .home-page { padding: 20px; }
  .home-page__edition { display: none; }
  .home-page__hero { grid-template-columns: 1fr; gap: 12px; padding: 36px 0 24px; }
  .home-page__intro { position: relative; z-index: 2; }
  .home-page__title { font-size: clamp(30px, 7vw, 44px); }
  .home-page__art { width: 88%; max-width: 330px; margin-top: 12px; }
  .home-page__grid { grid-template-columns: 1fr; gap: 10px; }
  .home-page__card { padding: 18px; }
  .home-page__icon { float: left; margin: 3px 16px 20px 0; }
  .home-page__sleeve-title { font-size: 38px; }
  .home-page__section-heading > :last-child { display: none; }
}
</style>
