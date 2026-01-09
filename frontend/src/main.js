import { createApp } from 'vue'
import App from './App.vue'
import { registerSW } from 'virtual:pwa-register'

// Service Worker registrieren
const updateSW = registerSW({
    onNeedRefresh() {
        if (confirm('Neue Version verfügbar! Jetzt aktualisieren?')) {
            updateSW(true)
        }
    },
    onOfflineReady() {
        console.log('App ist offline-bereit')
    }
})

createApp(App).mount('#app')