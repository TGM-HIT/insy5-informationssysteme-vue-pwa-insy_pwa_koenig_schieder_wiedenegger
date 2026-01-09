import api from './api'; // <-- GEÄNDERT: Importiere die zentrale api Instanz

class AuthService {
    login(username, password) {
        // GEÄNDERT: Nutze die zentrale api Instanz
        return api.post('/auth/login', { username, password })
            .then((response) => {
                if (response.data.token) {
                    localStorage.setItem("user", JSON.stringify(response.data));
                }
                return response.data;
            });
    }

    logout() {
        localStorage.removeItem("user");
    }

    getCurrentUser() {
        const user = localStorage.getItem("user");
        return user ? JSON.parse(user) : null;
    }

    isLoggedIn() {
        return this.getCurrentUser() !== null;
    }

    getRole() {
        const user = this.getCurrentUser();
        return user ? user.role : null;
    }
}

export default new AuthService();