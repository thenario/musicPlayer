<template>
    <div class="min-h-[80vh] flex items-center justify-center p-4">
        <el-card class="w-full max-w-md border-none overflow-hidden relative profile-card"
            :body-style="{ padding: '0px' }">
            <div class="h-24 bg-linear-to-r from-indigo-500 via-purple-500 to-pink-500"></div>

            <div class="relative px-6 pb-10 flex flex-col items-center">
                <div class="relative -mt-12 mb-6">
                    <div class="p-1 bg-white rounded-full shadow-xl relative group">
                        <el-avatar :size="100" :src="prevCoverUrl" class="bg-gray-100 text-3xl font-bold">
                            {{ editForm.user_name?.charAt(0).toUpperCase() || 'U' }}
                        </el-avatar>
                        <div
                            class="absolute inset-1 bg-black bg-opacity-40 rounded-full flex flex-col items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
                            <el-icon :size="24">
                                <Camera />
                            </el-icon>
                            <span class="text-xs mt-1">更换头像</span>
                            <input type="file" accept="image/*" class="absolute inset-0 opacity-0 cursor-pointer"
                                @change="handleFileChange" />
                        </div>
                    </div>
                </div>

                <h1 class="text-xl font-bold text-gray-800 mb-6">编辑个人资料</h1>

                <el-form ref="formRef" :model="editForm" :rules="rules" label-position="top" class="w-full px-4">
                    <el-form-item label="用户名" prop="user_name">
                        <el-input v-model="editForm.user_name" placeholder="请输入新的用户名" size="large" class="custom-input"
                            maxlength="20" show-word-limit />
                    </el-form-item>


                    <div class="flex flex-col gap-3 mt-8 w-full">
                        <el-button type="primary" size="large" class="save-btn" :loading="submitting"
                            @click="submitForm">
                            保存修改
                        </el-button>

                        <el-button size="large" plain class="cancel-btn" @click="goBack">
                            取消
                        </el-button>
                    </div>
                </el-form>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user';
import { userApi } from '../../axios/userApi';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { storeToRefs } from 'pinia';
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Camera } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const { user, userCoverUrl } = storeToRefs(userStore);

const formRef = ref<FormInstance>();
const preCover = ref<File>();
const prevCoverUrl = ref<string>(userCoverUrl.value || '');
const preUserName = ref<string>(user.value?.user_name || "");
const submitting = ref(false);

const editForm = reactive({
    user_name: preUserName.value,
});

const rules = reactive<FormRules>({
    user_name: [
        { required: true, message: '用户名不能为空', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ]
});

const handleFileChange = (e: Event) => {
    const target = e.target as HTMLInputElement;
    const file = target.files?.[0];
    if (file) {
        if (file.size > 2 * 1024 * 1024) {
            ElMessage.warning('图片大小不能超过 2MB');
            return;
        }
        preCover.value = file;
        prevCoverUrl.value = URL.createObjectURL(file);
    }
};

const submitForm = async () => {
    if (!formRef.value) return;

    await formRef.value.validate(async (valid) => {
        if (!valid) return;

        submitting.value = true;
        const formdata = new FormData();
        formdata.append('user_name', editForm.user_name);

        if (preCover.value) {
            formdata.append('user_cover', preCover.value);
        }

        try {
            const res = await userApi.editUserProfile(formdata);
            ElMessage.success("资料修改成功！");
            if (user.value) {
                user.value.user_name = res.user_name;
                userCoverUrl.value = res.user_cover_url;
            }
            router.push('/userProfile');
        } catch (err) {
            console.error(err);
            ElMessage.error("修改失败");
        } finally {
            submitting.value = false;
        }
    });
};

const goBack = () => {
    router.push('/userProfile');
};
</script>

<style scoped>
/* 继承上一版的卡片悬浮特效和圆角 */
.profile-card {
    border-radius: 24px;
    box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.1);
    transition: all 0.4s ease;
}

/* 输入框圆角优化 */
:deep(.custom-input .el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px #e5e7eb inset;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #6366f1 inset !important;
}

/* 主保存按钮（延续渐变风格） */
.save-btn {
    height: 46px;
    border-radius: 12px;
    background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
    border: none;
    font-weight: 600;
    font-size: 0.95rem;
}

.save-btn:hover {
    opacity: 0.9;
    transform: translateY(-1px);
}

/* 取消按钮 */
.cancel-btn {
    height: 46px;
    border-radius: 12px;
    font-weight: 500;
    border-color: #e5e7eb;
    color: #6b7280;
}

.cancel-btn:hover {
    background-color: #f9fafb;
    color: #4f46e5;
    border-color: #4f46e5;
}
</style>