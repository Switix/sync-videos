import React, { useState, useEffect } from 'react';
import { setRegisteringUser, clearRegisteringUser } from '../redux/slice/authSlice';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import authService from '../service/AuthService';

function RegisterPage() {
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const registeringUser = useSelector(state => state.registeringUser);

    const dispatch = useDispatch();
    const navigate = useNavigate();

    useEffect(() => {
        return () => {
            dispatch(clearRegisteringUser());
        };
    }, [dispatch]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Basic validation
        if (!registeringUser.username || !password) {
            setError('Please fill in all fields');
            return;
        }
        setError('');
        dispatch(clearRegisteringUser);

        try {
            await authService.register(registeringUser.username, password, registeringUser.color);
            navigate('/');
        } catch (error) {
            setError(error.response.data.message);
        }

    };

    const onUsernameTyping = (e) => {
        const username = e.target.value;
        dispatch(setRegisteringUser({ ...registeringUser, username }));
    };

    const onColorPicked = (e) => {
        const color = e.target.value;
        dispatch(setRegisteringUser({ ...registeringUser, color }));
    };

    return (
        <div className="h-full flex items-center justify-center ">
            <div className="bg-neutral-800 p-8 rounded-lg shadow-lg w-full max-w-md">
                <h2 className="text-2xl font-bold text-white mb-6">Register</h2>
                {error && <p className="text-red-500 mb-4">{error}</p>}
                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label htmlFor="username" className="block text-sm font-medium text-gray-400 mb-2">Username</label>
                        <input
                            type="text"
                            id="username"
                            value={registeringUser.username}
                            onChange={onUsernameTyping}
                            className="w-full p-2 rounded bg-neutral-900 text-white border border-neutral-700 focus:outline-none focus:border-green-500"
                        />
                    </div>
                    <div className="mb-4">
                        <label htmlFor="password" className="block text-sm font-medium text-gray-400 mb-2">Password</label>
                        <input
                            type="password"
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full p-2 rounded bg-neutral-900 text-white border border-neutral-700 focus:outline-none focus:border-green-500"
                        />
                    </div>
                    <div className="mb-6">
                        <label htmlFor="color" className="block text-sm font-medium text-gray-400 mb-2">Pick a Color</label>
                        <input
                            type="color"
                            id="color"
                            value={registeringUser.color}
                            onChange={onColorPicked}
                            className="w-full h-10 p-1 rounded bg-neutral-900 border border-neutral-700 focus:outline-none focus:border-green-500"
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full p-2 rounded bg-green-600 hover:bg-green-700 text-white font-bold"
                    >
                        Register
                    </button>
                </form>
            </div>
        </div>
    );
}

export default RegisterPage;
