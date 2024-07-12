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
                // Navigate to the created room
                navigate(`/room/${createdRoomId}`);
            })
            .catch(error => {
                console.error('There was an error creating the room!', error);
            });
    };

    return (
        <div>
            <button onClick={createRoom}>Create Room</button>
        </div>
    );
}

export default LandingPage;
