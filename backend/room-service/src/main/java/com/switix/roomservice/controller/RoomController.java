package com.switix.roomservice.controller;

import com.switix.roomservice.exception.RoomNotFoundException;
import com.switix.roomservice.model.Room;
import com.switix.roomservice.repository.RoomRepository;
import com.switix.roomservice.service.RoomService;
import com.switix.roomservice.service.RoomServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class RoomController {


    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/create")
    public Room createRoom() {
        return roomService.createRoom();
    }

    @GetMapping("/exists/{roomId}")
    public ResponseEntity<Boolean> roomExists(@PathVariable("roomId") String roomId) {
        try {
            UUID uuid = UUID.fromString(roomId);
            roomService.getRoomById(uuid);
            return ResponseEntity.ok(true);
        } catch (IllegalArgumentException | RoomNotFoundException e) {
            return ResponseEntity.ok(false);
        }
    }

}

