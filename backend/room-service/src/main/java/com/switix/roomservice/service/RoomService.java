package com.switix.roomservice.service;

import com.switix.roomservice.model.Room;
import com.switix.roomservice.model.RoomState;
import com.switix.roomservice.model.UserDto;
import com.switix.roomservice.model.Video;

import java.util.List;

public interface RoomService {
    void addVideoToQueue(String roomId, Video video);


    RoomState getRoomState(String roomId);

    Room createRoom();

    Room getRoomById(String id);

    void addUserToRoom(String roomId, UserDto user);

    void removeUserFromRoom(String roomId, UserDto user);

    void setCurrentVideo(String roomId, Video currentVideo);

    void setIsPlaying(String roomId, Boolean isPlaying);

    void setCurrentSeek(String roomId, double currentSeek);

    void removeVideoFormQueue(String roomId, String url);

    void setQueue(String roomId, List<Video> queue);
}
