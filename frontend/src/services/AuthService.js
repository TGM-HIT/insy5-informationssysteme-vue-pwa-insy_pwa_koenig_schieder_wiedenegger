import axios from "axios";

const API_URL = "http://localhost:8081/api/auth";

class AuthService {
    login(username, password) {
        return axios
            .post(`${API_URL}/login`, { username, password })
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