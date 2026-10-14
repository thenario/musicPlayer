import { onBeforeUnmount, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'
import type { FormInstance, FormRules } from 'element-plus'
import type { ActionResult } from '@/types'
import { toActionFailure } from '@/utils/action-result'
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

  const handleFileChange = (e: Event): ActionResult => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (!file) return { success: false, message: '未选择图片', canceled: true }
    if (file.size > 2 * 1024 * 1024) {
      return { success: false, message: '图片大小不能超过 2MB' }
    }
    revokePreviewUrl()
    coverFile.value = file
    prevCoverUrl.value = URL.createObjectURL(file)
    return { success: true }
  }

  /** 保存结果交给页面；字段校验失败不再额外弹窗。 */
  const submit = async (): Promise<ActionResult> => {
    if (!formRef.value) return { success: false, message: '表单尚未就绪' }
    try {
      await formRef.value.validate()
    } catch {
      return { success: false, message: '请检查表单内容', validation: true }
    }

    submitting.value = true
    try {
      const formdata = new FormData()
      formdata.append('user_name', editForm.user_name)
      if (coverFile.value) formdata.append('user_cover', coverFile.value)

      const res = await userApi.editUserProfile(formdata)
      if (user.value) {
        user.value.user_name = res.user_name
        userCoverUrl.value = res.user_cover_url
        revokePreviewUrl()
        prevCoverUrl.value = res.user_cover_url
      }
      return { success: true, message: '资料修改成功！' }
    } catch (err) {
      return toActionFailure(err, '资料修改失败')
    } finally {
      submitting.value = false
    }
  }

  onBeforeUnmount(revokePreviewUrl)

  return { formRef, prevCoverUrl, editForm, rules, submitting, handleFileChange, submit }
}
