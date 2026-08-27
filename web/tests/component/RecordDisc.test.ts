// @vitest-environment happy-dom

import { describe, expect, it } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import RecordDisc from '@/components/song-detail/components/RecordDisc.vue'

describe('RecordDisc', () => {
  it('renders the cover and playing animation state', () => {
    const wrapper = shallowMount(RecordDisc, {
      props: { cover: '/cover.jpg', isPlaying: true },
    })

    expect(wrapper.find('.record-img').attributes('src')).toBe('/cover.jpg')
    expect(wrapper.find('.record-vinyl').classes()).toContain('is-playing')
  })

  it('pauses the animation when playback is stopped', () => {
    const wrapper = shallowMount(RecordDisc, {
      props: { cover: '', isPlaying: false },
    })

    expect(wrapper.find('.record-vinyl').classes()).not.toContain('is-playing')
    expect(wrapper.find('.record-img').attributes('src')).toBe('')
  })
})
