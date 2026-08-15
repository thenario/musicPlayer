import { onBeforeUnmount, ref } from 'vue'
import type { UploadFile } from 'element-plus'
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

  const load = async () => {
    try {
      const res = await playlistApi.getPlaylistById(playlistId)
      if (res.playlist) {
        form.value.name = res.playlist.playlist_name
        form.value.description = res.playlist.description || ''
        revokePreviewUrl()
        previewImage.value = getImageUrl(res.playlist.playlist_cover_url)
      }
    } catch (err: unknown) {
      // 错误已由拦截器统一提示，这里只记录日志
      console.error(err instanceof Error ? err.message : err)
    }
  }

  const handleFileChange = (uploadFile: UploadFile) => {
    const file = uploadFile.raw
    if (!file) return
    if (!PLAYLIST_COVER_TYPES.includes(file.type)) {
      ElMessage.error('只能上传 JPG/PNG 格式!')
      return
    }
    revokePreviewUrl()
    selectedFile.value = file
    previewImage.value = URL.createObjectURL(file)
  }

  /** 保存成功返回 true，由调用方决定跳转。 */
  const submit = async (): Promise<boolean> => {
    submitting.value = true
    try {
      const formData = new FormData()
      formData.append('playlist_id', playlistId)
      formData.append('name', form.value.name)
      formData.append('description', form.value.description)
      if (selectedFile.value) formData.append('cover_image', selectedFile.value)

      await playlistApi.editPlaylistDetails(formData)
      ElMessage.success('更新成功')
      return true
    } catch (err: unknown) {
      // 错误已由拦截器统一提示，这里只记录日志
      console.error('更新失败:', err instanceof Error ? err.message : err)
      return false
    } finally {
      submitting.value = false
    }
  }

  onBeforeUnmount(revokePreviewUrl)

  return { form, previewImage, selectedFile, submitting, load, handleFileChange, submit }
}
