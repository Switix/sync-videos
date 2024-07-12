import axios from 'axios';
import authService from './AuthService';


const API_URL = 'http://localhost:8080';

const api = axios.create({
    baseURL: API_URL,
});

api.interceptors.request.use(
    async (config) => {

        let token = authService.getAccessToken();

        // Set the Authorization header if a token is available
        if (!token) {
            await authService.register(null, null);
        }

        token = authService.getAccessToken();
        config.headers.Authorization = `${token}`;

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;
