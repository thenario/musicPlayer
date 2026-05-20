import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

//创建一个数组保存路由
const routes = [
  {
    path: '/', //跳转时的路径
    name: 'Home', //该路径的名字
    component: () => import('../views/Home.vue'), //懒加载，声明时不执行，被调用时执行
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresGuest: true }, //在meta里加入标识，表明是否保护该页面
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresGuest: true },
  },
  {
    path: '/userProfile',
    name: 'UserProfile',
    component: () => import('../views/UserProfile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/editUserProfile',
    name: 'EditUserProfile',
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

//路由保护检查登陆状态
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (!userStore.isAuthenticated) {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      userStore.isAuthenticated = true
      userStore.user = JSON.parse(savedUser)
    }
  }

  //如果跳转的页面需要登录且用户未认证，跳转至login
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next('/login')
  }
  // //如果跳转的页面不需要登录且用户认证，跳转至主页
  else if (to.meta.requiresGuest && userStore.isAuthenticated) {
    next('/')
  }
  //其他正常跳转
  else {
    next()
  }
})
