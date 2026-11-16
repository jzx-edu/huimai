import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5174,
    proxy: {
      // 后端接口与媒体资源统一走 /api
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true
      }
    }
  }
})
