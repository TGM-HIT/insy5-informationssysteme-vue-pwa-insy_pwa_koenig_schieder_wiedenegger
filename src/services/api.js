import axios from 'axios'

const api = axios.create({
    baseURL: 'localhost:8081/api/',
    headers: {
        'Content-Type': 'application/json'
    }
})

export default api
