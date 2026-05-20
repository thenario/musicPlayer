import './assets/main.css' //全局样式
import './assets/index.css' //tdcss
import { createApp } from 'vue' //用于创建vue实例
import { createPinia } from 'pinia' //创建状态管理仓库
import request from '../axios/axios' //引入封装的axios
import App from './App.vue' //引入根组件
import { router } from './router' //引入路由规则

const app = createApp(App) //以根组件创建一个vue实例
const pinia = createPinia() //创建一个pinia实例
app.use(pinia) //将插件安装到应用当中

app.provide('axios', request) //vue的依赖注入，把封装好的axios广播出去
app.use(router)
app.use(router) //将路由插件安装到应用当中

app.mount('#app') //将内容挂载到index里的那个appdiv里
