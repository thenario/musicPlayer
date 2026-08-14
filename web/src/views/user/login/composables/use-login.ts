import { reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { USERNAME_MIN_LENGTH, LOGIN_PASSWORD_MIN_LENGTH } from '../../const'

/** 登录表单：状态、校验与提交。 */
export function useLogin() {
  const userStore = useUserStore()
  const loading = ref(false)
  const form = reactive({ user_name: '', password: '' })
  const errors = reactive({ user_name: '', password: '' })

  const validate = () => {
    let isValid = true
    errors.user_name = form.user_name.length < USERNAME_MIN_LENGTH ? `用户名长度至少为 ${USERNAME_MIN_LENGTH} 位` : ''
    errors.password = form.password.length < LOGIN_PASSWORD_MIN_LENGTH ? `密码长度至少为 ${LOGIN_PASSWORD_MIN_LENGTH} 位` : ''
    if (errors.user_name || errors.password) isValid = false
    return isValid
  }

  const login = async () => {
    if (!validate()) return { success: false }
    loading.value = true
    try {
      return await userStore.login(form.user_name, form.password)
    } finally {
      loading.value = false
    }
  }

  return { form, errors, loading, validate, login }
}
