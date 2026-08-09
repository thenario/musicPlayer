import { reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { USERNAME_MIN_LENGTH, REGISTER_PASSWORD_MIN_LENGTH, EMAIL_PATTERN } from '../../const'

/** 注册表单：状态、校验与提交。 */
export function useRegister() {
  const userStore = useUserStore()
  const loading = ref(false)
  const form = reactive({ user_name: '', user_email: '', password: '' })
  const errors = reactive({ user_name: '', user_email: '', password: '' })

  const validate = () => {
    let isValid = true
    errors.user_name = form.user_name.length < USERNAME_MIN_LENGTH ? `用户名至少${USERNAME_MIN_LENGTH}个字符` : ''
    errors.user_email = EMAIL_PATTERN.test(form.user_email) ? '' : '邮箱格式不正确'
    errors.password = form.password.length < REGISTER_PASSWORD_MIN_LENGTH ? `密码至少${REGISTER_PASSWORD_MIN_LENGTH}位` : ''
    if (errors.user_name || errors.user_email || errors.password) isValid = false
    return isValid
  }

  const register = async () => {
    if (!validate()) return { success: false }
    loading.value = true
    try {
      return await userStore.register(form)
    } finally {
      loading.value = false
    }
  }

  return { form, errors, loading, validate, register }
}
