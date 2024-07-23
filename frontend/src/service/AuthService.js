import axios from 'axios';
import { store } from '../redux/store';
import { decodeToken } from 'react-jwt';
import { setUser, clearUser, setAccessToken, clearAccessToken, setRefreshToken, clearRefreshToken } from '../redux/slice/authSlice';


const API_URL = 'http://188.47.81.23:8080';

class AuthService {

    static async saveTokens(tokens) {
        store.dispatch(setAccessToken(tokens.accessToken));
        store.dispatch(setRefreshToken(tokens.refreshToken));
        const claims = decodeToken(tokens.accessToken);
        axios.get(`http://188.47.81.23:8080/users/${claims.userId}`, {
            headers: {
                Authorization: `Bearer ${tokens.accessToken}`,
            },
        })
            .then(response => {
                store.dispatch(setUser(response.data));
            });
    }

    static async login(username, password) {
        try {
            const response = await axios.post(`${API_URL}/auth/login`, { username, password });
            this.saveTokens(response.data);
        } catch (error) {
            console.error('Error logging in:', error);
            throw error;
        }
    }

    static async register(username, password, color) {
        try {
            const response = await axios.post(`${API_URL}/auth/register`, { username, password, color });
            this.saveTokens(response.data);
        } catch (error) {
            console.error('Error signing on:', error);
            throw error;
        }
    }

    static async refreshToken() {
        const refreshToken = store.getState().refreshToken;

        return axios.post(`${API_URL}/auth/refresh`, { refreshToken })
            .then(response => {
                this.saveTokens(response.data);
                return response.data;
            })
            .catch(async error => {
                const response = error.response;

                if (response && response.status === 401) {
                    if (response.data.message === 'EXPIRED_REFRESH_TOKEN') {
                        const user = this.getCurrentUser();

                        store.dispatch(clearUser());
                        store.dispatch(clearAccessToken());
                        store.dispatch(clearRefreshToken());

                        if (user && user.role === "TEMPORARY_USER") {
                            await this.register(null, null, null);
                        } else if (user && user.role === "USER") {
                            return Promise.reject(error);
                        }
                    }
                }
                return;
            });
    }

    static logout() {
        store.dispatch(clearUser());
        store.dispatch(clearAccessToken());
        store.dispatch(clearRefreshToken());
    }

    static getCurrentUser() {
        return store.getState().user;
    }

    static getAccessToken() {
        return store.getState().accessToken;
    }
}

export default AuthService;
