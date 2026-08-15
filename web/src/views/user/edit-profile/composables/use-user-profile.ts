import { onBeforeUnmount, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user-api'

/** 编辑个人资料：表单状态、头像上传与保存（成功后同步 store）。 */
export function useUserProfile() {
  const userStore = useUserStore()
  const { user, userCoverUrl } = storeToRefs(userStore)

  const formRef = ref<FormInstance>()
  const coverFile = ref<File>()
  const prevCoverUrl = ref<string>(userCoverUrl.value || '')
  const preUserName = ref<string>(user.value?.user_name || '')
  const submitting = ref(false)

  const revokePreviewUrl = () => {
    if (prevCoverUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(prevCoverUrl.value)
      prevCoverUrl.value = ''
    }
  }

  const editForm = reactive({
    user_name: preUserName.value,
  })

  const rules = reactive<FormRules>({
    user_name: [
      { required: true, message: '用户名不能为空', trigger: 'blur' },
      { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
    ],
  })

  const handleFileChange = (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (!file) return
    if (file.size > 2 * 1024 * 1024) {
      ElMessage.warning('图片大小不能超过 2MB')
      return
    }
    revokePreviewUrl()
    coverFile.value = file
    prevCoverUrl.value = URL.createObjectURL(file)
  }

  /** 保存成功返回 true，由调用方决定跳转。 */
  const submit = async (): Promise<boolean> => {
    if (!formRef.value) return false
    try {
      await formRef.value.validate()
    } catch {
      return false
    }

    submitting.value = true
    try {
      const formdata = new FormData()
      formdata.append('user_name', editForm.user_name)
      if (coverFile.value) formdata.append('user_cover', coverFile.value)

      const res = await userApi.editUserProfile(formdata)
      ElMessage.success('资料修改成功！')
      if (user.value) {
        user.value.user_name = res.user_name
        userCoverUrl.value = res.user_cover_url
        revokePreviewUrl()
        prevCoverUrl.value = res.user_cover_url
      }
      return true
    } catch (err) {
      console.error(err)
      return false
    } finally {
      submitting.value = false
    }
  }

  onBeforeUnmount(revokePreviewUrl)

  return { formRef, prevCoverUrl, editForm, rules, submitting, handleFileChange, submit }
}
