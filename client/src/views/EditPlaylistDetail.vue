<template>
    <div class="min-h-screen bg-gray-950 p-8 flex justify-center">
        <div class="max-w-5xl w-full grid grid-cols-1 md:grid-cols-12 gap-8">
            <!-- 左侧：表单 -->
            <div class="md:col-span-7 bg-gray-900 rounded-2xl p-8 border border-white/5 shadow-xl">
                <h2 class="text-2xl font-bold mb-8 flex items-center gap-2">
                    <el-icon>
                        <Edit />
                    </el-icon> 编辑歌单详情
                </h2>

                <el-form :model="form" label-position="top" class="custom-form">
                    <el-form-item label="歌单封面">
                        <div class="flex items-center gap-4">
                            <el-upload class="avatar-uploader" action="#" :auto-upload="false" :show-file-list="false"
                                :on-change="handleFileChange">
                                <div v-if="previewImage"
                                    class="relative group w-32 h-32 rounded-lg overflow-hidden border-2 border-dashed border-gray-700">
                                    <img :src="previewImage" class="w-full h-full object-cover" alt="封面图片" />
                                    <div
                                        class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity">
                                        <el-icon color="white">
                                            <Camera />
                                        </el-icon>
                                    </div>
                                </div>
                                <div v-else
                                    class="w-32 h-32 rounded-lg border-2 border-dashed border-gray-700 flex flex-col items-center justify-center text-gray-500 hover:border-blue-500 transition-colors">
                                    <el-icon :size="24">
                                        <Plus />
                                    </el-icon>
                                    <span class="text-xs mt-2">上传封面</span>
                                </div>
                            </el-upload>
                            <div class="text-xs text-gray-500">
                                支持 JPG, PNG 格式<br>建议尺寸 500x500 px
                            </div>
                        </div>
                    </el-form-item>

                    <el-form-item label="歌单名称">
                        <el-input v-model="form.name" placeholder="请输入歌单名称" maxlength="40" show-word-limit />
                    </el-form-item>

                    <el-form-item label="歌单描述">
                        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="介绍一下你的歌单..."
                            maxlength="200" show-word-limit />
                    </el-form-item>

                    <div class="flex gap-4 mt-8">
                        <el-button type="primary" size="large" class="flex-1" :loading="submitting" @click="submitForm">
                            保存修改
                        </el-button>
                        <el-button size="large" @click="router.back()">取消</el-button>
                    </div>
                </el-form>
            </div>

            <!-- 右侧：预览 -->
            <div class="md:col-span-5 flex flex-col gap-4">
                <p class="text-sm font-bold text-gray-500 uppercase tracking-widest">实时预览</p>
                <div class="sticky top-8">
                    <div class="bg-gray-900 rounded-2xl overflow-hidden shadow-2xl border border-white/5 group">
                        <div class="aspect-square relative overflow-hidden">
                            <img :src="previewImage || '/default-cover.png'" alt="封面图片"
                                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" />
                            <div class="absolute inset-0 bg-linear-to-t from-black/80 via-transparent to-transparent">
                            </div>
                            <div class="absolute bottom-4 left-4 right-4">
                                <h3 class="text-xl font-bold truncate">{{ form.name || '未命名歌单' }}</h3>
                                <p class="text-sm text-gray-400 line-clamp-2 mt-1">{{ form.description || '暂无描述...' }}
                                </p>
                            </div>
                        </div>
                        <div class="p-4 bg-gray-800/50 flex justify-between items-center">
                            <div class="flex items-center gap-2">
                                <el-avatar :size="20" class="bg-blue-600">{{ user?.user_name?.[0] || 'U' }}</el-avatar>
                                <span class="text-xs text-gray-300">{{ user?.user_name || '未知昵称' }}</span>
                            </div>
                            <span class="text-[10px] text-gray-500">预览效果</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { playlistApi } from "../../axios/playlistApi"
import { storeToRefs } from 'pinia'
import { Edit, Camera, Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { user } = storeToRefs(userStore)

const form = ref({ name: '', description: '' })
const playlistId = route.params.id as string
const previewImage = ref('')
const selectedFile = ref<File | null>(null)
const submitting = ref(false)

const getImageUrl = (url: string) => {
    if (!url) return ''
    if (url.startsWith('http')) return url
    return `${import.meta.env.VITE_API_URL}${url.startsWith('/') ? '' : '/'}${url}`
}

onMounted(async () => {
    try {
        const res = await playlistApi.getPlaylistById(Number(playlistId))
        if (res.playlist) {
            form.value.name = res.playlist.playlist_name
            form.value.description = res.playlist.description || ''
            previewImage.value = getImageUrl(res.playlist.playlist_cover_url)
        }
    } catch (err: any) {
        ElMessage.error(err.message || '加载歌单信息失败')
    }
})

const handleFileChange = (uploadFile: any) => {
    const file = uploadFile.raw
    if (!file) return
    if (!['image/jpeg', 'image/png'].includes(file.type)) {
        ElMessage.error('只能上传 JPG/PNG 格式!')
        return
    }
    selectedFile.value = file
    previewImage.value = URL.createObjectURL(file)
}

const submitForm = async () => {
    submitting.value = true
    try {
        const formData = new FormData()
        formData.append('playlist_id', playlistId)
        formData.append('name', form.value.name)
        formData.append('description', form.value.description)

        if (selectedFile.value) {
            formData.append('cover_image', selectedFile.value)
        }

        await playlistApi.editPlaylistDetails(formData)

        ElMessage.success('更新成功')

        router.push(`/playlists/${playlistId}`)

    } catch (err: any) {
        // 🚩 统一捕获 401, 403, 500 等所有异常
        console.error('更新失败:', err)
        ElMessage.error(err.message || '更新失败')
    } finally {
        submitting.value = false
    }
}
</script>