// @vitest-environment happy-dom

import { ref } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'

const lyrics = ref([
  { time: 0, content: '第一句' },
  { time: 10, content: '第二句', translation: 'Translation' },
])
const lyricsContainer = ref(null)
const lyricRefs = ref([])
const currentLineIndex = ref(1)

vi.mock('@/components/song-detail/composables/use-lyrics-scroll', () => ({
  useLyricsScroll: () => ({ lyrics, lyricsContainer, lyricRefs, currentLineIndex }),
}))

import LyricsPanel from '@/components/song-detail/components/LyricsPanel.vue'

describe('LyricsPanel', () => {
  it('renders lyric lines and highlights the current line', () => {
    const wrapper = shallowMount(LyricsPanel)

    expect(wrapper.findAll('.lyric-line')).toHaveLength(2)
    expect(wrapper.findAll('.lyric-line')[1]!.classes()).toContain('active')
    expect(wrapper.find('.translation').text()).toBe('Translation')
  })

  it('renders an empty state when no lyrics are available', () => {
    lyrics.value = []
    const wrapper = shallowMount(LyricsPanel)

    expect(wrapper.find('.lyrics-empty').text()).toBe('暂无歌词内容')
    lyrics.value = [{ time: 0, content: '第一句' }, { time: 10, content: '第二句', translation: 'Translation' }]
  })
})
