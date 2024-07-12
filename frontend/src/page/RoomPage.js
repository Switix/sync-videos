import React, { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { NotificationTypes } from '../constants/RoomNotificationTypes';
import ReactPlayer from 'react-player';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import api from '../service/api';
import { useSelector } from 'react-redux';

function RoomPage() {
    const { roomId } = useParams();
    const navigate = useNavigate();
    const [notifications, setNotifications] = useState([]);

    const stompClientRef = useRef(null);

    const playerRef = useRef(null);
    const [videoUrl, setVideoUrl] = useState('');
    const [queue, setQueue] = useState([]);
    const [currentVideo, setCurrentVideo] = useState(null);

    const [isPlaying, setIsPlaying] = useState(false);

    const user =  useSelector(state => state.user);

    const SEEK_TRESHOLD = 0.5 // in seconds

    useEffect( () => {
       
        const checkRoomExists = async (roomId) => {
            try {
                const response = await api.get(`http://localhost:8080/rooms/exists/${roomId}`)
                return response.data;
            } catch (error) {
                console.error('Error checking room existence', error);
                return false;
            }
        };
        const sendJoinNotification = () => {
            if (stompClientRef.current) {
                stompClientRef.current.send(`/app/room/${roomId}/join`, {}, JSON.stringify({ username: user.username }));
            }
        };

        const subscribeToRoom = (roomId) => {
            if (stompClientRef.current) {
                stompClientRef.current.subscribe(`/topic/room/${roomId}`, (message) => {
                    const notification = JSON.parse(message.body);

                    switch (notification.type) {
                        case NotificationTypes.VIDEO_ADDED:
                            const videoUrl = notification.message;
                            setQueue((prevQueue) => [...prevQueue, videoUrl]);
                            break;
                        case NotificationTypes.USER_JOINED:
                            break;
                        case NotificationTypes.VIDEO_PLAY:
                            setIsPlaying(true);
                            if (Math.abs(playerRef.current.getCurrentTime() - notification.message) > SEEK_TRESHOLD) {
                                playerRef.current.seekTo(notification.message, 'seconds', true)
                            }
                            break;
                        case NotificationTypes.VIDEO_PAUSE:
                            setIsPlaying(false);
                            break;
                        case NotificationTypes.SYNC_CHECK:
                            if (Math.abs(playerRef.current.getCurrentTime() - notification.message) > SEEK_TRESHOLD) {
                                playerRef.current.seekTo(notification.message, 'seconds', isPlaying)
                            }
                            break;
                        default:
                            console.warn('Unhandled notification type:', notification.type);
                            break;
                    }

                    setNotifications((prevNotifications) => [...prevNotifications, notification]);
                });
            }
        };

        const fetchInitialQueue = (roomId) => {
            api.get(`http://localhost:8080/rooms/${roomId}/queue`)
                .then(response => {
                    setQueue(response.data);
                })
                .catch(error => {
                    console.error('There was an error fetching the queue!', error);
                });
        }

        checkRoomExists(roomId)
            .then(exists => {
                if (exists ) {
                    if(user == null) return;
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
                        // Get the current room queue
                        fetchInitialQueue(roomId);
                    }, (error) => {
                        console.error('Error connecting to WebSocket', error);
                    });
                } else {
                    navigate('/404');
                }
            });

        return () => {
            // Disconnect from WebSocket server on component unmount
            if (stompClientRef.current) {
                stompClientRef.current.disconnect();
            }
        };
        // eslint-disable-next-line
    }, [user]);



    const addVideoToQueue = () => {
        api.post(`http://localhost:8080/rooms/addVideo?roomId=${roomId}`, videoUrl, {
            headers: {
                'Content-Type': 'text/plain'
            }
        })
            .then(response => {
                setVideoUrl('');
            })
            .catch(error => {
                console.error('There was an error adding the video!', error);
            });
    };


    useEffect(() => {
        if (currentVideo != null)
            return;
        playNextVideo();
        // eslint-disable-next-line
    }, [queue]);

    const playNextVideo = () => {
        const nextVideo = queue.shift();
        setCurrentVideo(nextVideo || null);

    };

    const handlePlay = () => {
        console.log('play')
        if (stompClientRef.current) {
            stompClientRef.current.send(`/app/room/${roomId}/play`, {}, JSON.stringify({ username: user.username, currentSeek: playerRef.current.getCurrentTime() }));
        }

    }
    const handlePause = () => {
        console.log('pause')
        if (stompClientRef.current) {
            stompClientRef.current.send(`/app/room/${roomId}/pause`, {}, JSON.stringify({ username: user.username, currentSeek: playerRef.current.getCurrentTime() }));
        }

    }
 
    return (
        <div>
             {user ? <p>Username: {user.username}</p> : <p>No user logged in</p>}
            <h2>Room: {roomId}</h2>
            <input
                type="text"
                value={videoUrl}
                onChange={(e) => setVideoUrl(e.target.value)}
                placeholder="Enter YouTube URL"
            />
            <button onClick={addVideoToQueue}>Add to Queue</button>
            <ReactPlayer
                ref={playerRef}
                url={currentVideo}
                controls
                playing={isPlaying}
                width="100%"
                height="100%"
                onEnded={playNextVideo}
                onPlay={handlePlay}
                onPause={handlePause}
            />
            <div>
                <h3>Current Video</h3>
                {currentVideo}
            </div>
            <div>
                <h3>Queue</h3>
                <ul>
                    {queue.map((video, index) => (
                        <li key={index}>{video}</li>
                    ))}
                </ul>
            </div>
            <div>
                <h3>Notifications</h3>
                <ul>
                    {notifications.map((notification, index) => (
                        <li key={index}>{notification.type}: {notification.message}</li>
                    ))}
                </ul>
            </div>
        </div>
    );
}

export default RoomPage;
