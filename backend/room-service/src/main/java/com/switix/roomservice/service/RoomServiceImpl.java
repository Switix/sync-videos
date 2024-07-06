package com.switix.roomservice.service;

import com.switix.roomservice.exception.RoomNotFoundException;
import com.switix.roomservice.model.Room;
import com.switix.roomservice.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final Map<UUID, List<String>> roomQueues = new HashMap<>();

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<String> getQueue(UUID roomId) {
        return roomQueues.computeIfAbsent(roomId, k -> new LinkedList<>());
    }

    @Override
    public void addVideoToQueue(UUID roomId, String videoUrl) {
        List<String> queue = getQueue(roomId);
        queue.add(videoUrl);
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
