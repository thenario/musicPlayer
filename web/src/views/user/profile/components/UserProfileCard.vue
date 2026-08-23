<template>
  <div class="profile-page">
    <header class="profile-page__banner">
      <div class="profile-page__banner-content">
        <span class="profile-page__eyebrow">个人中心</span>
      </div>
    </header>

    <main class="profile-page__content">
      <section class="profile-page__identity" aria-label="用户信息">
        <div class="profile-page__avatar-ring">
          <el-avatar :size="88" :src="userCoverUrl" class="profile-page__avatar">
            {{ user?.user_name?.charAt(0).toUpperCase() || 'U' }}
          </el-avatar>
        </div>

        <div class="profile-page__info">
          <h2 class="profile-page__name">{{ user?.user_name || '未登录用户' }}</h2>
          <p class="profile-page__email">{{ user?.user_email || '暂无邮箱信息' }}</p>
        </div>
      </section>

      <section class="profile-page__section" aria-labelledby="profile-actions-title">
        <div class="profile-page__section-heading">
          <h2 id="profile-actions-title" class="profile-page__section-title">我的音乐</h2>
          <p class="profile-page__section-desc">快捷进入常用的账号与内容管理功能</p>
        </div>

        <div class="profile-page__shortcut-list">
          <button type="button" class="profile-page__shortcut" @click="goToMyUploadSongs">
            <span class="profile-page__shortcut-icon profile-page__shortcut-icon--primary">
              <el-icon>
                <Collection />
              </el-icon>
            </span>
            <span class="profile-page__shortcut-text">
              <strong>我上传的音乐</strong>
              <small>查看和管理你上传的歌曲</small>
            </span>
            <el-icon class="profile-page__shortcut-arrow">
              <ArrowRight />
            </el-icon>
          </button>

          <button type="button" class="profile-page__shortcut" @click="goToEdit">
            <span class="profile-page__shortcut-icon">
              <el-icon>
                <Setting />
              </el-icon>
            </span>
            <span class="profile-page__shortcut-text">
              <strong>账号设置</strong>
              <small>修改用户名、头像和账号资料</small>
            </span>
            <el-icon class="profile-page__shortcut-arrow">
              <ArrowRight />
            </el-icon>
          </button>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import { ArrowRight, Collection, Setting } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const { user, userCoverUrl } = storeToRefs(userStore);

const goToEdit = () => {
  router.push({ path: '/edit-user-profile' });
};

const goToMyUploadSongs = () => {
  router.push({ path: '/user-uploads' });
};
</script>

<style scoped>
.profile-page {
  box-sizing: border-box;
  min-height: 100%;
  overflow: auto;
  background: #f7f8fa;
}

.profile-page__banner {
  height: 36px;
  color: #fff;
  background: linear-gradient(125deg, #6256c5, #8176d1);
}

.profile-page__banner-content {
  height: 100%;
  width: 100%;
  max-width: 1040px;
  margin: 0 auto;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding-left: 6px;
}

.profile-page__content {
  width: 100%;
  max-width: 1040px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

.profile-page__eyebrow {
  color: rgb(255 255 255 / 76%);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.profile-page__page-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.025em;
}

.profile-page__page-desc {
  margin: 10px 0 0;
  color: rgb(255 255 255 / 82%);
  font-size: 15px;
}

.profile-page__identity,
.profile-page__section {
  background: #fff;
}

.profile-page__identity {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px;
}

.profile-page__avatar-ring {
  padding: 4px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 4px 12px rgb(37 42 56 / 12%);
}

.profile-page__avatar {
  color: #6256c5;
  font-size: 28px;
  font-weight: 700;
  background: #f0effb;
}

.profile-page__info {
  min-width: 0;
}

.profile-page__name {
  margin: 0;
  overflow: hidden;
  color: #2d3340;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-page__email {
  margin: 6px 0 0;
  overflow: hidden;
  color: #9097a7;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-page__section {
  padding: 24px;
}

.profile-page__section-heading {
  margin-bottom: 16px;
}

.profile-page__section-title {
  margin: 0;
  color: #303542;
  font-size: 18px;
  font-weight: 700;
}

.profile-page__section-desc {
  margin: 6px 0 0;
  color: #858c9a;
  font-size: 13px;
}

.profile-page__shortcut-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.profile-page__shortcut {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 1px solid #e8eaf0;
  border-radius: 10px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  background: #fff;
  transition: border-color 160ms ease, background 160ms ease;
}

.profile-page__shortcut:hover {
  border-color: #bcb5e8;
  background: #faf9ff;
}

.profile-page__shortcut-icon {
  display: grid;
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 9px;
  color: #6f7787;
  background: #f1f3f7;
  font-size: 19px;
}

.profile-page__shortcut-icon--primary {
  color: #6256c5;
  background: #f0effb;
}

.profile-page__shortcut-text {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

.profile-page__shortcut-text strong,
.profile-page__shortcut-text small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-page__shortcut-text strong {
  color: #3a404d;
  font-size: 14px;
  font-weight: 600;
}

.profile-page__shortcut-text small {
  color: #9097a7;
  font-size: 12px;
}

.profile-page__shortcut-arrow {
  flex: 0 0 auto;
  color: #a0a6b4;
  transition: transform 160ms ease, color 160ms ease;
}

.profile-page__shortcut:hover .profile-page__shortcut-arrow {
  transform: translateX(2px);
  color: #6256c5;
}

@media (max-width: 640px) {
  .profile-page__banner {
    min-height: 156px;
    padding: 32px 20px;
  }

  .profile-page__page-title {
    font-size: 27px;
  }

  .profile-page__content {
    gap: 16px;
    padding: 20px 16px 36px;
  }

  .profile-page__identity,
  .profile-page__section {
    padding: 18px;
  }

  .profile-page__shortcut-list {
    grid-template-columns: 1fr;
  }
}
</style>
