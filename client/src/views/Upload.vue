<template>
  <div class="h-full overflow-y-auto custom-scrollbar p-6">
    <div class="max-w-3xl mx-auto mt-4 pb-24">
      <h1 class="text-3xl font-bold mb-8 text-white">上传歌曲</h1>
      <div class="bg-gray-800 rounded-xl p-8 shadow-xl border border-gray-700">
        <div class="mb-8">
          <label class="block text-sm font-medium text-gray-400 mb-2">音频文件 (必选)</label>
          <el-upload class="audio-uploader" drag action="#" :auto-upload="false" :limit="1"
            accept="audio/mp3,audio/flac" :on-change="handleAudioChange" :on-remove="() => audioFile = null">
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="text-gray-500 text-xs mt-2">支持 MP3 / FLAC 格式</div>
            </template>
          </el-upload>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-2">封面图片 (可选)</label>
            <el-upload class="cover-uploader" action="#" :auto-upload="false" :show-file-list="false"
              :on-change="handleCoverChange">
              <img v-if="coverPreview" :src="coverPreview" class="cover-image" />
              <el-icon v-else class="cover-uploader-icon">
                <Plus />
              </el-icon>
            </el-upload>
          </div>
          <div class="space-y-4">
            <el-input v-model="form.title" placeholder="歌曲标题" dark />

            <el-input v-model="form.artist" placeholder="艺术家" />

            <el-input v-model="form.album" placeholder="专辑" />

          </div>

        </div>
        <el-button type="primary" class="w-full" size="large" :loading="uploading" @click="submitUpload">

          开始上传

        </el-button>

      </div>

    </div>

  </div>

</template>
<script setup lang="ts">
import { ref } from 'vue'
import { songApi } from '../../api/songApi'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'
const userstore = useUserStore()
const { user } = storeToRefs(userstore)

const audioFile = ref<any>(null)
const coverFile = ref<any>(null)
const coverPreview = ref<string>()
const uploading = ref(false)

const form = ref({
  title: '',
  artist: '',
  album: ''
})

const handleAudioChange = (uploadAudioFile: any) => {
  audioFile.value = uploadAudioFile;
}

const handleCoverChange = (uploadCoverImage: any) => {
  coverFile.value = uploadCoverImage.raw;
  coverPreview.value = URL.createObjectURL(uploadCoverImage.raw);
}

const submitUpload = async () => {
  if (!audioFile.value) return ElMessage.warning('请先选择音频文件');
  if (!coverFile.value) return ElMessage.warning('请选择封面');
  if (!form.value.title) return ElMessage.warning('请输入歌曲标题');
  if (!form.value.artist) return ElMessage.warning('请输入艺术家');
  if (!form.value.album) return ElMessage.warning('请输入专辑');
  uploading.value = true;
  try {
    const formdata = new FormData();
    formdata.append('audiofile', audioFile.value.raw);
    formdata.append('coverfile', coverFile.value);
    const now = new Date();
    formdata.append('uploader_name', user.value?.user_name ?? '')
    formdata.append('uploader_id', String(user.value?.user_id) ?? '')
    formdata.append('added_date', now.toISOString());
    formdata.append('title', form.value.title);
    formdata.append('artist', form.value.artist);
    formdata.append('album', form.value.album);
    const res = await songApi.uploadSong(formdata);
    if (res.success) ElMessage.success('上传成功')
    else ElMessage.error('上传失败请稍后重试')
  }
  finally { uploading.value = false; }
}

</script>