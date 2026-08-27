// @vitest-environment happy-dom

import { ref } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'

const volumeValue = ref(50)
const toggleMute = vi.fn()
const setVolume = vi.fn()

vi.mock('@/components/player-bar/composables/use-volume-control', () => ({
  useVolumeControl: () => ({ volumeValue, toggleMute, setVolume }),
}))

import VolumeControl from '@/components/player-bar/components/VolumeControl.vue'

describe('VolumeControl', () => {
  it('toggles mute when the speaker button is clicked', async () => {
    const wrapper = shallowMount(VolumeControl)

    await wrapper.get('.volume-control__mute-btn').trigger('click')

    expect(toggleMute).toHaveBeenCalledOnce()
  })

  it('passes the range input value to setVolume', async () => {
    const wrapper = shallowMount(VolumeControl)
    const input = wrapper.get('.volume-range')

    await input.setValue('30')

    expect(setVolume).toHaveBeenCalledWith(30)
  })
})
