import './assets/main.css'
import './assets/index.css' 

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import api from './utils/api' 
import VueAxios from 'vue-axios'

import App from './App.vue'
import { routes } from './router'

const app = createApp(App)
const pinia = createPinia()
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 配置axios


app.use(pinia)
app.use(router)
app.use(VueAxios, api)

app.mount('#app')
