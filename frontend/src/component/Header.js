import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { clearUser,clearAccessToken,clearRefreshToken } from '../redux/slice/authSlice';
import { useDispatch, useSelector } from 'react-redux';

function Header() {
    const user = useSelector(state => state.user);
    const registeringUser = useSelector(state => state.registeringUser);
    const dispatch = useDispatch();
    const navigate = useNavigate();

    function LoginRegisterLinks() {
        return (
            <>
                <Link to="/login" className="px-3 py-2 text-md font-medium text-white text-center rounded-md bg-green-600 hover:bg-green-700">
                    Log In
                </Link>
                <Link to="/register" className="px-3 py-2 text-md font-medium text-white text-center rounded-md bg-neutral-700 hover:bg-neutral-800">
                    Sign Up
                </Link>
            </>
        );
    }

    const handleLogout = () => {
        dispatch(clearUser());
        dispatch(clearAccessToken());
        dispatch(clearRefreshToken());
        navigate('/');
    };

    return (
        <header className="sticky top-0 bg-stone-950 shadow-md z-50">
            <div className="mx-auto py-2 px-4 sm:px-6 lg:px-8 flex justify-between items-center">
                <Link to="/" className="text-2xl font-bold">
                    <span className='text-red-600'>Sync </span>
                    <span className='text-white'>Videos</span>
                </Link>
                <nav className="flex space-x-4 items-center">
                    {registeringUser.username ? (
                        // Display registeringUser.username if available
                        <>
                            <p style={{ color: registeringUser.color }}> {registeringUser.username}</p>
                            <LoginRegisterLinks />
                        </>
                    ) : user ? (
                        // Display user.username if user is logged in
                        <>
                            <p style={{ color: user.userColor }}> {user.username}</p>
                            {user.role === 'TEMPORARY_USER' ? (
                                <LoginRegisterLinks />
                            ) : (
                                <Link to="/" className="px-3 py-2 text-md font-medium text-white text-center rounded-md bg-red-600 hover:bg-red-700" onClick={handleLogout}>
                                    Logout
                                </Link>
                            )}
                        </>
                    ) : (
                        // Display Log In and Sign Up links if user is not logged in
                        <LoginRegisterLinks />
                    )}
                </nav>
            </div>
        </header>
    );
}

export default Header;
