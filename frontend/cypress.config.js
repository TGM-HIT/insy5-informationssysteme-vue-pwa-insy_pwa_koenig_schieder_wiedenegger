const { defineConfig } = require('cypress')

module.exports = defineConfig({
    e2e: {
        baseUrl: 'http://localhost:8082',
        specPattern: 'tests/**/*.cy.{js,jsx,ts,tsx}',
        supportFile: 'cypress/support/e2e.js',

        videosFolder: 'cypress/videos',
        video: true,
        videoCompression: 32,
        videoUploadOnPasses: true,

        screenshotsFolder: 'cypress/screenshots',
        screenshotOnRunFailure: true,

        reporter: 'cypress-mochawesome-reporter',  // ← Geändert
        reporterOptions: {
            reportDir: 'cypress/reports',
            charts: true,
            reportPageTitle: 'REST API E2E Tests',
            embeddedScreenshots: true,
            inlineAssets: true,
            videoOnFailOnly: false
        },

        env: {
            backendUrl: 'http://localhost:8081/api'
        },

        setupNodeEvents(on, config) {
            require('cypress-mochawesome-reporter/plugin')(on)
            require('cypress-json-results')({
                on,
                filename: 'results.json'
            })
            return config
        },
    },
})
