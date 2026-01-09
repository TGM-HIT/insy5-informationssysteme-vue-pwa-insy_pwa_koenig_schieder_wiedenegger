/**
 * Cleanup Command - Löscht alle Test-Daten aus der Datenbank
 */
Cypress.Commands.add('cleanupDatabase', () => {
    cy.log('🧹 Cleaning up test data...')

    cy.request({
        method: 'DELETE',
        url: 'http://localhost:8081/api/test/cleanup',
        failOnStatusCode: false,
        timeout: 10000
    }).then((response) => {
        if (response.status === 200) {
            cy.log('Cleanup successful!', response.body)
        } else if (response.status === 404) {
            cy.log('Cleanup endpoint not found - skipping')
        } else {
            cy.log('Cleanup failed with status:', response.status)
        }
    })
})

/**
 * Cleanup ALL data - VORSICHT: Löscht die komplette Datenbank!
 */
Cypress.Commands.add('cleanupDatabaseAll', () => {
    cy.log('Cleaning ALL data from database...')

    cy.request({
        method: 'DELETE',
        url: 'http://localhost:8081/api/test/cleanup/all',
        failOnStatusCode: false,
        timeout: 10000
    }).then((response) => {
        if (response.status === 200) {
            cy.log('Full cleanup successful!')
        } else {
            cy.log('Full cleanup failed')
        }
    })
})

/**
 * Helper Commands für CRUD Operationen
 */
Cypress.Commands.add('createAnalysis', (analysisData) => {
    return cy.request('POST', 'http://localhost:8081/api/analysis', analysisData)
})

Cypress.Commands.add('createSample', (sampleData) => {
    return cy.request('POST', 'http://localhost:8081/api/sample', sampleData)
})

Cypress.Commands.add('createBox', (boxData) => {
    return cy.request('POST', 'http://localhost:8081/api/box', boxData)
})

Cypress.Commands.add('createLog', (logData) => {
    return cy.request('POST', 'http://localhost:8081/api/log', logData)
})


// Helper Commands zum Erstellen
Cypress.Commands.add('createAnalysis', (analysisData) => {
    return cy.request('POST', 'http://localhost:8081/api/analysis', analysisData)
})

Cypress.Commands.add('createSample', (sampleData) => {
    return cy.request('POST', 'http://localhost:8081/api/sample', sampleData)
})

Cypress.Commands.add('createBox', (boxData) => {
    return cy.request('POST', 'http://localhost:8081/api/box', boxData)
})

Cypress.Commands.add('createLog', (logData) => {
    return cy.request('POST', 'http://localhost:8081/api/log', logData)
})
