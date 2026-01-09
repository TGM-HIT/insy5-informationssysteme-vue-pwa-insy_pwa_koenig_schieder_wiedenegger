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

// Vuetify Styles & Icons
import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css' // Wichtig für die Icons

// Vuetify Setup
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

const vuetify = createVuetify({
    components,
    directives,
})

createApp(App)
    .use(vuetify)
    .mount('#app')