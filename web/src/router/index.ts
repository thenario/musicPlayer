import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userStorage } from '@/utils/storage'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    requiresGuest?: boolean
    title?: string
  }
}

//创建一个数组保存路由
const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/home/Home.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/user/login/Login.vue'),
    meta: { requiresGuest: true, title: '登录' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/user/register/Register.vue'),
    meta: { requiresGuest: true, title: '注册' },
  },
  {
    path: '/user-profile',
    name: 'user-profile',
    component: () => import('@/views/user/profile/Profile.vue'),
    meta: { requiresAuth: true, title: '个人中心' },
  },
  {
    path: '/edit-user-profile',
    name: 'edit-user-profile',
    component: () => import('@/views/user/edit-profile/EditProfile.vue'),
    meta: { requiresAuth: true, title: '编辑资料' },
  },
  {
    path: '/user-uploads',
    name: 'user-uploads',
    component: () => import('@/views/song/my-uploads/MyUploads.vue'),
    meta: { requiresAuth: true, title: '我的上传' },
  },
  {
    path: '/user-uploads/:id/edit',
    name: 'edit-user-upload',
    component: () => import('@/views/song/edit-upload/EditUpload.vue'),
    meta: { requiresAuth: true, title: '编辑上传歌曲' },
  },
  {
    path: '/songs',
    name: 'songs',
    component: () => import('@/views/song/song-list/SongList.vue'),
    meta: { title: '歌曲库' },
  },
  {
    path: '/upload',
    name: 'upload',
    component: () => import('@/views/song/upload/SongUpload.vue'),
    meta: { requiresAuth: true, title: '上传歌曲' },
  },
  {
    path: '/playlists',
    name: 'playlists',
    component: () => import('@/views/playlist/playlists/Playlists.vue'),
    meta: { title: '歌单' },
  },
  {
    path: '/playlists/:id',
    name: 'playlist-detail',
    component: () => import('@/views/playlist/detail/PlaylistDetail.vue'),
    meta: { title: '歌单详情' },
  },
  {
    path: '/playlists/:id/edit',
    name: 'edit-playlist',
    component: () => import('@/views/playlist/edit/PlaylistEdit.vue'),
    meta: { requiresAuth: true, title: '编辑歌单' },
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

//路由保护检查登陆状态
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (!userStore.isAuthenticated) {
    const savedUser = userStorage.get()
    if (savedUser) {
      userStore.isAuthenticated = true
      userStore.user = savedUser
    }
  }

  //如果跳转的页面需要登录且用户未认证，跳转至login
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next('/login')
  }
  //如果跳转的页面不需要登录且用户认证，跳转至主页
  else if (to.meta.requiresGuest && userStore.isAuthenticated) {
    next('/')
  }
  //其他正常跳转
  else {
    next()
  }
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 音乐播放器` : '音乐播放器'
})
