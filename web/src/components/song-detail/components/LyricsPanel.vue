<template>
  <div class="lyrics-wrapper" ref="lyricsContainer">
    <div v-if="lyrics && lyrics.length > 0" class="lyrics-scroll">
      <div class="scroll-spacer"></div>
      <div v-for="(line, index) in lyrics" :key="index"
        :ref="(el) => lyricRefs[index] = el as HTMLDivElement" class="lyric-line"
        :class="{ 'active': currentLineIndex === index }">
        <p class="text">{{ line.content }}</p>
        <p v-if="line.translation" class="translation">{{ line.translation }}</p>
      </div>
      <div class="scroll-spacer"></div>
    </div>

    <div v-else class="lyrics-empty">
      <p>暂无歌词内容</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'LyricsPanel' })
import { useLyricsScroll } from '../composables/use-lyrics-scroll'

const { lyrics, lyricsContainer, lyricRefs, currentLineIndex } = useLyricsScroll()
</script>

<style scoped>
.lyrics-wrapper {
  height: 100%;
  mask-image: linear-gradient(to bottom, transparent 0%, #000 12%, #000 88%, transparent 100%);
  overflow-y: auto;
  scrollbar-width: none;
}

.lyrics-wrapper::-webkit-scrollbar {
  display: none;
}

.lyric-line {
  font-size: 19px;
  font-weight: 500;
  line-height: 1.6;
  color: var(--music-muted);
  transition: color 240ms ease, transform 240ms ease;
  text-align: left;
  padding: 16px 0;
  max-width: 100%;
}

.lyric-line.active {
  color: var(--music-text);
  font-size: 24px;
  font-weight: 700;
  opacity: 1;
  transform: translateX(10px);
}

.translation {
  font-size: 16px;
  margin-top: 4px;
  color: var(--music-muted);
}

.lyrics-empty {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  color: var(--music-muted);
}

.scroll-spacer {
  height: 40%;
}
</style>
