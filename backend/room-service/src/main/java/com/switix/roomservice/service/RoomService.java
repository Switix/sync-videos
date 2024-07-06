package com.switix.roomservice.service;

import com.switix.roomservice.model.Room;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

public interface RoomService {
    void addVideoToQueue(UUID roomId, String videoUrl);

    List<String> getQueue(UUID roomId);

    Room createRoom();

    Room getRoomById(UUID id);
}
