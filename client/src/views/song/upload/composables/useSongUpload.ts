import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { songApi } from '@/api/songApi'
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

  const validateAndSetAudio = (file: File) => {
    const isAudio = AUDIO_EXTENSIONS.some((ext) => file.name.toLowerCase().endsWith(ext))
    if (!isAudio) {
      ElMessage.warning('不支持的文件格式')
      return
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
  }

  const handleAudioSelect = (e: Event) => {
    const files = (e.target as HTMLInputElement).files
    if (files && files[0]) validateAndSetAudio(files[0])
  }

  const handleAudioDrop = (e: DragEvent) => {
    isDragging.value = false
    const files = e.dataTransfer?.files
    if (files && files[0]) validateAndSetAudio(files[0])
  }

  const handleCoverSelect = (e: Event) => {
    const files = (e.target as HTMLInputElement).files
    if (files && files[0]) {
      coverFile.value = files[0]
      coverPreview.value = URL.createObjectURL(files[0])
    }
  }

  const submit = async (uploaderId?: number) => {
    if (!audioFile.value) return ElMessage.warning('请选择音频文件')
    if (!coverFile.value) return ElMessage.warning('请选择封面图片')
    if (!form.value.title) return ElMessage.warning('请输入歌曲标题')

    uploading.value = true
    uploadProgress.value = 0

    try {
      const formData = new FormData()
      formData.append('audiofile', audioFile.value)
      formData.append('coverfile', coverFile.value)
      formData.append('uploader_id', String(uploaderId ?? 0))
      formData.append('title', form.value.title)
      formData.append('artist', form.value.artist)
      formData.append('album', form.value.album)
      formData.append('lyrics', form.value.lyrics)

      await songApi.uploadSong(formData, (progressEvent: any) => {
        if (progressEvent.total) {
          uploadProgress.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        }
      })

      ElMessage.success('歌曲上传成功！')
      reset()
    } catch (error: any) {
      // 错误已由拦截器统一提示，这里只记录日志
      console.error('Upload Error:', error)
    } finally {
      uploading.value = false
    }
  }

  const reset = () => {
    audioFile.value = null
    coverFile.value = null
    coverPreview.value = ''
    uploadProgress.value = 0
    form.value = { title: '', artist: '', album: '', lyrics: '' }
    if (audioInput.value) audioInput.value.value = ''
    if (coverInput.value) coverInput.value.value = ''
  }

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
