import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresGuest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresGuest: true },
  },
  {
    path: '/userProfile',
    name: 'UserProfile', // 唯一名称
    component: () => import('../views/UserProfile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/editUserProfile',
    name: 'EditUserProfile', // 修正：不要和上面的重名
    component: () => import('../views/EditUserProfile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/userUploadSongs',
    name: 'UserUploadSongs',
    component: () => import('../views/UserUploadSongs.vue'),
    meta: { requiresAuth: true },
  },
  {
    // 修正拼写：Upload (单数)，去掉多余的 s
    path: '/EditUserUploadSong/:id',
    name: 'EditUserUploadSong',
    component: () => import('../views/EditUserUploadSongs.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/songs',
    name: 'Songs',
    component: () => import('../views/Songs.vue'),
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('../views/Upload.vue'),
    meta: { requiresAuth: true },
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 1. 恢复登录状态（处理页面刷新）
  if (!userStore.isAuthenticated) {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      userStore.isAuthenticated = true
      // 如果你的 store 需要 user 对象，记得也恢复它
      // userStore.user = JSON.parse(savedUser)
    }
  }

  // 2. 权限拦截逻辑
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresGuest && userStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export { routes }
