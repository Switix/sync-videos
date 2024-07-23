import React from 'react';
import api from '../service/api';
import { useNavigate } from 'react-router-dom';

function LandingPage() {
    const navigate = useNavigate();

    const createRoom = () => {
        api.post('http://188.47.81.23:8080/rooms/create', {})
            .then(response => {
                console.log('Room created:', response.data);
                const createdRoomId = response.data.id;
                navigate(`/room/${createdRoomId}`);
            })
            .catch(() => {
                navigate('/login')
            });
    };

    return (
        <div className="h-full flex flex-col items-center justify-center bg-neutral-900">
            <button
                onClick={createRoom}
                className=" max-w-xs sm:max-w-md md:max-w-lg lg:max-w-xl px-14  py-3 bg-red-800 hover:bg-red-900 text-neutral-50 font-semibold rounded-full shadow-md focus:outline-none text-2xl sm:text-3xl md:text-4xl "
            >
                Create Room
            </button>
        </div>
    );
}

export default LandingPage;
