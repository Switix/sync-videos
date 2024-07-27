package com.switix.roomservice.controller;

import com.switix.roomservice.exception.RoomNotFoundException;
import com.switix.roomservice.model.Room;
import com.switix.roomservice.model.RoomState;
import com.switix.roomservice.model.Video;
import com.switix.roomservice.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@AllArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create")
    public Room createRoom() {
        return roomService.createRoom();
    }


    @GetMapping("/exists/{roomId}")
    public ResponseEntity<Boolean> roomExists(@PathVariable("roomId") String roomIdStr) {
        try {
            roomService.getRoomById(roomIdStr);
            return ResponseEntity.ok(true);
        } catch (IllegalArgumentException | RoomNotFoundException e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/{roomId}/state")
    public ResponseEntity<RoomState> getRoomState(@PathVariable("roomId") String roomIdStr) {
        RoomState roomState = roomService.getRoomState(roomIdStr);
        return ResponseEntity.ok(roomState);

    }

    @PostMapping("/{roomId}/state/currentVideo")
    public ResponseEntity<Void> setCurrentVideo(@PathVariable("roomId") String roomIdStr, @RequestBody Video video) {
        roomService.setCurrentVideo(roomIdStr, video);
        roomService.setCurrentSeek(roomIdStr, 0);
        return ResponseEntity.ok().build();

    }

}

