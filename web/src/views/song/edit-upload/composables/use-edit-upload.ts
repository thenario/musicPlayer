import { onBeforeUnmount, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
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
    lyrics: '',
    t_lyrics: '',
  })

  const rules = reactive<FormRules>({
    song_name: [{ required: true, message: '歌名是必填项', trigger: 'blur' }],
  })

  const fetchLyrics = async () => {
    lyricsLoading.value = true
    try {
      const res = await songApi.getLyrics(songId)
      formData.lyrics = res.lyrics || ''
      formData.t_lyrics = res.t_lyrics || ''
    } catch (err) {
      // 错误已由拦截器统一提示，这里只记录日志
      console.error('获取歌曲详情失败:', err)
    } finally {
      lyricsLoading.value = false
    }
  }

  const handleFileChange = (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (!file) return
    if (file.size > 2 * 1024 * 1024) {
      ElMessage.warning('图片不能超过 2MB')
      return
    }
    revokePreviewUrl()
    songCoverFile.value = file
    previewUrl.value = URL.createObjectURL(file)
  }

  /** 保存成功返回 true，由调用方决定是否跳转。 */
  const save = async (): Promise<boolean> => {
    if (!formRef.value) return false
    try {
      await formRef.value.validate()
    } catch {
      return false // 校验未通过
    }

    submitting.value = true
    try {
      const finalData = new FormData()
      if (songCoverFile.value) finalData.append('song_cover', songCoverFile.value)
      finalData.append('song_name', formData.song_name)
      finalData.append('lyrics', formData.lyrics)
      finalData.append('t_lyrics', formData.t_lyrics)
      await songApi.editUserUploadSongs(finalData, songId)
      ElMessage.success('修改成功')
      return true
    } catch (err) {
      console.error(err)
      return false
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
