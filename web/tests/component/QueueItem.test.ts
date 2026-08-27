// @vitest-environment happy-dom

import { describe, expect, it } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import QueueItem from '@/components/queue-drawer/components/QueueItem.vue'

describe('QueueItem', () => {
  const item = { queue_item_id: 1, song: { song_id: 9, song_title: 'Song', artist: 'Artist' } } as never
  const global = {
    stubs: {
      'el-icon': true,
      'el-button': { name: 'ElButtonStub', template: '<button><slot /></button>' },
    },
  }

  it('renders the song and active playing state', () => {
    const wrapper = shallowMount(QueueItem, {
      props: { item, isActive: true, isPlaying: true },
      global,
    })

    expect(wrapper.get('.queue-item__title').text()).toBe('Song')
    expect(wrapper.get('.queue-item__artist').text()).toBe('Artist')
    expect(wrapper.find('.playing-bar-animation').exists()).toBe(true)
    expect(wrapper.classes()).toContain('queue-item--active')
  })

  it('emits play on double click and remove on button click', async () => {
    const wrapper = shallowMount(QueueItem, {
      props: { item, isActive: false, isPlaying: false },
      global,
    })

    await wrapper.trigger('dblclick')
    await wrapper.findComponent({ name: 'ElButtonStub' }).vm.$emit('click')
    expect(wrapper.emitted('play')).toHaveLength(1)
    expect(wrapper.emitted('remove')).toHaveLength(1)
  })
})
