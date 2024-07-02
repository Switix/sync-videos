// RoomPage.js
import React, { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

function RoomPage() {
    const { roomId } = useParams();
    const [notifications, setNotifications] = useState([]);
    const stompClientRef = useRef(null);

    useEffect(() => {
        const sendJoinNotification = () => {
            if (stompClientRef.current) {
                stompClientRef.current.send(`/app/joinRoom/${roomId}`, {}, JSON.stringify({ username: 'tempUser' }));
            }
        };
    
        const subscribeToRoom = (roomId) => {
            if (stompClientRef.current) {
                stompClientRef.current.subscribe(`/topic/room/${roomId}`, (message) => {
                    const notification = JSON.parse(message.body);
                    setNotifications((prevNotifications) => [...prevNotifications, notification]);
                });
            }
        };
        // Connect to WebSocket server
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);
    
        client.connect({}, (frame) => {
            stompClientRef.current = client;
            console.log('Connected: ' + frame);
            // Subscribe to room notifications
            subscribeToRoom(roomId);
            // Notify server that user has joined the room
            sendJoinNotification();
        }, (error) => {
            console.error('Error connecting to WebSocket', error);
        });
    
        return () => {
            // Disconnect from WebSocket server on component unmount
            if (stompClientRef.current) {
                stompClientRef.current.disconnect();
            }
        };
    }, [roomId]);



    return (
        <div>
            <h2>Room: {roomId}</h2>
            <div>
                <h3>Notifications</h3>
                <ul>
                    {notifications.map((notification, index) => (
                        <li key={index}>{notification.message}</li>
                    ))}
                </ul>
            </div>
        </div>
    );
}

export default RoomPage;
