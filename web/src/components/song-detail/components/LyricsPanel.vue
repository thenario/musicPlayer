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
  font-size: 17px;
  font-weight: 500;
  line-height: 1.6;
  color: #a8abb2;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: left;
  padding: 12px 0;
  max-width: 100%;
}

.lyric-line.active {
  color: #303133;
  font-size: 22px;
  font-weight: 700;
  opacity: 1;
  transform: translateX(10px);
}

.translation {
  font-size: 16px;
  margin-top: 4px;
  color: #909399;
}

.lyrics-empty {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  color: #909399;
}

.scroll-spacer {
  height: 40%;
}
</style>
