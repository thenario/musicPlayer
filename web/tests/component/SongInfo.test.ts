// @vitest-environment happy-dom

import { describe, expect, it } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import SongInfo from '@/components/player-bar/components/SongInfo.vue'

describe('SongInfo', () => {
  it('renders song metadata and cover image', () => {
    const wrapper = shallowMount(SongInfo, {
      props: { cover: '/cover.jpg', title: 'Test Song', artist: 'Test Artist' },
      global: { stubs: { 'el-icon': true } },
    })

    expect(wrapper.get('.song-info__title').text()).toBe('Test Song')
    expect(wrapper.get('.song-info__artist').text()).toBe('Test Artist')
    expect(wrapper.get('img').attributes('src')).toBe('/cover.jpg')
  })

  it('renders a fallback without a cover and emits expand on click', async () => {
    const wrapper = shallowMount(SongInfo, {
      props: { cover: '', title: 'Test Song', artist: 'Test Artist' },
      global: { stubs: { 'el-icon': true } },
    })

    expect(wrapper.find('.song-info__cover-fallback').exists()).toBe(true)
    await wrapper.get('.song-info__cover-wrap').trigger('click')
    expect(wrapper.emitted('expand')).toHaveLength(1)
  })
})
