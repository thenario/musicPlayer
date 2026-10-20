import { onBeforeUnmount, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ActionResult } from '@/types'
import { toActionFailure } from '@/utils/action-result'
import { songApi } from '@/api/song-api'

/** 编辑上传歌曲：表单状态、歌词加载、封面上传与保存。 */
export function useEditUpload(songId: number | string) {
  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const lyricsLoading = ref(false)
  const songCoverFile = ref<File>()
  const previewUrl = ref<string>('')

  const revokePreviewUrl = () => {
    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value)
      previewUrl.value = ''
    }
  }

  const formData = reactive({
    song_name: '',
    artist: '',
    album: '',
    lyrics: '',
    t_lyrics: '',
  })

  const rules = reactive<FormRules>({
    song_name: [{ required: true, message: '歌名是必填项', trigger: 'blur' }],
  })

  const fetchLyrics = async (): Promise<ActionResult> => {
    lyricsLoading.value = true
    try {
      const res = await songApi.getLyrics(songId)
      formData.lyrics = res.lyrics || ''
      formData.t_lyrics = res.t_lyrics || ''
      return { success: true }
    } catch (err) {
      return toActionFailure(err, '歌词加载失败')
    } finally {
      lyricsLoading.value = false
    }
  }

  const handleFileChange = (e: Event): ActionResult => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (!file) return { success: false, message: '未选择图片', canceled: true }
    if (file.size > 2 * 1024 * 1024) {
      return { success: false, message: '图片不能超过 2MB' }
    }
    revokePreviewUrl()
    songCoverFile.value = file
    previewUrl.value = URL.createObjectURL(file)
    return { success: true }
  }

  /** 保存结果交给组件；表单校验失败由字段提示，不重复弹窗。 */
  const save = async (): Promise<ActionResult> => {
    if (!formRef.value) return { success: false, message: '表单尚未就绪' }
    try {
      await formRef.value.validate()
    } catch {
      return { success: false, message: '请检查表单内容', validation: true }
    }

    submitting.value = true
    try {
      const finalData = new FormData()
      if (songCoverFile.value) finalData.append('song_cover', songCoverFile.value)
      finalData.append('song_name', formData.song_name)
      finalData.append('artist', formData.artist)
      finalData.append('album', formData.album)
      finalData.append('lyrics', formData.lyrics)
      finalData.append('t_lyrics', formData.t_lyrics)
      await songApi.editUserUploadSongs(finalData, songId)
      return { success: true, message: '修改成功' }
    } catch (err) {
      return toActionFailure(err, '歌曲修改失败')
    } finally {
      submitting.value = false
    }
  }

  onBeforeUnmount(revokePreviewUrl)

  return {
    formRef,
    submitting,
    lyricsLoading,
    songCoverFile,
    previewUrl,
    formData,
    rules,
    fetchLyrics,
    handleFileChange,
    save,
  }
}
