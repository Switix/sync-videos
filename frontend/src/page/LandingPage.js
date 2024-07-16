import React from 'react';
import api from '../service/api';
import { useNavigate } from 'react-router-dom';

function LandingPage() {
    const navigate = useNavigate();

    const createRoom = () => {
        api.post('http://localhost:8080/rooms/create', {})
            .then(response => {
                console.log('Room created:', response.data);
                const createdRoomId = response.data.id;
                navigate(`/room/${createdRoomId}`);
            })
            .catch(error => {
                console.error('There was an error creating the room!', error);
            });
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-neutral-900">
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
