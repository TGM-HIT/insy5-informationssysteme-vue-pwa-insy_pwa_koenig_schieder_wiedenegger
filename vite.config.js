import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    plugins: [vue()],
    server: {
        port: 8082,
        proxy: {
            '/api': {
                target: 'https://localhost:8081',
                changeOrigin: true,
                secure: false
            }
        }
    }
})
