import { createSlice } from '@reduxjs/toolkit';

const initialState = {
    user: null,
    accessToken: null,
    refreshToken: null
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
    },
});

export const { setUser, clearUser, setAccessToken, clearAccessToken, setRefreshToken, clearRefreshToken } = authSlice.actions;
export default authSlice.reducer;
