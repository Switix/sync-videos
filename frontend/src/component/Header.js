import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { clearUser, clearAccessToken, clearRefreshToken } from '../redux/slice/authSlice';
import { useDispatch, useSelector } from 'react-redux';
import { FaBars, FaTimes } from 'react-icons/fa';

function Header() {
    const user = useSelector(state => state.user);
    const registeringUser = useSelector(state => state.registeringUser);
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const [menuOpen, setMenuOpen] = useState(false);

    const toggleMenu = () => {
        setMenuOpen(!menuOpen);
    };



    const handleLogout = () => {
        dispatch(clearUser());
        dispatch(clearAccessToken());
        dispatch(clearRefreshToken());

        navigate('/');
    };

    return (
        <header className="sticky top-0 bg-stone-950 shadow-md z-50">
            <div className="mx-auto py-2 px-4 sm:px-6 lg:px-8 flex justify-between items-center">
                <Link to="/" className="text-xl sm:text-2xl font-bold">
                    <span className='text-red-600'>Sync </span>
                    <span className='text-white'>Videos</span>
                </Link>
                <div className="flex items-center space-x-4">
                    {registeringUser?.username ? (
                        <p style={{ color: registeringUser.color }} className="text-sm sm:text-base">{registeringUser.username}</p>
                    ) : user ? (
                        <>
                            <p style={{ color: user.userColor }} className="text-sm sm:text-base">{user.username}</p>
                        </>
                    ) : null}
                    <div className="sm:hidden">
                        <button onClick={toggleMenu} className="text-white focus:outline-none">
                            {menuOpen ? <FaTimes className="w-6 h-6" /> : <FaBars className="w-6 h-6" />}
                        </button>
                    </div>
                    {!menuOpen && (
                        <nav className="hidden sm:flex sm:space-x-4 sm:items-center  ">


                            {(!registeringUser?.username && !user) || user?.role === 'TEMPORARY_USER' ? (
                                <>
                                    <Link to="/login" className="px-3 py-2 text-md font-medium text-white text-center rounded-md bg-green-600 hover:bg-green-700">
                                        Log In
                                    </Link>
                                    <Link to="/register" className="px-3 py-2 text-md font-medium text-white text-center rounded-md bg-neutral-700 hover:bg-neutral-800">
                                        Sign Up
                                    </Link>
                                </>
                            ) : null}
                            {user && user?.role !== 'TEMPORARY_USER' && (
                                <Link to="/" className="block px-3 py-2 text-base text-center text-white font-medium rounded-md bg-red-600 hover:bg-red-700" onClick={handleLogout}>
                                    Logout
                                </Link>
                            )}
                        </nav>
                    )}
                </div>

            </div>

            {menuOpen && (
                <nav className="absolute top-11 left-0 w-full bg-stone-950 text-white p-4 space-y-2 z-40 sm:hidden">


                    {(!registeringUser?.username && !user) && (
                        <>
                            <div className='flex flex-col space-y-2'>
                                <Link to="/login" className="px-3 py-2 text-md font-medium text-white text-center rounded-md bg-green-600 hover:bg-green-700">
                                    Log In
                                </Link>
                                <Link to="/register" className="px-3 py-2 text-md font-medium text-white text-center rounded-md bg-neutral-700 hover:bg-neutral-800">
                                    Sign Up
                                </Link>
                            </div>

                        </>
                    )}
                    {user?.role === 'TEMPORARY_USER' && (
                        <>
                            <div className='flex flex-col space-y-2'>
                                <Link to="/login" className="px-3 py-2 text-md font-medium text-white text-center rounded-md bg-green-600 hover:bg-green-700">
                                    Log In
                                </Link>
                                <Link to="/register" className="px-3 py-2 text-md font-medium text-white text-center rounded-md bg-neutral-700 hover:bg-neutral-800">
                                    Sign Up
                                </Link>
                            </div>
                        </>
                    )}
                    {user && user?.role !== 'TEMPORARY_USER' && (
                        <Link to="/" className="block px-3 py-2 text-base text-white text-center font-medium rounded-md bg-red-600 hover:bg-red-700" onClick={handleLogout}>
                            Logout
                        </Link>
                    )}
                </nav>
            )}
        </header>
    );
}

export default Header;
