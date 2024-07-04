package com.switix.roomservice.service;

import com.switix.roomservice.exception.RoomNotFoundException;
import com.switix.roomservice.model.Room;
import com.switix.roomservice.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void addVideoToQueue(UUID roomId, String videoUrl) {

    }

    @Override
    public Room createRoom() {
        Room room = new Room();
        room.setName("test");
        return roomRepository.save(room);
    }

    @Override
    public Room getRoomById(UUID id) {
        return roomRepository.findById(id).orElseThrow(() -> new RoomNotFoundException("Room not found"));
    }
}
