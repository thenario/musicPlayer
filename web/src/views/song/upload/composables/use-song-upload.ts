import { onBeforeUnmount, ref } from 'vue'
import type { AxiosProgressEvent } from 'axios'
import type { ActionResult } from '@/types'
import { toActionFailure } from '@/utils/action-result'
import { songApi } from '@/api/song-api'
import { AUDIO_EXTENSIONS } from '../const'

/** 上传歌曲：文件选择/拖拽校验、表单状态、上传进度与提交。 */
export function useSongUpload() {
  const audioInput = ref<HTMLInputElement | null>(null)
  const coverInput = ref<HTMLInputElement | null>(null)
  const audioFile = ref<File | null>(null)
  const coverFile = ref<File | null>(null)
  const coverPreview = ref<string>('')
  const isDragging = ref(false)
  const uploading = ref(false)
  const uploadProgress = ref(0)

  const form = ref({
    title: '',
    artist: '',
    album: '',
    lyrics: '',
  })

  const revokeCoverPreview = () => {
    if (coverPreview.value) {
      URL.revokeObjectURL(coverPreview.value)
      coverPreview.value = ''
    }
  }

  const validateAndSetAudio = (file: File): ActionResult => {
    const isAudio = AUDIO_EXTENSIONS.some((ext) => file.name.toLowerCase().endsWith(ext))
    if (!isAudio) {
      return { success: false, message: '不支持的文件格式' }
    }
    audioFile.value = file

    // 文件名形如 "标题_歌手.mp3" 时自动填充标题/歌手
    if (!form.value.title && file.name.includes('_')) {
      const parts = file.name.split('.')[0]!.split('_')
      form.value.title = parts[0]!
      form.value.artist = parts[1] || ''
    } else if (!form.value.title) {
      form.value.title = file.name.split('.')[0]!
    }
    return { success: true }
  }

  const handleAudioSelect = (e: Event): ActionResult => {
    const files = (e.target as HTMLInputElement).files
    return files?.[0] ? validateAndSetAudio(files[0])
      : { success: false, message: '未选择音频', canceled: true }
  }

  const handleAudioDrop = (e: DragEvent): ActionResult => {
    isDragging.value = false
    const files = e.dataTransfer?.files
    return files?.[0] ? validateAndSetAudio(files[0])
      : { success: false, message: '未选择音频', canceled: true }
  }

  const handleCoverSelect = (e: Event): ActionResult => {
    const files = (e.target as HTMLInputElement).files
    if (files && files[0]) {
      revokeCoverPreview()
      coverFile.value = files[0]
      coverPreview.value = URL.createObjectURL(files[0])
      return { success: true }
    }
    return { success: false, message: '未选择封面', canceled: true }
  }

  const submit = async (): Promise<ActionResult> => {
    if (!audioFile.value) return { success: false, message: '请选择音频文件' }
    if (!coverFile.value) return { success: false, message: '请选择封面图片' }
    if (!form.value.title) return { success: false, message: '请输入歌曲标题' }

    uploading.value = true
    uploadProgress.value = 0

    try {
      const formData = new FormData()
      formData.append('audiofile', audioFile.value)
      formData.append('coverfile', coverFile.value)
      formData.append('title', form.value.title)
      formData.append('artist', form.value.artist)
      formData.append('album', form.value.album)
      formData.append('lyrics', form.value.lyrics)

      await songApi.uploadSong(formData, (progressEvent: AxiosProgressEvent) => {
        if (progressEvent.total) {
          uploadProgress.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        }
      })

      reset()
      return { success: true, message: '歌曲上传成功！' }
    } catch (error: unknown) {
      return toActionFailure(error, '歌曲上传失败')
    } finally {
      uploading.value = false
    }
  }

  const reset = () => {
    audioFile.value = null
    coverFile.value = null
    revokeCoverPreview()
    uploadProgress.value = 0
    form.value = { title: '', artist: '', album: '', lyrics: '' }
    if (audioInput.value) audioInput.value.value = ''
    if (coverInput.value) coverInput.value.value = ''
  }

  onBeforeUnmount(revokeCoverPreview)

  return {
    audioInput,
    coverInput,
    audioFile,
    coverFile,
    coverPreview,
    isDragging,
    uploading,
    uploadProgress,
    form,
    handleAudioSelect,
    handleAudioDrop,
    handleCoverSelect,
    submit,
    reset,
  }
}
