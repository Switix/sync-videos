package com.switix.roomservice.service;

import com.switix.roomservice.model.Room;
import com.switix.roomservice.model.RoomState;
import com.switix.roomservice.model.UserDto;

import java.util.UUID;

public interface RoomService {
    void addVideoToQueue(UUID roomId, String videoUrl);


    RoomState getRoomState(UUID roomId);

    Room createRoom();

    Room getRoomById(UUID id);

    void addUserToRoom(UUID roomId, UserDto user);

    void removeUserFromRoom(UUID roomId, UserDto user);

    void setCurrentVideoUrl(UUID roomId, String currentVideoUrl);

    void setIsPlaying(UUID roomId, Boolean isPlaying);

    void setCurrentSeek(UUID roomId, double currentSeek);
}
