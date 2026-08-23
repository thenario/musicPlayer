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
.page {
    box-sizing: border-box;
    display: flex;
    min-height: 100%;
    align-items: center;
    justify-content: center;
    padding: 32px 24px;
    background: #f7f8fa;
}

.profile-form {
    position: relative;
    width: 100%;
    max-width: 440px;
    overflow: hidden;
    border: 1px solid #e6e8ee;
}

.profile-form__banner {
    height: 96px;
    background: linear-gradient(125deg, #6256c5, #8176d1);
}

.profile-form__body {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0 28px 32px;
}

.profile-form__avatar-wrap {
    position: relative;
    margin-top: -48px;
    margin-bottom: 20px;
}

.profile-form__avatar-ring {
    position: relative;
    padding: 4px;
    border-radius: 50%;
    background: #fff;
    box-shadow: 0 4px 12px rgb(37 42 56 / 14%);
}

.profile-form__avatar {
    color: #6256c5;
    font-size: 28px;
    font-weight: 700;
    background: #f0effb;
}

.profile-form__overlay {
    position: absolute;
    inset: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    opacity: 0;
    color: #fff;
    cursor: pointer;
    background: rgb(24 27 35 / 62%);
    transition: opacity 160ms ease;
}

.profile-form__avatar-ring:hover .profile-form__overlay {
    opacity: 1;
}

.profile-form__overlay-text {
    margin-top: 4px;
    font-size: 12px;
}

.profile-form__overlay-input {
    position: absolute;
    inset: 0;
    opacity: 0;
    cursor: pointer;
}

.profile-form__title {
    margin: 0 0 22px;
    color: #303542;
    font-size: 20px;
    font-weight: 700;
}

.profile-form__form {
    width: 100%;
}

.profile-form__actions {
    display: flex;
    flex-direction: column;
    width: 100%;
    gap: 10px;
    margin-top: 26px;
}

/* 继承上一版的卡片悬浮特效和圆角 */
.profile-card {
    border-radius: 14px;
    box-shadow: 0 4px 14px rgb(37 42 56 / 8%);
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
    height: 44px;
    border: 0;
    border-radius: 8px;
    background: #6256c5;
    font-weight: 600;
    font-size: 0.95rem;
}

.save-btn:hover {
    background: #5549b7;
}

/* 取消按钮 */
.cancel-btn {
    height: 44px;
    border-radius: 8px;
    font-weight: 500;
    border-color: #e5e7eb;
    color: #6b7280;
}

.cancel-btn:hover {
    background-color: #f7f6ff;
    color: #6256c5;
    border-color: #aaa2df;
}
</style>
