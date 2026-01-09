describe('Analysis CRUD Operations', () => {
    const BACKEND_URL = 'http://localhost:8081/api'
    const API_URL = `${BACKEND_URL}/analysis`
    const SAMPLE_URL = `${BACKEND_URL}/sample`
    let testTimestamp

    before(() => {
        cy.cleanupDatabase()
        // Create a test sample for foreign key constraints
        testTimestamp = new Date().toISOString().slice(0, 23)
        cy.request('POST', SAMPLE_URL, {
            id: {
                s_id: 'TESTSAMPLE',
                s_stamp: testTimestamp
            },
            name: 'Test Sample for Analysis'
        })
    })

    after(() => {
        cy.log('Cleanup wird ausgeführt...')
        cy.cleanupDatabase()
        cy.log('Cleanup fertig!')
    })

    beforeEach(() => {
        cy.visit('/')
    })

    it('should CREATE a new analysis', () => {
        const newAnalysis = {
            s_id: 'TESTSAMPLE',
            s_stamp: testTimestamp,
            pol: 10.50,
            nat: 20.30,
            kal: 15.20,
            comment: 'Cypress Test Analysis',
            lane: 1
        }

        cy.request('POST', API_URL, newAnalysis)
            .then((response) => {
                expect(response.status).to.eq(201)
                expect(response.body).to.have.property('a_id')
                expect(response.body.comment).to.eq('Cypress Test Analysis')
            })
    })

    it('should READ all analyses', () => {
        cy.request('GET', API_URL)
            .then((response) => {
                expect(response.status).to.eq(200)
                expect(response.body).to.have.property('content')
                expect(response.body.content).to.be.an('array')
            })
    })

    it('should READ a specific analysis by ID', () => {
        cy.request('POST', API_URL, {
            s_id: 'TESTSAMPLE',
            s_stamp: testTimestamp,
            comment: 'Read Test',
            lane: 1
        }).then((createResponse) => {
            const analysisId = createResponse.body.a_id

            cy.request('GET', `${API_URL}/${analysisId}`)
                .then((response) => {
                    expect(response.status).to.eq(200)
                    expect(response.body.a_id).to.eq(analysisId)
                    expect(response.body.comment).to.eq('Read Test')
                })
        })
    })

    it('should UPDATE an existing analysis', () => {
        cy.request('POST', API_URL, {
            s_id: 'TESTSAMPLE',
            s_stamp: testTimestamp,
            comment: 'Original Comment',
            lane: 1
        }).then((createResponse) => {
            const analysisId = createResponse.body.a_id

            const updatedData = {
                s_id: 'TESTSAMPLE',
                s_stamp: testTimestamp,
                comment: 'Updated Comment',
                lane: 2,
                pol: 25.75
            }

            cy.request('PUT', `${API_URL}/${analysisId}`, updatedData)
                .then((response) => {
                    expect(response.status).to.eq(200)
                    expect(response.body.comment).to.eq('Updated Comment')
                    expect(response.body.lane).to.eq(2)
                })
        })
    })

    it('should DELETE an analysis', () => {
        cy.request('POST', API_URL, {
            s_id: 'TESTSAMPLE',
            s_stamp: testTimestamp,
            comment: 'To be deleted',
            lane: 1
        }).then((createResponse) => {
            const analysisId = createResponse.body.a_id

            cy.request('DELETE', `${API_URL}/${analysisId}`)
                .then((response) => {
                    expect(response.status).to.eq(204)
                })

            cy.request({
                method: 'GET',
                url: `${API_URL}/${analysisId}`,
                failOnStatusCode: false
            }).then((response) => {
                expect(response.status).to.eq(404)
            })
        })
    })

    it('should handle ERROR when creating analysis with a_id set', () => {
        const invalidAnalysis = {
            a_id: 999999,
            s_id: 'TESTSAMPLE',
            s_stamp: testTimestamp,
            comment: 'Should fail',
            lane: 1
        }

        cy.request({
            method: 'POST',
            url: API_URL,
            body: invalidAnalysis,
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should handle ERROR when creating analysis without s_id', () => {
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                comment: 'Missing s_id',
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should handle ERROR when creating analysis without s_stamp', () => {
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                s_id: 'TESTSAMPLE',
                comment: 'Missing s_stamp',
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should handle ERROR when creating analysis with non-existent sample', () => {
        const timestamp = new Date().toISOString().slice(0, 23)
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                s_id: 'NONEXISTENT',
                s_stamp: timestamp,
                comment: 'Should fail',
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should handle ERROR when updating non-existent analysis', () => {
        cy.request({
            method: 'PUT',
            url: `${API_URL}/999999`,
            body: {
                s_id: 'TESTSAMPLE',
                s_stamp: testTimestamp,
                comment: 'Should fail',
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(404)
        })
    })

    it('should handle ERROR when deleting non-existent analysis', () => {
        cy.request({
            method: 'DELETE',
            url: `${API_URL}/999999`,
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(404)
        })
    })

    it('should accept analysis with valid numeric values', () => {
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                s_id: 'TESTSAMPLE',
                s_stamp: testTimestamp,
                pol: 999999.99,
                nat: 888888.88,
                kal: 777777.77,
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(201)
        })
    })
})
