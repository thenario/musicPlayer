// @vitest-environment happy-dom

import { describe, expect, it } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import QueuePreviewList from '@/components/queue-drawer/components/QueuePreviewList.vue'

describe('QueuePreviewList', () => {
  it('renders queue title, count, and songs', () => {
    const wrapper = shallowMount(QueuePreviewList, {
      props: {
        queue: {
          queue_id: 1,
          queue_name: 'My Queue',
          queue_items: [
            { queue_item_id: 7, queue_item_position: 1, song: { song_title: 'Song', artist: 'Artist' } },
          ],
        } as never,
      },
      global: { stubs: { 'el-icon': true } },
    })

    expect(wrapper.get('.queue-preview-list__title').text()).toBe('My Queue')
    expect(wrapper.get('.queue-preview-list__count').text()).toBe('1 首歌')
    expect(wrapper.get('.queue-preview-list__song').text()).toBe('Song')
    expect(wrapper.get('.queue-preview-list__artist').text()).toBe('Artist')
  })

  it('emits the clicked item index', async () => {
    const wrapper = shallowMount(QueuePreviewList, {
      props: { queue: { queue_name: 'Queue', queue_items: [{ queue_item_id: 1, song: { song_title: 'Song' } }] } as never },
      global: { stubs: { 'el-icon': true } },
    })

    await wrapper.get('.queue-preview-list__row').trigger('click')
    expect(wrapper.emitted('play')).toEqual([[0]])
  })
})
