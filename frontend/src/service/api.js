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
            await authService.register(null, null, null);
        }

        token = authService.getAccessToken();
        config.headers.Authorization = `Bearer ${token}`;

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    response => response,
    async error => {
        if (error.response) {
            console.log('Error Response:', error.response);

            if (error.response.status === 401) {
                // Attempt to refresh the token
                try {
                    await authService.refreshToken();

                    const originalRequest = error.config;
                    return api(originalRequest); // Retry the request
                } catch (refreshError) {
                    return Promise.reject(refreshError);
                }
            } else if (error.response.status === 403) {
                console.error('Forbidden access:', error.response);
            } else if (error.response.status === 500) {
                console.error('Internal server error:', error.response);
            } else {
                console.error('Unexpected error:', error.response);
            }
        } else if (error.request) {
            console.error('Error Request:', error.request);
        } else {
            console.error('Error Message:', error.message);
        }


        return Promise.reject(error);
    }
);

export default api;
