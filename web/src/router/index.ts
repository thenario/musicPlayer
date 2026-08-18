import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { tokenStorage, userStorage } from '@/utils/storage'

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
    path: '/login',
    name: 'login',
    component: () => import('@/views/user/login/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'regiter',
    component: () => import('@/views/user/register/Register.vue'),
  },
  {
    path: '/',
    component: () => import('@/common/components/DefaultLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: "/home",
        name: 'home',
        component: () => import('@/views/home/Home.vue')
      },
      {
        path: '/songs',
        name: 'songs',
        component: () => import('@/views/song/song-list/SongList.vue'),
        meta: { title: '搜索结果' },
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
        path: '/upload',
        name: 'upload',
        component: () => import('@/views/song/upload/SongUpload.vue'),
        meta: { requiresAuth: true, title: '上传歌曲' },
      },
    ],
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫只以 Token 是否存在为准，避免失效会话卡在访客页。
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const hasToken = !!tokenStorage.get()
  if (hasToken && !userStore.isAuthenticated) {
    const savedUser = userStorage.get()
    if (savedUser) {
      userStore.isAuthenticated = true
      userStore.user = savedUser
    }
  }

  if (to.meta.requiresAuth && !hasToken) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresGuest && hasToken) {
    next('/')
  } else {
    next()
  }
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 音乐播放器` : '音乐播放器'
})
