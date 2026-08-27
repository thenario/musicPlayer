import { beforeEach, describe, expect, it, vi } from 'vitest'

const userStore = vi.hoisted(() => ({
  login: vi.fn(),
  register: vi.fn(),
}))

vi.mock('@/stores/user', () => ({ useUserStore: () => userStore }))

import { useLogin } from '@/views/user/login/composables/use-login'
import { useRegister } from '@/views/user/register/composables/use-register'

beforeEach(() => vi.resetAllMocks())

describe('useLogin', () => {
  it('reports username and password validation errors', () => {
    const login = useLogin()

    expect(login.validate()).toBe(false)
    expect(login.errors.user_name).toContain('至少为 3')
    expect(login.errors.password).toContain('至少为 6')

    login.form.user_name = 'Alice'
    login.form.password = '123456'
    expect(login.validate()).toBe(true)
    expect(login.errors).toEqual({ user_name: '', password: '' })
  })

  it('does not call the store for invalid input and delegates valid login', async () => {
    const login = useLogin()
    login.form.user_name = 'Alice'
    login.form.password = '123456'
    userStore.login.mockResolvedValue({ success: true })

    await expect(login.login()).resolves.toEqual({ success: true })
    expect(userStore.login).toHaveBeenCalledWith('Alice', '123456')
    expect(login.loading.value).toBe(false)
  })
})

describe('useRegister', () => {
  it('validates username, email, and password rules', () => {
    const register = useRegister()

    expect(register.validate()).toBe(false)
    expect(register.errors.user_email).toBe('邮箱格式不正确')

    register.form.user_name = 'Alice'
    register.form.user_email = 'alice@example.com'
    register.form.password = '12345678'
    expect(register.validate()).toBe(true)
    expect(register.errors).toEqual({ user_name: '', user_email: '', password: '' })
  })

  it('delegates valid registration to the user store', async () => {
    const register = useRegister()
    register.form.user_name = 'Alice'
    register.form.user_email = 'alice@example.com'
    register.form.password = '12345678'
    userStore.register.mockResolvedValue({ success: true })

    await expect(register.register()).resolves.toEqual({ success: true })
    expect(userStore.register).toHaveBeenCalledWith(register.form)
    expect(register.loading.value).toBe(false)
  })
})
