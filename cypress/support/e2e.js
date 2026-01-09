// ***********************************************************
// This example support/e2e.js is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands'

// Alternatively you can use CommonJS syntax:
// require('./commands')

// Custom command für API requests
Cypress.Commands.add('apiRequest', (method, endpoint, body = null) => {
    const backendUrl = Cypress.env('backendUrl') || 'http://localhost:8081/api'

    return cy.request({
        method: method,
        url: `${backendUrl}${endpoint}`,
        body: body,
        failOnStatusCode: false
    })



})

after(() => {
    cy.log('Cleanup wird ausgeführt...')
    cy.cleanupDatabase()  // Ruft den custom Command auf
    cy.log('✅ Cleanup fertig!')
})
