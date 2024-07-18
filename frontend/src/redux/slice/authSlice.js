import { createSlice } from '@reduxjs/toolkit';

const initialState = {
    user: null,
    accessToken: null,
    refreshToken: null,
    registeringUser: {
        username: '',
        color: '#FFFFFF',
    },
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setUser: (state, action) => {
            state.user = action.payload;
        },
        clearUser: (state) => {
            state.user = null;
        },
        setAccessToken: (state, action) => {
            state.accessToken = action.payload;
        },
        clearAccessToken: (state) => {
            state.accessToken = null;
        },
        setRefreshToken: (state, action) => {
            state.refreshToken = action.payload;
        },
        clearRefreshToken: (state) => {
            state.refreshToken = null;
        },
        setRegisteringUser: (state, action) => {
            state.registeringUser = action.payload;
        },
        clearRegisteringUser: (state) => {
            state.registeringUser = {
                username: '',
                color: '#FFFFFF',
            };
        },
    },
});

export const {
    setUser,
    clearUser,
    setAccessToken,
    clearAccessToken,
    setRefreshToken,
    clearRefreshToken,
    setRegisteringUser,
    clearRegisteringUser
} = authSlice.actions;
export default authSlice.reducer;
