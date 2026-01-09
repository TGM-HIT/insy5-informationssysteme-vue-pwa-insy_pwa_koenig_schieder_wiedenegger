import api from './api'

class AnalysisService {
    getAll() {
        return api.get('/analysis')
    }

    getById(id) {
        return api.get(`/analysis/${id}`)
    }

    create(data) {
        return api.post('/analysis', data)
    }

    update(id, data) {
        return api.put(`/analysis/${id}`, data)
    }

    delete(id) {
        return api.delete(`/analysis/${id}`)
    }
}

export default new AnalysisService()
