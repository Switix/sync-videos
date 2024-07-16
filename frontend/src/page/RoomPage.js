import React, { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import RoomNotificationType from '../constants/RoomNotificationTypes';
import RoomNotification from '../model/RoomNotification';
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
    const notificationListRef = useRef(null);
    const videoStarted = useRef(true);
    const issuedBy = useRef(null);

    //roomState
    const [queue, setQueue] = useState([]);
    const [roomUsers, setRoomUsers] = useState([]);
    const [currentVideoUrl, setCurrentVideoUrl] = useState(null);
    const [currentSeek, setCurrentSeek] = useState(null);


    const playerRef = useRef(null);
    const [isPlaying, setIsPlaying] = useState(false);

    const [videoUrl, setVideoUrl] = useState('');

    const user = useSelector(state => state.user);

    const SEEK_TRESHOLD = 0.5 // in seconds



    useEffect(() => {
        window.addEventListener('beforeunload', () => {
            if (stompClientRef.current) {
                stompClientRef.current.send(`/app/room/${roomId}/leave`, {}, JSON.stringify(user));
            }
        });
        // eslint-disable-next-line
    }, [])

    useEffect(() => {

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
                stompClientRef.current.send(`/app/room/${roomId}/join`, {}, JSON.stringify(user));
            }
        };
        const fetchRoomState = async (roomId) => {
            try {
                const response = await api.get(`http://localhost:8080/rooms/${roomId}/state`)
                const data = response.data;

                setQueue(data.queue);
                setRoomUsers(data.users);
                setCurrentVideoUrl(data.currentVideoUrl);
                setIsPlaying(data.isPlaying);
                setCurrentSeek(data.currentSeek);

            } catch (error) {
                console.error('Error fetching room state', error);
                return false;
            }
        }

        const subscribeToRoom = (roomId) => {
            if (stompClientRef.current) {
                stompClientRef.current.subscribe(`/topic/room/${roomId}`, (message) => {
                    const serverNotification = JSON.parse(message.body);
                    const issuer = serverNotification.issuer

                    const roomNotification = new RoomNotification();

                    switch (serverNotification.type) {
                        case RoomNotificationType.VIDEO_ADDED:
                            const videoUrl = serverNotification.message;
                            setQueue((prevQueue) => [...prevQueue, videoUrl]);

                            roomNotification.setType(RoomNotificationType.VIDEO_ADDED);
                            roomNotification.setMessage("Added Video");
                            roomNotification.setIssuer(issuer);
                            break;
                        case RoomNotificationType.USER_JOINED:
                            roomNotification.setType(RoomNotificationType.USER_JOINED);
                            roomNotification.setMessage("Joined the room");
                            roomNotification.setIssuer(issuer);

                            if (issuer.id !== user.id) {
                                setRoomUsers((prevRoomUsers) => [...prevRoomUsers, issuer]);
                            }
                            break;
                        case RoomNotificationType.USER_LEFT:
                            roomNotification.setType(RoomNotificationType.USER_LEFT);
                            roomNotification.setMessage("Left the room");
                            roomNotification.setIssuer(issuer);

                            setRoomUsers((prevRoomUsers) => prevRoomUsers.filter(user => user.id !== issuer.id));
                            break;
                        case RoomNotificationType.VIDEO_PLAY:
                            if (user.id !== issuer.id) {
                                issuedBy.current = issuer;
                            }

                            setIsPlaying(true);
                            if (Math.abs(playerRef.current.getCurrentTime() - serverNotification.message) > SEEK_TRESHOLD) {
                                playerRef.current.seekTo(serverNotification.message, 'seconds', true)
                            }

                            roomNotification.setType(RoomNotificationType.VIDEO_PLAY);
                            roomNotification.setMessage(`Video resumed with seek: ${serverNotification.message}`);
                            roomNotification.setIssuer(issuer);
                            break;
                        case RoomNotificationType.VIDEO_PAUSE:
                            if (user.id !== issuer.id) {
                                issuedBy.current = issuer;
                            }

                            setIsPlaying(false);

                            roomNotification.setType(RoomNotificationType.VIDEO_PAUSE);
                            roomNotification.setMessage(`Video paused with seek: ${serverNotification.message}`);
                            roomNotification.setIssuer(issuer);
                            break;
                        case RoomNotificationType.SYNC_CHECK:
                            if (Math.abs(playerRef.current.getCurrentTime() - serverNotification.message) > SEEK_TRESHOLD) {
                                playerRef.current.seekTo(serverNotification.message, 'seconds', isPlaying)
                            }
                            break;
                        default:
                            console.warn('Unhandled notification type:', serverNotification.type);
                            break;
                    }

                    setNotifications((prevNotifications) => [...prevNotifications, roomNotification]);
                });
            }
        };


        checkRoomExists(roomId)
            .then(exists => {
                if (exists) {
                    if (user == null) return;
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

                        fetchRoomState(roomId);
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


    useEffect(() => {
        if (currentVideoUrl != null)
            return;
        playNextVideo();
        // eslint-disable-next-line
    }, [queue]);

    useEffect(() => {
        scrollToBottom();
    }, [notifications]);

    const scrollToBottom = () => {
        notificationListRef.current.scrollTop = notificationListRef.current.scrollHeight;
    };

    const addVideoToQueue = () => {
        if (stompClientRef.current) {
            stompClientRef.current.send(`/app/room/${roomId}/addVideo`, {}, JSON.stringify({ user, videoUrl }));
        }
    };

    const playNextVideo = () => {
        const nextVideo = queue.shift();
        setCurrentVideoUrl(nextVideo || null);
        setCurrentSeek(0);
        if (nextVideo) {
            api.post(`http://localhost:8080/rooms/${roomId}/state/currentVideoUrl`, { currentVideoUrl: nextVideo })
        };
    };

    const handlePlay = () => {
        if (videoStarted.current) {
            videoStarted.current = false
            return;
        }
        if (issuedBy.current != null && user.id !== issuedBy.current.id) {
            issuedBy.current = null;
            return;
        }
        console.log('play')
        if (stompClientRef.current) {
            stompClientRef.current.send(`/app/room/${roomId}/play`, {}, JSON.stringify({ user, isPlaying: true, currentSeek: playerRef.current.getCurrentTime() }));
        }
        issuedBy.current = null;

    }
    const handlePause = () => {
        if (issuedBy.current != null && user.id !== issuedBy.current.id) {
            issuedBy.current = null;
            return;
        }
        console.log('pause')
        if (stompClientRef.current) {
            console.log('send pause')
            stompClientRef.current.send(`/app/room/${roomId}/pause`, {}, JSON.stringify({ user, isPlaying: false, currentSeek: playerRef.current.getCurrentTime() }));
        }

    }

    const handleStart = () => {
        console.log('start')
        videoStarted.current = isPlaying;
    }

    const handleReady = () => {
        console.log('ready');
        playerRef.current.seekTo(currentSeek, 'seconds', false);
    }


    return (
        <div>
            {user ? <p>Username:
                <span style={{ color: user.userColor }}> {user.username}</span>
            </p> : <p>No user logged in</p>}

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
                url={currentVideoUrl}
                controls
                playing={isPlaying}
                width="100%"
                height="100%"
                onEnded={playNextVideo}
                onPlay={handlePlay}
                onStart={handleStart}
                onPause={handlePause}
                onReady={handleReady}
            />
            <div>
                <h3>Current Video</h3>
                {currentVideoUrl}
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
                <h3>Lobby</h3>
                <ul>
                    {roomUsers.map((user, index) => (
                        <li style={{ color: user.userColor }} key={index}>{user.username}</li>
                    ))}
                </ul>
            </div>
            <div>
                <h3>Notifications</h3>
                <ul ref={notificationListRef} style={{ maxHeight: '300px', overflowY: 'scroll', padding: '10px', border: '1px solid #ccc' }}>
                    {notifications.map((notification, index) => (
                        <li key={index} style={{ marginBottom: '10px' }}>
                            <div>
                                {`${notification.type} [`}
                                <span style={{ color: notification.issuer.userColor }}>{notification.issuer.username}</span>
                                {`]: ${notification.message}`}
                            </div>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}

export default RoomPage;
