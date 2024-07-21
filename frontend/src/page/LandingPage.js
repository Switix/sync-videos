import React from 'react';
import api from '../service/api';
import { useNavigate } from 'react-router-dom';

function LandingPage() {
    const navigate = useNavigate();

    const createRoom = () => {
        api.post('http://26.134.154.97:8080/rooms/create', {})
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
        <div className="h-[calc(100vh-3.5rem)] flex flex-col items-center justify-center bg-neutral-900">
            <button
                onClick={createRoom}
                className="px-14 py-7 bg-red-800 hover:bg-red-900 text-neutral-50 font-semibold rounded-full shadow-md focus:outline-none text-5xl"
            >
                Create Room
            </button>
        </div>
    );
}

export default LandingPage;
