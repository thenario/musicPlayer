<template>
    <!-- 1. 这里的 h-full 会占据父级（Layout）的所有高度 -->
    <div class="h-full flex flex-col bg-gray-50">

        <!-- 2. 这里的容器负责滚动 -->
        <div class="flex-1 overflow-y-auto p-4 sm:p-8 custom-scrollbar">

            <!-- 3. 卡片居中限制宽度 -->
            <el-card
                class="w-full max-w-3xl mx-auto border-none shadow-lg rounded-3xl overflow-hidden animate-fade-in mb-10"
                :body-style="{ padding: '0px' }">
                <!-- 顶部装饰栏 -->
                <div class="h-24 bg-linear-to-r from-indigo-600 to-purple-600 flex items-center px-8 shrink-0">
                    <el-button circle @click="router.back()" class="hover:scale-110 transition-transform">
                        <el-icon>
                            <Back />
                        </el-icon>
                    </el-button>
                    <h1 class="text-white text-xl font-bold ml-4">编辑歌曲：{{ formData.song_name || '加载中...' }}</h1>
                </div>

                <div class="p-8">
                    <el-form ref="formRef" :model="formData" :rules="rules" label-position="top">

                        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                            <!-- 左侧：封面编辑 -->
                            <div class="flex justify-center mb-4">
                                <div
                                    class="relative w-48 h-48 group cursor-pointer shadow-xl rounded-2xl overflow-hidden border-4 border-white">
                                    <el-image :src="previewUrl" class="w-full h-full object-cover">
                                        <template #error>
                                            <div
                                                class="w-full h-full bg-gray-50 flex items-center justify-center text-gray-300">
                                                <el-icon :size="48">
                                                    <Mic />
                                                </el-icon>
                                            </div>
                                        </template>
                                    </el-image>

                                    <div
                                        class="absolute inset-0 bg-black/50 flex flex-col items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-all duration-300 backdrop-blur-sm">
                                        <el-icon :size="32"
                                            class="mb-2 transform group-hover:scale-110 transition-transform">
                                            <Camera />
                                        </el-icon>
                                        <span class="text-xs font-bold tracking-wider">更换封面</span>
                                        <div v-if="song_cover_file"
                                            class="mt-2 px-2 py-0.5 bg-green-500 rounded text-[10px]">已选择新图片</div>
                                    </div>
                                    <input type="file" accept="image/*"
                                        class="absolute inset-0 opacity-0 cursor-pointer z-10"
                                        @change="handleFileChange" />
                                </div>
                            </div>

                            <!-- 右侧：基础信息 -->
                            <div class="md:col-span-2">
                                <el-form-item label="歌曲名称" prop="song_name">
                                    <el-input v-model="formData.song_name" placeholder="请输入歌名" size="large"
                                        class="custom-input" />
                                </el-form-item>
                                <div class="mt-4 p-4 bg-blue-50 rounded-xl border border-blue-100">
                                    <p class="text-xs text-blue-600">提示：修改封面或歌名后，请点击下方保存按钮生效。</p>
                                </div>
                            </div>
                        </div>

                        <!-- 下方：歌词编辑 -->
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
                            <el-form-item label="LRC 原文歌词" prop="lyrics">
                                <el-input v-model="formData.lyrics" type="textarea" :rows="12"
                                    placeholder="[00:00.00] 歌词内容" class="lyrics-input" v-loading="lyricsLoading" />
                            </el-form-item>
                            <el-form-item label="LRC 翻译歌词" prop="t_lyrics">
                                <el-input v-model="formData.t_lyrics" type="textarea" :rows="12"
                                    placeholder="[00:00.00] 翻译内容" class="lyrics-input" v-loading="lyricsLoading" />
                            </el-form-item>
                        </div>

                        <!-- 操作按钮 -->
                        <div class="flex gap-4 mt-10">
                            <el-button type="primary" size="large" class="flex-1 save-btn" :loading="submitting"
                                @click="submitForm">
                                确认修改并保存
                            </el-button>
                            <el-button size="large" class="px-10 rounded-xl" @click="router.back()">取消</el-button>
                        </div>
                    </el-form>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useSongStore } from '@/stores/song';
import { songApi } from '../../axios/songApi';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { Back, Camera, Mic } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const songStore = useSongStore();

// 获取环境变量
const API_BASE_URL = import.meta.env.VITE_API_URL;
const song_id = Number(route.params.id);

const formRef = ref<FormInstance>();
const submitting = ref(false);
const lyricsLoading = ref(false);
const song_cover_file = ref<File>();
const previewUrl = ref<string>('');

const formData = reactive({
    song_name: '',
    lyrics: '',
    t_lyrics: '',
});

const rules = reactive<FormRules>({
    song_name: [{ required: true, message: '歌名是必填项', trigger: 'blur' }]
});

// 处理图片完整路径的函数
const getCoverUrl = (url: string) => {
    if (!url) return '';
    if (url.startsWith('http')) return url;
    const separator = url.startsWith('/') ? '' : '/';
    return `${import.meta.env.VITE_API_BASE_URL}${separator}${url}`;
};

onMounted(async () => {
    console.log('当前编辑的歌曲ID:', song_id);

    const cached = songStore.currentEditingSong;
    console.log('缓存中的完整歌曲数据:', cached);

    if (cached) {
        if (Number(cached.song_id) === song_id) {
            formData.song_name = cached.song_title || '';
            previewUrl.value = getCoverUrl(cached.song_cover_url || '');
        }
    }

    // 2. 无论是否有缓存，都去获取歌词数据
    if (song_id) {
        fetchLyrics();
    } else {
        ElMessage.error("参数错误：未获取到歌曲ID");
    }
});

const fetchLyrics = async () => {
    try {
        lyricsLoading.value = true;
        // 获取歌词
        const res = await songApi.getLyrics(song_id);
        console.log('获取到的歌词详情:', res);

        formData.lyrics = res.lyrics || '';
        formData.t_lyrics = res.t_lyrics || '';

        // // 3. 保底逻辑：如果用户直接在当前页面刷新，Pinia 缓存会丢失
        // // 此时从 API 返回值中重新补全歌名和封面
        // if (!formData.song_name && res.song_title) {
        //     formData.song_name = res.song_title;
        // }
        // if (!previewUrl.value && res.song_cover_url) {
        //     previewUrl.value = getCoverUrl(res.song_cover_url);
        // }

    } catch (err) {
        console.error('获取歌曲详情失败:', err);
        ElMessage.error("部分歌曲数据加载失败");
    } finally {
        lyricsLoading.value = false;
    }
};

const handleFileChange = (e: Event) => {
    const target = e.target as HTMLInputElement;
    const file = target.files?.[0];
    if (file) {
        if (file.size > 2 * 1024 * 1024) return ElMessage.warning('图片不能超过 2MB');
        song_cover_file.value = file;
        previewUrl.value = URL.createObjectURL(file);
    }
};

const submitForm = async () => {
    if (!formRef.value) return;
    await formRef.value.validate(async (valid) => {
        if (!valid) return;
        submitting.value = true;

        const finalData = new FormData();
        if (song_cover_file.value) {
            finalData.append('song_cover', song_cover_file.value);
        }
        // 后端接收的字段应保持一致
        finalData.append('song_name', formData.song_name);
        finalData.append('lyrics', formData.lyrics);
        finalData.append('t_lyrics', formData.t_lyrics);

        try {
            await songApi.EditUserUploadSongs(finalData, song_id);
            ElMessage.success("修改成功");
            router.push('/userUploadSongs');
        } catch (err) {
            console.error('保存失败:', err);
            ElMessage.error("保存失败");
        } finally {
            submitting.value = false;
        }
    });
};
</script>

<style scoped>
/* 核心修复：确保容器高度 */
.h-full {
    height: 100%;
}

.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #e5e7eb;
    border-radius: 10px;
}

/* 保持你原有的动画和样式 */
.animate-fade-in {
    animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

:deep(.lyrics-input .el-textarea__inner) {
    border-radius: 12px;
    background-color: #f9fafb;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
    font-size: 13px;
    padding: 12px;
    border-color: #e5e7eb;
    /* 解决部分浏览器无法滚动的关键：允许 textarea 内部滚动 */
    overflow-y: auto;
}

:deep(.custom-input .el-input__wrapper) {
    border-radius: 12px;
    box-shadow: 0 0 0 1px #e5e7eb inset;
}

.save-btn {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    border: none;
    border-radius: 12px;
    font-weight: 600;
}
</style>