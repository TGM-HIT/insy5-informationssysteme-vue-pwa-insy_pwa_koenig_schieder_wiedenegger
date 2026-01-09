describe('Sample CRUD Operations', () => {
    const BACKEND_URL = 'http://localhost:8081/api'
    const API_URL = `${BACKEND_URL}/sample`

    before(() => {
        cy.cleanupDatabase()
    })

    after(() => {
        cy.log('Cleanup wird ausgeführt...')
        cy.cleanupDatabase()
        cy.log('Cleanup fertig!')
    })

    beforeEach(() => {
        cy.visit('/')
    })

    it('should CREATE a new sample', () => {
        const timestamp = new Date().toISOString()
        const newSample = {
            id: {
                s_id: 'TEST_SAMPLE_1',
                s_stamp: timestamp
            },
            name: 'Cypress Test Sample',
            weight_net: 150.5,
            weight_bru: 200.0,
            weight_tar: 49.5,
            quantity: 5,
            distance: 10.2,
            lane: 1,
            comment: 'Created by Cypress'
        }

        cy.request('POST', API_URL, newSample)
            .then((response) => {
                expect(response.status).to.eq(200)
                expect(response.body.id.s_id).to.eq('TEST_SAMPLE_1')
                expect(response.body.name).to.eq('Cypress Test Sample')
                expect(response.body.weight_net).to.eq(150.5)
            })
    })


    it('should UPDATE an existing sample', () => {
        const timestamp = new Date().toISOString()
        cy.request('POST', API_URL, {
            id: {
                s_id: 'UPDATE_TST_01',
                s_stamp: timestamp
            },
            name: 'Original Name',
            weight_net: 100.0,
            lane: 1
        }).then((createResponse) => {
            const sId = createResponse.body.id.s_id
            const sStamp = createResponse.body.id.s_stamp

            const updatedData = {
                name: 'Updated Name',
                weight_net: 250.5,
                weight_bru: 300.0,
                comment: 'Updated via Cypress',
                lane: 2
            }

            cy.request('PUT', `${API_URL}/${sId}/${sStamp}`, updatedData)
                .then((response) => {
                    expect(response.status).to.eq(200)
                    expect(response.body.name).to.eq('Updated Name')
                    expect(response.body.weight_net).to.eq(250.5)
                    expect(response.body.lane).to.eq(2)
                })
        })
    })

    it('should DELETE a sample', () => {
        const timestamp = new Date().toISOString()
        cy.request('POST', API_URL, {
            id: {
                s_id: 'DELETE_TST_01',
                s_stamp: timestamp
            },
            name: 'To be deleted',
            lane: 1
        }).then((createResponse) => {
            const sId = createResponse.body.id.s_id
            const sStamp = createResponse.body.id.s_stamp

            cy.request('DELETE', `${API_URL}/${sId}/${sStamp}`)
                .then((response) => {
                    expect(response.status).to.eq(200)
                })

            cy.request({
                method: 'GET',
                url: `${API_URL}/${sId}/${sStamp}`,
                failOnStatusCode: false
            }).then((response) => {
                expect(response.status).to.eq(404)
            })
        })
    })

    it('should handle ERROR when getting non-existent sample', () => {
        cy.request({
            method: 'GET',
            url: `${API_URL}/NONEXISTENT/2026-01-01T00:00:00`,
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(404)
        })
    })

    it('should handle ERROR when updating non-existent sample', () => {
        cy.request({
            method: 'PUT',
            url: `${API_URL}/NONEXISTENT/2026-01-01T00:00:00`,
            body: {
                name: 'Should fail'
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(404)
        })
    })

    it('should handle ERROR when deleting non-existent sample', () => {
        cy.request({
            method: 'DELETE',
            url: `${API_URL}/NONEXISTENT/2026-01-01T00:00:00`,
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(404)
        })
    })

    it('should handle ERROR with invalid timestamp format', () => {
        cy.request({
            method: 'GET',
            url: `${API_URL}/TEST001/invalid-timestamp`,
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.be.oneOf([400, 500])
        })
    })

    it('should handle ERROR when creating sample with missing ID', () => {
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                name: 'Invalid Sample',
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should handle ERROR when creating sample with missing s_id', () => {
        const timestamp = new Date().toISOString()
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                id: {
                    s_stamp: timestamp
                },
                name: 'Missing s_id',
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should handle ERROR when creating sample with missing s_stamp', () => {
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                id: {
                    s_id: 'MISSING_STAMP'
                },
                name: 'Missing s_stamp',
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should handle ERROR when creating sample with s_id longer than 13 characters', () => {
        const timestamp = new Date().toISOString()
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                id: {
                    s_id: 'THIS_IS_TOO_LONG_ID_FOR_VALIDATION',
                    s_stamp: timestamp
                },
                name: 'Too Long ID',
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should handle ERROR when creating sample with invalid timestamp format', () => {
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                id: {
                    s_id: 'ERR_TSTAMP_01',
                    s_stamp: 'not-a-valid-timestamp'
                },
                name: 'Invalid timestamp',
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should handle ERROR when creating duplicate sample', () => {
        const timestamp = new Date().toISOString()
        const sampleData = {
            id: {
                s_id: 'DUPLICATE_001',
                s_stamp: timestamp
            },
            name: 'Duplicate Test'
        }

        cy.request('POST', API_URL, sampleData)
            .then(() => {
                cy.request({
                    method: 'POST',
                    url: API_URL,
                    body: sampleData,
                    failOnStatusCode: false
                }).then((response) => {
                    expect(response.status).to.eq(409)
                })
            })
    })

    it('should accept sample with negative weight values', () => {
        const timestamp = new Date().toISOString()
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                id: {
                    s_id: 'NEG_WEIGHT_01',
                    s_stamp: timestamp
                },
                name: 'Negative Weights',
                weight_net: -100.5,
                weight_bru: -200.0,
                lane: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(200)
        })
    })

    it('should handle ERROR when creating sample with invalid numeric types', () => {
        const timestamp = new Date().toISOString()
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                id: {
                    s_id: 'ERR_TYPES_01',
                    s_stamp: timestamp
                },
                name: 'Invalid Types',
                weight_net: 'not-a-number',
                quantity: 'invalid'
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should accept sample with minimal required fields', () => {
        const timestamp = new Date().toISOString()
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                id: {
                    s_id: 'MINIMAL_001',
                    s_stamp: timestamp
                }
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(200)
        })
    })

    it('should accept sample with s_id of exactly 13 characters', () => {
        const timestamp = new Date().toISOString()
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                id: {
                    s_id: '1234567890123',
                    s_stamp: timestamp
                },
                name: 'Exactly 13 chars'
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(200)
        })
    })
})
