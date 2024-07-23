import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../service/AuthService';

function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Basic validation
        if (!username || !password) {
            setError('Please fill in all fields');
            return;
        }
        setError('');

        try {
            await authService.login(username, password);
            navigate('/');
        } catch (error) {
            setError(error.response.data.message)
        }

    };

    return (
        <div className=" px-8 sm:px-0 h-full flex items-center justify-center">
            <div className="bg-neutral-800 p-8 rounded-lg shadow-lg w-full max-w-md">
                <h2 className="text-2xl font-bold text-white mb-6">Login</h2>
                {error && <p className="text-red-500 mb-4">{error}</p>}
                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label htmlFor="email" className="block text-md font-medium text-gray-400 mb-2">Username</label>
                        <input
                            type="text"
                            id="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            className="w-full p-2 rounded bg-neutral-900 text-white border border-neutral-700 focus:outline-none focus:border-green-500"
                        />
                    </div>
                    <div className="mb-6">
                        <label htmlFor="password" className="block text-md font-medium text-gray-400 mb-2">Password</label>
                        <input
                            type="password"
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full p-2 rounded bg-neutral-900 text-white border border-neutral-700 focus:outline-none focus:border-green-500"
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full p-2 rounded bg-green-600 hover:bg-green-700 text-white font-bold"
                    >
                        Login
                    </button>
                </form>
            </div>
        </div>
    );
}

export default LoginPage;
