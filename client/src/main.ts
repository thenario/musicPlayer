import './assets/main.css'
import './assets/index.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import request from '../axios/axios'
import App from './App.vue'
import { routes } from './router'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

app.provide('axios', request)
const router = createRouter({
  history: createWebHistory(),
  routes,
})
app.use(router)

app.mount('#app')
