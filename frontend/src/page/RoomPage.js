import React, { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import RoomNotificationType from '../constants/RoomNotificationTypes';
import RoomNotification from '../model/RoomNotification';
import ReactPlayer from 'react-player';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import api from '../service/api';
import axios from 'axios'
import { useSelector } from 'react-redux';
import QueuedVideo from '../component/QueuedVideo';
import UserItem from '../component/UserItem';
import { FaTrash, FaArrowUp, FaArrowDown } from "react-icons/fa";


function RoomPage() {
    const { roomId } = useParams();
    const navigate = useNavigate();
    const [notifications, setNotifications] = useState([]);

    const stompClientRef = useRef(null);
    const listRef = useRef(null);
    const videoStarted = useRef(true);
    const issuedBy = useRef(null);

    const [predictedVideo, setPredictedVideo] = useState(null);
    const [predictedVideoData, setPredictedVideoData] = useState(null);

    const [activeTab, setActiveTab] = useState('queue');

    //roomState
    const [queue, setQueue] = useState([]);
    const [roomUsers, setRoomUsers] = useState([]);
    const [currentVideo, setCurrentVideo] = useState(null);
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
                const response = await api.get(`http://188.47.81.23:8080/rooms/exists/${roomId}`)
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
                const response = await api.get(`http://188.47.81.23:8080/rooms/${roomId}/state`)
                const data = response.data;

                setQueue(data.queue);
                setRoomUsers(data.users);
                setCurrentVideo(data.currentVideo);
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
                            roomNotification.setType(RoomNotificationType.VIDEO_ADDED);
                            roomNotification.setMessage("Added Video");
                            roomNotification.setIssuer(issuer);

                            const videoData = serverNotification.message;
                            setQueue(prevQueue => {
                                if (prevQueue) {
                                    return [...prevQueue, videoData];
                                } else {
                                    return [videoData];
                                }
                            });
                            break;
                        case RoomNotificationType.VIDEO_REMOVED:
                            roomNotification.setType(RoomNotificationType.VIDEO_REMOVED);
                            roomNotification.setMessage('Video Removed');
                            roomNotification.setIssuer(issuer);
                            const url = serverNotification.message;
                            setQueue((prevQueue) => prevQueue.filter(video => video.url !== url));
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
                        case RoomNotificationType.VIDEO_MOVED:
                            const queue = serverNotification.message;
                            setQueue(queue);

                            roomNotification.setType(RoomNotificationType.VIDEO_MOVED);
                            roomNotification.setMessage(`Moved video`);
                            roomNotification.setIssuer(issuer);
                            break;
                        case RoomNotificationType.VIDEO_SKIPPED:
                            if (user.id !== issuer.id) {
                                playNextVideo();
                            }

                            roomNotification.setType(RoomNotificationType.VIDEO_SKIPPED);
                            roomNotification.setMessage(`Skipped video`);
                            roomNotification.setIssuer(issuer);
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
                    const socket = new SockJS('http://188.47.81.23:8080/ws');
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
        if (currentVideo != null)
            return;
        playNextVideo();
        // eslint-disable-next-line
    }, [queue]);

    useEffect(() => {
        if (activeTab === "notifications") {
            scrollToBottom();
        }
        else {
            scrollToTop();
        }
        // eslint-disable-next-line
    }, [activeTab]);

    const scrollToBottom = () => {
        if (listRef.current) {
            listRef.current.scrollTop = listRef.current.scrollHeight;
        }

    };
    const scrollToTop = () => {
        if (listRef.current) {
            listRef.current.scrollTop = 0;
        }

    };

    const playNextVideo = () => {
        setQueue(prevQueue => {
            if (!prevQueue || prevQueue.length === 0) {
                return prevQueue;
            }
            const [nextVideo, ...nextQueue] = prevQueue;


            if (nextVideo) {
                setCurrentVideo(nextVideo);
                setCurrentSeek(0);
                api.post(`http://188.47.81.23:8080/rooms/${roomId}/state/currentVideo`, nextVideo)
            }
            return nextQueue;
        });
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

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            handleVideoSubmit();
        }
    };

    const handleVideoSubmit = () => {
        if (!predictedVideo && !predictedVideoData) {
            return;
        }

        if (stompClientRef.current) {
            stompClientRef.current.send(`/app/room/${roomId}/addVideo`, {}, JSON.stringify({ user, videoData: predictedVideoData }));
        }
        setVideoUrl("");
        setPredictedVideo(null);
        setPredictedVideoData(null);

    };
    const handleVideoUrlTyping = (e) => {
        const url = e.target.value;
        setVideoUrl(url);

        if (url.includes('youtube.com/watch?v=') || url.includes('youtu.be/')) {
            axios.get(`https://www.youtube.com/oembed?url=${url}`)
                .then(response => {
                    const { title, author_name, thumbnail_url } = response.data;
                    const predictedVideoData = { title, author: author_name, thumbnail: thumbnail_url, url };
                    setPredictedVideoData(predictedVideoData);
                    setPredictedVideo(<QueuedVideo videoData={predictedVideoData} />);
                })
                .catch(() => {
                    setPredictedVideo(null);
                    setPredictedVideoData(null);
                });
        } else {
            setPredictedVideo(null);
            setPredictedVideoData(null);
        }
    };

    const removeFromQueue = (url) => {
        if (stompClientRef.current) {
            stompClientRef.current.send(`/app/room/${roomId}/removeVideo`, {}, JSON.stringify({ user, url }));
        }
    }

    const skipVideo = () => {
        playNextVideo();
        if (stompClientRef.current) {
            stompClientRef.current.send(`/app/room/${roomId}/skipVideo`, {}, JSON.stringify(user));
        }
    }

    const moveVideo = (url, direction) => {
        const index = queue.findIndex(video => video.url === url);
        if (index === -1) return;

        // Ensure the video is not moved out of bounds
        if ((direction === 'up' && index === 0) || (direction === 'down' && index === queue.length - 1)) {
            return;
        }

        const newQueue = Array.from(queue);
        const [movedVideo] = newQueue.splice(index, 1);

        if (direction === 'up' && index > 0) {
            newQueue.splice(index - 1, 0, movedVideo);
        } else if (direction === 'down' && index < newQueue.length) {
            newQueue.splice(index + 1, 0, movedVideo);
        }

        if (stompClientRef.current) {
            stompClientRef.current.send(`/app/room/${roomId}/updateQueue`, {}, JSON.stringify({ user, queue: newQueue }));
        }

        setQueue(newQueue);
    };

    return (
        <div className="sm:px-4 bg-neutral-900 text-white flex flex-col sm:flex-row">
            <div className="w-full sm:w-[calc(100%-18rem)] pr-0 sm:pr-4">

                <div className="relative bg-black rounded-3xl mb-4 sm:mb-0" style={{ paddingTop: '56.25%' }}>
                    {currentVideo ? (
                        <ReactPlayer
                            ref={playerRef}
                            url={currentVideo.url}
                            controls
                            playing={isPlaying}
                            pip={true}
                            width="100%"
                            height="100%"
                            className="absolute top-0 left-0 w-full h-full"
                            onEnded={playNextVideo}
                            onPlay={handlePlay}
                            onPause={handlePause}
                            onStart={handleStart}
                            onReady={handleReady}
                        />
                    ) : (
                        <div className="absolute top-0 left-0 w-full h-full flex items-center justify-center text-stone-500 text-4xl">
                            <p>No video</p>
                        </div>
                    )}
                </div>
                {currentVideo && (
                    <div className="mt-2 px-4 sm:px-0 ml-1 flex items-start justify-between">
                        <div>
                            <p className="text-xl font-bold line-clamp-1">{currentVideo.title}</p>
                            <p className="text-md text-neutral-400 line-clamp-1">{currentVideo.author}</p>
                        </div>
                        {queue && queue.length > 0 && (
                            <button
                                onClick={skipVideo}
                                className="ml-4 px-2 bg-green-600 text-white rounded hover:bg-green-700 transition duration-300"
                            >
                                {'>>'}
                            </button>
                        )}
                    </div>
                )}
            </div>
            <div className="w-full sm:w-[36rem] px-4 sm:px-0  flex flex-col space-y-4">
                <div className="relative">
                    <h2 className="text-2xl font-bold mb-4 hidden sm:block">Room: {roomId}</h2>
                    <input
                        type="text"
                        value={videoUrl}
                        onChange={handleVideoUrlTyping}
                        onKeyDown={handleKeyDown}
                        placeholder="Enter YouTube URL"
                        className="w-full p-2 mt-2 sm:mt-0 rounded bg-neutral-800 text-white border border-neutral-700 focus:outline-none"
                    />
                    {predictedVideo && (
                        <div
                            className="absolute z-50 rounded-lg shadow-lg overflow-hidden border-8 border-neutral-800 top-full mt-4 w-full hover:cursor-pointer"
                            onClick={handleVideoSubmit}
                        >
                            {predictedVideo}
                        </div>
                    )}
                </div>
                <div className="flex justify-around">
                    <button
                        onClick={() => setActiveTab('queue')}
                        className={`w-36 py-2 text-md font-medium text-white text-center rounded ${activeTab === 'queue' ? 'bg-green-600' : 'bg-neutral-700'} hover:bg-green-700`}
                    >
                        Queue
                    </button>
                    <button
                        onClick={() => setActiveTab('notifications')}
                        className={`w-36 py-2 text-md font-medium text-white text-center rounded-md ${activeTab === 'notifications' ? 'bg-green-600' : 'bg-neutral-700'} hover:bg-green-700`}
                    >
                        Notifications
                    </button>
                </div>
                <div className="flex-grow ">
                    <ul ref={listRef} className="bg-neutral-800 space-y-2 p-2 rounded max-h-[21rem] overflow-y-auto">
                        {activeTab === 'queue' && queue && queue.length > 0 ? (
                            queue.map((videoData, index) => (
                                <li key={index} className="relative group">
                                    <QueuedVideo videoData={videoData} />
                                    {index > 0 && (
                                        <FaArrowUp
                                            className="absolute top-2 right-2 p-1 cursor-pointer w-6 h-6 hidden group-hover:block text-gray-400 hover:text-gray-300"
                                            onClick={() => moveVideo(videoData.url, 'up')}
                                        />
                                    )}
                                    {index < queue.length - 1 && (
                                        <FaArrowDown
                                            className="absolute top-10 right-2 p-1 cursor-pointer w-6 h-6 hidden group-hover:block text-gray-400 hover:text-gray-300"
                                            onClick={() => moveVideo(videoData.url, 'down')}
                                        />
                                    )}
                                    <FaTrash
                                        className="absolute bottom-2 right-2 p-1 cursor-pointer w-6 h-6 text-red-700 hidden group-hover:block hover:text-red-600"
                                        onClick={() => removeFromQueue(videoData.url)}
                                    />
                                </li>
                            ))
                        ) : activeTab === 'queue' ? (
                            <li className="py-2">Queue is empty</li>
                        ) : null}
                        {activeTab === 'notifications' ? (
                            notifications.map((notification, index) => (
                                <li key={index} className="mb-2 last:mb-0">
                                    <div>
                                        {`${notification.type} [`}
                                        <span style={{ color: notification.issuer.userColor }}>{notification.issuer.username}</span>
                                        {`]: ${notification.message}`}
                                    </div>
                                </li>
                            ))
                        ) : activeTab === 'notifications' ? (
                            <li className="py-2">No notifications</li>
                        ) : null}
                    </ul>
                </div>
                <div className="flex flex-col mb-2">
                    <div className="w-full">
                        <h3 className="flex items-center justify-center text-xl font-bold mb-2">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 mr-2 text-red-500">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M18 18.72a9.094 9.094 0 0 0 3.741-.479 3 3 0 0 0-4.682-2.72m.94 3.198.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0 1 12 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 0 1 6 18.719m12 0a5.971 5.971 0 0 0-.941-3.197m0 0A5.995 5.995 0 0 0 12 12.75a5.995 5.995 0 0 0-5.058 2.772m0 0a3 3 0 0 0-4.681 2.72 8.986 8.986 0 0 0 3.74.477m.94-3.197a5.971 5.971 0 0 0-.94 3.197M15 6.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Zm6 3a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Zm-13.5 0a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Z" />
                            </svg>
                            {roomUsers.length}
                        </h3>
                        <ul className=" mb-4 rounded max-h-44 overflow-y-auto grid grid-cols-2 gap-2">
                            {roomUsers.map((user, index) => (
                                <li key={index}>
                                    <UserItem user={user} />
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>
            </div>
        </div>

    );
}

export default RoomPage;