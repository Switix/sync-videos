import axios from 'axios';
import { store } from '../redux/store';
import { decodeToken } from 'react-jwt';
import { setUser, clearUser, setAccessToken, clearAccessToken, setRefreshToken, clearRefreshToken } from '../redux/slice/authSlice';

const API_URL = 'http://localhost:8080';

const saveTokens = async (tokens) => {
    store.dispatch(setAccessToken(tokens.accessToken))
    store.dispatch(setRefreshToken(tokens.refreshToken))
    const claims = decodeToken(tokens.accessToken);
    axios.get(`http://localhost:8080/users/${claims.userId}`, {
        headers: {
            Authorization: `${tokens.accessToken}`,
        },
    })
        .then(response => {
            store.dispatch(setUser(response.data))
        })


}

const AuthService = {
    login: async (username, password) => {
        try {
            const response = await axios.post(`${API_URL}/auth/login`, { username, password });
            saveTokens(response.data)
        } catch (error) {
            console.error('Error logging in:', error);
            throw error;
        }
    },

    register: async (username, password, color) => {
        try {
            const response = await axios.post(`${API_URL}/auth/register`, { username, password, color });
            saveTokens(response.data)
        } catch (error) {
            console.error('Error signing on:', error);
            throw error;
        }
    },

    logout: async () => {
        store.dispatch(clearUser());
        store.dispatch(clearAccessToken());
        store.dispatch(clearRefreshToken());
    },



    getCurrentUser: () => {
        return store.getState().user;
    },
    getAccessToken: () => {
        return store.getState().accessToken;
    },
};

export default AuthService;
