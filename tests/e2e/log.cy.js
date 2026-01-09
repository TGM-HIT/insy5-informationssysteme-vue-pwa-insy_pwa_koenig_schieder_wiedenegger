describe('Log Display (Read-Only)', () => {
    const BACKEND_URL = 'http://localhost:8081/api'
    const API_URL = `${BACKEND_URL}/log`

    beforeEach(() => {
        cy.visit('/')
    })


    it('should DISPLAY all log entries', () => {
        cy.request('GET', API_URL)
            .then((response) => {
                expect(response.status).to.eq(200)
                expect(response.body).to.be.an('array')
            })
    })

    it('should DISPLAY log entry by ID if logs exist', () => {
        // Hole alle Logs
        cy.request('GET', API_URL)
            .then((response) => {
                expect(response.status).to.eq(200)

                // Falls Logs existieren, zeige einen davon an
                if (response.body.length > 0) {
                    const firstLog = response.body[0]

                    cy.request('GET', `${API_URL}/${firstLog.id}`)
                        .then((detailResponse) => {
                            expect(detailResponse.status).to.eq(200)
                            expect(detailResponse.body).to.have.property('id')
                            expect(detailResponse.body).to.have.property('level')
                            expect(detailResponse.body).to.have.property('info')
                        })
                } else {
                    cy.log('Keine Logs in Datenbank - Test übersprungen')
                }
            })
    })

    it('should DISPLAY log properties correctly', () => {
        cy.request('GET', API_URL)
            .then((response) => {
                expect(response.status).to.eq(200)

                if (response.body.length > 0) {
                    const log = response.body[0]

                    // Prüfe dass erforderliche Felder vorhanden sind
                    expect(log).to.have.property('id')
                    expect(log).to.have.property('level')
                    expect(log).to.have.property('info')
                    expect(log).to.have.property('dateCreated')
                }
            })
    })

    it('should handle ERROR when getting non-existent log', () => {
        cy.request({
            method: 'GET',
            url: `${API_URL}/999999`,
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(404)
        })
    })

    it('should return ERROR for invalid log ID', () => {
        cy.request({
            method: 'GET',
            url: `${API_URL}/invalid-id`,
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.be.oneOf([400, 404])
        })
    })
})
