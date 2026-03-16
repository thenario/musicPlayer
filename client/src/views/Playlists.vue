<template>
  <div class="p-4">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-white">我的歌单</h1>
    </div>

    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4">
      <div v-if="userStore.isAuthenticated" @click="showCreateModal = true"
        class="bg-gray-800/50 hover:bg-gray-700 rounded-lg p-3 cursor-pointer transition-all duration-200 border-2 border-dashed border-gray-700 hover:border-blue-500 group flex flex-col items-center justify-center min-h-60">
        <el-icon :size="40" class="text-gray-400 group-hover:text-blue-500 mb-3 transition-colors">
          <Plus />
        </el-icon>
        <span class="text-gray-400 font-medium group-hover:text-white">新建歌单</span>
      </div>

    </div>

    <el-dialog v-model="showCreateModal" title="创建新歌单" width="460px" class="playlist-dialog" destroy-on-close
      @close="closeModal">
      <el-form ref="formRef" :model="newPlaylist" :rules="rules" label-position="top">
        <el-form-item label="歌单封面">
          <el-upload class="cover-uploader" action="#" :auto-upload="false" :show-file-list="false"
            :on-change="handleFileChange">
            <img v-if="previewUrl" :src="previewUrl" class="cover-preview" />
            <el-icon v-else class="uploader-icon">
              <Plus />
            </el-icon>
            <div v-if="previewUrl" class="upload-mask" @click.stop="removeImage">
              <el-icon color="white">
                <Delete />
              </el-icon>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="歌单名称" prop="name">
          <el-input v-model="newPlaylist.name" placeholder="请输入歌单名称" maxlength="20" show-word-limit />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="newPlaylist.description" type="textarea" rows="3" placeholder="介绍一下这个歌单吧..." />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3 mt-4">
          <el-button @click="closeModal">取消</el-button>
          <el-button type="primary" :loading="isSubmitting" @click="createPlaylist(formRef)">
            立即创建
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { playlistApi } from '../../api/playlistApi'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'
import { IPlaylist } from '../../type'
import { now } from '@vueuse/core'



const userStore = useUserStore()
const playlists = ref<IPlaylist[]>([])
const { user } = storeToRefs(userStore)
const formRef = ref<FormInstance>()
const showCreateModal = ref(false)
const isSubmitting = ref(false)
const previewUrl = ref('')
const selectedFile = ref<File | null>(null)

const newPlaylist = reactive({
  name: '',
  description: ''
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入歌单名称', trigger: 'blur' }]
})

const handleFileChange = (uploadFile: UploadFile) => {
  if (uploadFile.raw) {
    selectedFile.value = uploadFile.raw
    previewUrl.value = URL.createObjectURL(uploadFile.raw)
  }
}

const removeImage = () => {
  selectedFile.value = null
  previewUrl.value = ''
}

const closeModal = () => {
  showCreateModal.value = false
  removeImage()
  if (formRef.value) formRef.value.resetFields()
}

const createPlaylist = async (formEl: FormInstance | undefined) => {
  if (!formEl) return

  await formEl.validate(async (valid) => {
    if (!valid) return

    isSubmitting.value = true
    try {
      const formData = new FormData()
      formData.append('name', newPlaylist.name)
      formData.append('description', newPlaylist.description)
      formData.append('creator_id', String(user.value?.user_id))
      formData.append('created_date', now.toString())
      if (selectedFile.value) formData.append('cover_image', selectedFile.value)

      const res = await playlistApi.createPlaylist(formData)
      if (!res.success) {
        ElMessage.error("歌单创建失败")
        return
      }
      ElMessage.success('歌单创建成功')
      closeModal()
      await loadPlaylists()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.error || '创建失败')
    } finally {
      isSubmitting.value = false
    }
  })
}

const loadPlaylists = async () => {
  if (user.value) {
    const res = await playlistApi.getMyPlaylists(user.value?.user_id)
    if (!res.success) {
      ElMessage.error("获取歌单失败")
      return
    }
    playlists.value = res.playlists || []
  }

}

onMounted(loadPlaylists)
</script>