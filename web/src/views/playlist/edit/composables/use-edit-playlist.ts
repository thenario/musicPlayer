import { onBeforeUnmount, ref } from 'vue'
import type { UploadFile } from 'element-plus'
import type { ActionResult } from '@/types'
import { toActionFailure } from '@/utils/action-result'
import { playlistApi } from '@/api/playlist-api'
import { getImageUrl } from '@/utils/format'
import { PLAYLIST_COVER_TYPES } from '../const'

/** 编辑歌单：加载详情、表单状态、封面上传与保存。 */
export function useEditPlaylist(playlistId: string) {
  const form = ref({ name: '', description: '' })
  const previewImage = ref('')
  const selectedFile = ref<File | null>(null)
  const submitting = ref(false)

  const revokePreviewUrl = () => {
    if (previewImage.value.startsWith('blob:')) {
      URL.revokeObjectURL(previewImage.value)
      previewImage.value = ''
    }
  }

  const load = async (): Promise<ActionResult> => {
    try {
      const res = await playlistApi.getPlaylistById(playlistId)
      if (res.playlist) {
        form.value.name = res.playlist.playlist_name
        form.value.description = res.playlist.description || ''
        revokePreviewUrl()
        previewImage.value = getImageUrl(res.playlist.playlist_cover_url)
      }
      return { success: true }
    } catch (err: unknown) {
      return toActionFailure(err, '歌单详情加载失败')
    }
  }

  const handleFileChange = (uploadFile: UploadFile): ActionResult => {
    const file = uploadFile.raw
    if (!file) return { success: false, message: '未选择图片', canceled: true }
    if (!PLAYLIST_COVER_TYPES.includes(file.type)) {
      return { success: false, message: '只能上传 JPG/PNG 格式!' }
    }
    revokePreviewUrl()
    selectedFile.value = file
    previewImage.value = URL.createObjectURL(file)
    return { success: true }
  }

  /** 保存结果交给组件决定提示和跳转。 */
  const submit = async (): Promise<ActionResult> => {
    submitting.value = true
    try {
      const formData = new FormData()
      formData.append('playlist_id', playlistId)
      formData.append('name', form.value.name)
      formData.append('description', form.value.description)
      if (selectedFile.value) formData.append('cover_image', selectedFile.value)

      await playlistApi.editPlaylistDetails(formData)
      return { success: true, message: '更新成功' }
    } catch (err: unknown) {
      return toActionFailure(err, '更新歌单失败')
    } finally {
      submitting.value = false
    }
  }

  onBeforeUnmount(revokePreviewUrl)

  return { form, previewImage, selectedFile, submitting, load, handleFileChange, submit }
}
