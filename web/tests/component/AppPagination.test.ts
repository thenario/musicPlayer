// @vitest-environment happy-dom

import { describe, expect, it } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import AppPagination from '@/common/components/AppPagination.vue'

describe('AppPagination', () => {
  it('forwards pagination props and emits page changes', async () => {
    const wrapper = shallowMount(AppPagination, {
      props: { current: 2, pageSize: 20, total: 100 },
      global: {
        stubs: {
          'el-pagination': {
            name: 'ElPaginationStub',
            props: ['currentPage', 'pageSize', 'total', 'pageSizes', 'disabled'],
            template: '<div><button class="page" @click="$emit(\'update:current-page\', 3)">page</button><button class="size" @click="$emit(\'update:page-size\', 50)">size</button></div>',
          },
        },
      },
    })

    const pagination = wrapper.findComponent({ name: 'ElPaginationStub' })
    expect(pagination.props('currentPage')).toBe(2)
    expect(pagination.props('pageSize')).toBe(20)
    expect(pagination.props('total')).toBe(100)

    await wrapper.get('.page').trigger('click')
    await wrapper.get('.size').trigger('click')
    expect(wrapper.emitted('page-change')).toEqual([[3, 20], [1, 50]])
  })
})
