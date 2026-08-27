// @vitest-environment happy-dom

import { describe, expect, it } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import PlaylistCard from '@/views/playlist/playlists/components/PlaylistCard.vue'

describe('PlaylistCard', () => {
  const playlist = {
    playlist_id: 8,
    playlist_name: '我的歌单',
    playlist_cover_url: '/playlist.jpg',
  }

  it('renders playlist information and detail link', () => {
    const wrapper = shallowMount(PlaylistCard, {
      props: { playlist: playlist as never },
      global: {
        stubs: {
          RouterLink: { props: ['to'], template: '<a :href="to"><slot /></a>' },
        },
      },
    })

    expect(wrapper.find('.playlist-card__name').text()).toBe('我的歌单')
    expect(wrapper.find('.playlist-card__img').attributes('src')).toBe('/playlist.jpg')
    expect(wrapper.find('a').attributes('href')).toBe('/playlists/8')
  })

  it('uses the default cover when no cover is supplied', () => {
    const wrapper = shallowMount(PlaylistCard, {
      props: { playlist: { ...playlist, playlist_cover_url: '' } as never },
      global: {
        stubs: {
          RouterLink: { template: '<a><slot /></a>' },
        },
      },
    })

    expect(wrapper.find('.playlist-card__img').attributes('src')).toBe('/default-cover.svg')
  })
})
