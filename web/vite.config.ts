import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'
import viteCompression from 'vite-plugin-compression'

export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    viteCompression({
      threshold: 10240,
      algorithm: 'gzip',
      ext: '.gz',
    }),
  ],
  server: {
    proxy: {
      // 接口：/api/users/login -> http://127.0.0.1:8080/api/users/login
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      // 静态资源：/static/songs/x.mp3 -> 同后端（nginx 提供 /static）
      '/static': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
    host: '127.0.0.1',
    port: 5173,
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)), //给vite的路径
    },
  },
})
