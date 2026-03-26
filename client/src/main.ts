import './assets/main.css'
import './assets/index.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import request from '../axios/axios'
import App from './App.vue'
import { routes } from './router'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

app.provide('axios', request)
app.use(ElementPlus)
const router = createRouter({
  history: createWebHistory(),
  routes,
})
app.use(router)

app.mount('#app')
