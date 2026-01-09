import axios from 'axios'
import AuthService from './AuthService'

const api = axios.create({
    baseURL: '/api', // <-- GEÄNDERT: Relative URL
    headers: {
        'Content-Type': 'application/json'
    }
})

// Request Interceptor: Token anhängen
api.interceptors.request.use(
    config => {
        const user = AuthService.getCurrentUser();
        if (user && user.token) {
            config.headers['Authorization'] = 'Bearer ' + user.token;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// Response Interceptor - NUR loggen, NICHT ausloggen
api.interceptors.response.use(
    response => response,
    error => {
        console.log('API Error:', error.response?.status, error.response?.data);
        // KEIN logout, KEIN reload - einfach Fehler weitergeben
        return Promise.reject(error);
    }
);

export default api;