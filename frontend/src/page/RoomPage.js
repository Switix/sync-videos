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
                            setQueue((prevQueue) => [...prevQueue, videoData]);
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
        const nextVideo = queue.shift();
        if (nextVideo) {
            setCurrentVideo(nextVideo);
            setCurrentSeek(0);
            api.post(`http://localhost:8080/rooms/${roomId}/state/currentVideo`, nextVideo)
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


    return (
        <div className="px-4    bg-neutral-900  text-white  flex flex-col">
            <div className=" flex  ">
                <div className="w-[calc(100%-18rem)] pr-4 ">
                    <div className="relative bg-black rounded-3xl " style={{ paddingTop: '56.25%' }}>
                        {currentVideo ? (
                            <ReactPlayer
                                ref={playerRef}
                                url={currentVideo.url}
                                controls
                                playing={isPlaying}
                                width="100%"
                                height="100%"
                                className="absolute top-0 left-0 w-full h-full "
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
                    {/* title and author under video */}
                    {currentVideo && (<div className='mt-2 ml-1'>
                        <p className='text-xl font-bold line-clamp-1'>{currentVideo.title}</p>
                        <p className='text-md text-neutral-400 line-clamp-1'>{currentVideo.author}</p>
                    </div>)}
                </div>
                <div className="w-[36rem] pl-4 flex flex-col space-y-4 ">
                    <div className='relative'>
                        {/* room name */}
                        <h2 className="text-2xl font-bold mb-4">Room: {roomId}</h2>
                        {/* url input */}
                        <input
                            type="text"
                            value={videoUrl}
                            onChange={handleVideoUrlTyping}
                            onKeyDown={handleKeyDown}
                            placeholder="Enter YouTube URL"
                            className="w-full p-2 rounded bg-neutral-800 text-white border border-neutral-700 focus:outline-none"
                        />
                        {/* display predicted video from given url */}
                        {predictedVideo && (
                            <div
                                className="absolute  rounded-lg shadow-lg overflow-hidden border-8 border-neutral-800 top-full mt-4 w-full hover:cursor-pointer"
                                onClick={handleVideoSubmit}
                            >
                                {predictedVideo}
                            </div>
                        )}
                    </div>

                    {/* buttons to swap lists */}
                    <div className="flex justify-around">
                        <button
                            onClick={() => setActiveTab('queue')}
                            className={`  w-36 py-2 text-md font-medium text-white text-center rounded  hover:bg-green-700 ${activeTab === 'queue' ? 'bg-green-600' : 'bg-neutral-700'}`}
                        >
                            Queue
                        </button>
                        <button
                            onClick={() => setActiveTab('notifications')}
                            className={` w-36 py-2 text-md font-medium text-white text-center rounded-md  hover:bg-green-700 ${activeTab === 'notifications' ? 'bg-green-600' : 'bg-neutral-700'}`}
                        >
                            Notifications
                        </button>
                    </div>

                    <div className='flex-grow'>
                        <ul ref={listRef} className="bg-neutral-800 space-y-2 p-2 rounded max-h-[21rem] overflow-y-auto">
                            {/* queue list */}
                            {activeTab === 'queue' && queue.length > 0 ? (
                                queue.map((videoData, index) => (
                                    <li key={index}  >{<QueuedVideo videoData={videoData} />}</li>
                                ))
                            ) : activeTab === 'queue' ? (
                                <li className="py-2">Queue is empty</li>
                            ) : null}

                            {/* notifications list */}
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

                    {/* user list */}
                    <div className="flex flex-col !mb-[3.75rem] ">
                        <div className='w-full'>
                            <h3 className="flex items-center justify-center text-xl font-bold mb-2">
                                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 mr-2 text-red-500">
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M18 18.72a9.094 9.094 0 0 0 3.741-.479 3 3 0 0 0-4.682-2.72m.94 3.198.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0 1 12 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 0 1 6 18.719m12 0a5.971 5.971 0 0 0-.941-3.197m0 0A5.995 5.995 0 0 0 12 12.75a5.995 5.995 0 0 0-5.058 2.772m0 0a3 3 0 0 0-4.681 2.72 8.986 8.986 0 0 0 3.74.477m.94-3.197a5.971 5.971 0 0 0-.94 3.197M15 6.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Zm6 3a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Zm-13.5 0a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Z" />
                                </svg>
                                {roomUsers.length}
                            </h3>
                            <ul className="p-2 rounded max-h-44 overflow-y-auto grid grid-cols-2 gap-2">
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
        </div>
    );
}

export default RoomPage;