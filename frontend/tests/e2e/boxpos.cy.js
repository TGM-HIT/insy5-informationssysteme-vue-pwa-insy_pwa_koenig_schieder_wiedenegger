describe('BoxPos CRUD Operations', () => {
    const BACKEND_URL = 'http://localhost:8081/api'
    const API_URL = `${BACKEND_URL}/boxpos`
    const BOX_API_URL = `${BACKEND_URL}/box`
    const SAMPLE_API_URL = `${BACKEND_URL}/sample`

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

    it('should CREATE a new box position without sample reference', () => {
        // Create box first
        cy.request('POST', BOX_API_URL, {
            b_id: 'TEST',
            name: 'Test Box for Position',
            num_max: 50
        }).then(() => {
            const newBoxPos = {
                b_id: 'TEST',
                bpos_id: 1
                // s_id and s_stamp are optional, so we omit them
            }

            cy.request('POST', API_URL, newBoxPos)
                .then((response) => {
                    expect(response.status).to.eq(201)
                    expect(response.body.b_id).to.eq('TEST')
                    expect(response.body.bpos_id).to.eq(1)
                })
        })
    })

    it('should CREATE a new box position with sample reference', () => {
        const sampleStamp = new Date().toISOString()

        // Create box
        cy.request('POST', BOX_API_URL, {
            b_id: 'TSMP',
            name: 'Test Box with Sample',
            num_max: 50
        }).then(() => {
            // Create sample first - with nested id object
            cy.request('POST', SAMPLE_API_URL, {
                id: {
                    s_id: 'SAMPLE_001',
                    s_stamp: sampleStamp
                },
                name: 'Test Sample'
            }).then(() => {
                // Create box position with sample reference
                const newBoxPos = {
                    b_id: 'TSMP',
                    bpos_id: 1,
                    s_id: 'SAMPLE_001',
                    s_stamp: sampleStamp
                }

                cy.request('POST', API_URL, newBoxPos)
                    .then((response) => {
                        expect(response.status).to.eq(201)
                        expect(response.body.b_id).to.eq('TSMP')
                        expect(response.body.bpos_id).to.eq(1)
                        expect(response.body.s_id).to.eq('SAMPLE_001')
                    })
            })
        })
    })

    it('should READ all box positions', () => {
        cy.request('GET', API_URL)
            .then((response) => {
                expect(response.status).to.eq(200)
                expect(response.body.content).to.be.an('array')
            })
    })

    it('should READ a specific box position by composite ID', () => {
        cy.request('POST', BOX_API_URL, {
            b_id: 'READ',
            name: 'Read Box',
            num_max: 30
        }).then(() => {
            cy.request('POST', API_URL, {
                b_id: 'READ',
                bpos_id: 5
                // No sample reference needed
            }).then(() => {
                cy.request('GET', `${API_URL}/READ/5`)
                    .then((response) => {
                        expect(response.status).to.eq(200)
                        expect(response.body.b_id).to.eq('READ')
                        expect(response.body.bpos_id).to.eq(5)
                    })
            })
        })
    })

    it('should UPDATE an existing box position', () => {
        cy.request('POST', BOX_API_URL, {
            b_id: 'UPDT',
            name: 'Update Box',
            num_max: 40
        }).then(() => {
            cy.request('POST', API_URL, {
                b_id: 'UPDT',
                bpos_id: 3
            }).then(() => {
                const updatedData = {
                    b_id: 'UPDT',
                    bpos_id: 3
                    // Can add other fields to update if needed
                }

                cy.request('PUT', `${API_URL}/UPDT/3`, updatedData)
                    .then((response) => {
                        expect(response.status).to.eq(200)
                        expect(response.body.b_id).to.eq('UPDT')
                        expect(response.body.bpos_id).to.eq(3)
                    })
            })
        })
    })



    it('should DELETE a box position', () => {
        cy.request('POST', BOX_API_URL, {
            b_id: 'DELE',
            name: 'Delete Box',
            num_max: 20
        }).then(() => {
            cy.request('POST', API_URL, {
                b_id: 'DELE',
                bpos_id: 7
            }).then(() => {
                cy.request('DELETE', `${API_URL}/DELE/7`)
                    .then((response) => {
                        expect(response.status).to.eq(200)
                    })

                cy.request({
                    method: 'GET',
                    url: `${API_URL}/DELE/7`,
                    failOnStatusCode: false
                }).then((response) => {
                    expect(response.status).to.eq(404)
                })
            })
        })
    })

    it('should handle ERROR when getting non-existent box position', () => {
        cy.request({
            method: 'GET',
            url: `${API_URL}/NONE/999`,
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(404)
        })
    })

    it('should handle ERROR when updating non-existent box position', () => {
        cy.request({
            method: 'PUT',
            url: `${API_URL}/NONE/999`,
            body: {
                b_id: 'NONE',
                bpos_id: 999
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(404)
        })
    })

    it('should handle ERROR when deleting non-existent box position', () => {
        cy.request({
            method: 'DELETE',
            url: `${API_URL}/NONE/999`,
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(404)
        })
    })

    it('should handle ERROR when creating position with missing composite ID', () => {
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {},
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
        })
    })

    it('should handle ERROR when creating position for non-existent box', () => {
        cy.request({
            method: 'POST',
            url: API_URL,
            body: {
                b_id: 'FAKE',
                bpos_id: 1
            },
            failOnStatusCode: false
        }).then((response) => {
            expect(response.status).to.eq(400)
            expect(response.body).to.include('Foreign key violation: Box')
        })
    })

    it('should handle ERROR when creating position with non-existent sample', () => {
        cy.request('POST', BOX_API_URL, {
            b_id: 'ERRS',
            name: 'Error Sample Box',
            num_max: 30
        }).then(() => {
            cy.request({
                method: 'POST',
                url: API_URL,
                body: {
                    b_id: 'ERRS',
                    bpos_id: 1,
                    s_id: 'FAKE_SAMPLE',
                    s_stamp: new Date().toISOString()
                },
                failOnStatusCode: false
            }).then((response) => {
                expect(response.status).to.eq(400)
                expect(response.body).to.include('Foreign key violation: Sample')
            })
        })
    })

    it('should handle CONFLICT when creating duplicate box position', () => {
        cy.request('POST', BOX_API_URL, {
            b_id: 'DUPL',
            name: 'Duplicate Test Box',
            num_max: 30
        }).then(() => {
            const boxPos = {
                b_id: 'DUPL',
                bpos_id: 1
            }

            cy.request('POST', API_URL, boxPos).then(() => {
                cy.request({
                    method: 'POST',
                    url: API_URL,
                    body: boxPos,
                    failOnStatusCode: false
                }).then((response) => {
                    expect(response.status).to.eq(409)
                })
            })
        })
    })
})
