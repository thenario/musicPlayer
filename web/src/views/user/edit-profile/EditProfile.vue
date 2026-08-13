<template>
    <div class="page">
        <el-card class="profile-form profile-card"
            :body-style="{ padding: '0px' }">
            <div class="profile-form__banner"></div>

            <div class="profile-form__body">
                <div class="profile-form__avatar-wrap">
                    <div class="profile-form__avatar-ring">
                        <el-avatar :size="100" :src="prevCoverUrl" class="profile-form__avatar">
                            {{ editForm.user_name?.charAt(0).toUpperCase() || 'U' }}
                        </el-avatar>
                        <div
                            class="profile-form__overlay">
                            <el-icon :size="24">
                                <Camera />
                            </el-icon>
                            <span class="profile-form__overlay-text">更换头像</span>
                            <input type="file" accept="image/*" class="profile-form__overlay-input"
                                @change="handleFileChange" />
                        </div>
                    </div>
                </div>

                <h1 class="profile-form__title">编辑个人资料</h1>

                <el-form ref="formRef" :model="editForm" :rules="rules" label-position="top" class="profile-form__form">
                    <el-form-item label="用户名" prop="user_name">
                        <el-input v-model="editForm.user_name" placeholder="请输入新的用户名" size="large" class="custom-input"
                            maxlength="20" show-word-limit />
                    </el-form-item>


                    <div class="profile-form__actions">
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
defineOptions({ name: 'EditProfilePage' })
import { useRouter } from 'vue-router';
import { Camera } from '@element-plus/icons-vue';
import { useUserProfile } from './composables/use-user-profile';

const router = useRouter();
const { formRef, prevCoverUrl, editForm, rules, submitting, handleFileChange, submit } = useUserProfile();

const submitForm = async () => {
    if (await submit()) router.push('/user-profile');
};

const goBack = () => {
    router.push('/user-profile');
};
</script>

<style scoped>
@reference "../../../assets/index.css";

.page {
    @apply min-h-[80vh] flex items-center justify-center p-4;
}

.profile-form {
    @apply w-full max-w-md border-none overflow-hidden relative;
}

.profile-form__banner {
    @apply h-24 bg-linear-to-r from-indigo-500 via-purple-500 to-pink-500;
}

.profile-form__body {
    @apply relative px-6 pb-10 flex flex-col items-center;
}

.profile-form__avatar-wrap {
    @apply relative -mt-12 mb-6;
}

.profile-form__avatar-ring {
    @apply p-1 bg-white rounded-full shadow-xl relative;
}

.profile-form__avatar {
    @apply bg-gray-100 text-3xl font-bold;
}

.profile-form__overlay {
    @apply absolute inset-1 bg-black rounded-full flex flex-col items-center justify-center text-white opacity-0 transition-opacity cursor-pointer;
}

.profile-form__avatar-ring:hover .profile-form__overlay {
    @apply opacity-100;
}

.profile-form__overlay-text {
    @apply text-xs mt-1;
}

.profile-form__overlay-input {
    @apply absolute inset-0 opacity-0 cursor-pointer;
}

.profile-form__title {
    @apply text-xl font-bold text-gray-800 mb-6;
}

.profile-form__form {
    @apply w-full px-4;
}

.profile-form__actions {
    @apply flex flex-col gap-3 mt-8 w-full;
}

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