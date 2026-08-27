// @vitest-environment happy-dom

import { ref } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'

const sliderValue = ref(35)
const safeDuration = ref(120)
const bufferPercent = ref(50)
const handleSeekInput = vi.fn()
const handleSeekChange = vi.fn()

vi.mock('@/components/player-control/composables/use-seek-bar', () => ({
  useSeekBar: () => ({ sliderValue, safeDuration, bufferPercent, handleSeekInput, handleSeekChange }),
}))

import ProgressBar from '@/components/player-control/components/ProgressBar.vue'

describe('ProgressBar', () => {
  it('renders current time, duration, and buffer progress', () => {
    const wrapper = shallowMount(ProgressBar)

    expect(wrapper.find('.progress-bar__time--current').text()).toBe('0:35')
    expect(wrapper.find('.progress-bar__time:not(.progress-bar__time--current)').text()).toBe('2:00')
    expect(wrapper.find('.progress-bar__buffer').attributes('style')).toContain('width: 50%')
    expect(wrapper.find('.progress-bar__played').attributes('style')).toContain('width: 29.166666666666668%')
  })

  it('delegates seek input and change events', async () => {
    const wrapper = shallowMount(ProgressBar)
    const input = wrapper.get('.custom-range')

    await input.trigger('input')
    await input.trigger('change')

    expect(handleSeekInput).toHaveBeenCalledOnce()
    expect(handleSeekChange).toHaveBeenCalledOnce()
  })
})
