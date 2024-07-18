import React from 'react';
import { useSelector } from 'react-redux';

function UserItem({ user }) {
    const localUser = useSelector(state => state.user);

    return (
        <div
            className={`rounded-lg shadow-lg p-2 flex items-center ${localUser.id === user.id ? 'bg-gradient-to-r from-zinc-800 from-0% via-neutral-800 via-95% to-red-900 to-80%' : 'bg-gradient-to-r from-zinc-800 to-neutral-800 '}`}
        >
            <div className="flex-grow">
                <p
                    className={localUser.id === user.id ? "text-white font-bold" : "text-white"}
                    style={{ color: user.userColor }}
                >
                    {user.username}
                </p>
            </div>
        </div>
    );
}

export default UserItem;

