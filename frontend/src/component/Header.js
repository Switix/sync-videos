import React from 'react';
import { Link } from 'react-router-dom';

function Header() {
    return (
        <header className="fixed top-0 left-0 right-0 bg-stone-950 shadow-md z-50">
            <div className=" mx-auto py-2 px-4 sm:px-6 lg:px-8 flex justify-between items-center">
                <Link to="/" className="text-2xl font-bold">
                    <span className='text-red-600'>Sync </span>
                    <span className='text-white'>Videos</span>
                </Link>
                <nav className="flex space-x-4">
                    <Link to="/" className="px-3 py-2 text-md font-medium text-white text-center rounded-md bg-green-600 hover:bg-green-700">
                        Log In
                    </Link>
                    <Link to="/" className="px-3 py-2 text-md font-medium text-white  text-center rounded-md bg-neutral-700 hover:bg-neutral-800">
                        Sign Up
                    </Link>

                </nav>
            </div>
        </header>
    );
}

export default Header;
