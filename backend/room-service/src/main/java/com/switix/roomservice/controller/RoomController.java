package com.switix.roomservice.controller;

import com.switix.roomservice.exception.RoomNotFoundException;
import com.switix.roomservice.model.Room;
import com.switix.roomservice.model.RoomNotification;
import com.switix.roomservice.model.RoomNotificationType;
import com.switix.roomservice.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class RoomController {


    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomController(RoomService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/create")
    public Room createRoom() {
        return roomService.createRoom();
    }

    @PostMapping("/addVideo")
    public void addVideo(@RequestParam("roomId") String roomIdStr, @RequestBody String videoUrl) {
        UUID roomId = UUID.fromString(roomIdStr);
        roomService.addVideoToQueue(roomId, videoUrl);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, new RoomNotification<>(RoomNotificationType.VIDEO_ADDED, videoUrl));
    }

    @GetMapping("/exists/{roomId}")
    public ResponseEntity<Boolean> roomExists(@PathVariable("roomId") String roomIdStr) {
        try {
            UUID roomId = UUID.fromString(roomIdStr);
            roomService.getRoomById(roomId);
            return ResponseEntity.ok(true);
        } catch (IllegalArgumentException | RoomNotFoundException e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/{roomId}/queue")
    public List<String> getQueue(@PathVariable("roomId") String roomIdStr) {
        UUID roomId = UUID.fromString(roomIdStr);
        return roomService.getQueue(roomId);
    }

}

