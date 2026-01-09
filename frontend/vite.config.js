import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
    plugins: [
        vue(),
        VitePWA({
            registerType: 'autoUpdate',
            manifest: {
                name: 'Labor Management System',
                short_name: 'LabMS',
                description: 'TGM INSY Labor Verwaltung',
                theme_color: '#4DBA87',
                background_color: '#ffffff',
                display: 'standalone',  // Vollbild ohne Browser-UI
                start_url: '/',
                icons: [
                    {
                        src: '/icon-192.png',
                        sizes: '192x192',
                        type: 'image/png'
                    },
                    {
                        src: '/icon-512.png',
                        sizes: '512x512',
                        type: 'image/png'
                    }
                ]
            },
            workbox: {
                globPatterns: ['**/*.{js,css,html,ico,png,svg,json,vue,txt,woff2}'],

                runtimeCaching: [
                    {
                        urlPattern: /^https?:\/\/.*\/api\/.*/,  // Alle API-Calls
                        handler: 'NetworkFirst',
                        options: {
                            cacheName: 'api-cache',
                            expiration: {
                                maxEntries: 50,
                                maxAgeSeconds: 60 * 60 * 24  // 24 Stunden
                            },
                            cacheableResponse: {
                                statuses: [0, 200]  // Nur erfolgreiche Responses cachen
                            }
                        }
                    }
                ]
            }
        })
    ],
    server: {
        port: 8082,
        proxy: {
            '/api': {
                target: 'http://backend:8081',
                changeOrigin: true,
                secure: false
            }
        }
    }
})
