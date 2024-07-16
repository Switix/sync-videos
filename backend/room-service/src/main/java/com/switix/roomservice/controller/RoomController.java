package com.switix.roomservice.controller;

import com.switix.roomservice.exception.RoomNotFoundException;
import com.switix.roomservice.model.Room;
import com.switix.roomservice.model.RoomState;
import com.switix.roomservice.model.VideoUrlDto;
import com.switix.roomservice.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
            UUID roomId = UUID.fromString(roomIdStr);
            roomService.getRoomById(roomId);
            return ResponseEntity.ok(true);
        } catch (IllegalArgumentException | RoomNotFoundException e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/{roomId}/state")
    public ResponseEntity<RoomState> getRoomState(@PathVariable("roomId") String roomIdStr) {

        UUID roomId = UUID.fromString(roomIdStr);
        RoomState roomState = roomService.getRoomState(roomId);
        return ResponseEntity.ok(roomState);

    }

    @PostMapping("/{roomId}/state/currentVideoUrl")
    public ResponseEntity<RoomState> setCurrentVideoUrl(@PathVariable("roomId") String roomIdStr, @RequestBody VideoUrlDto videoUrlDto) {
        UUID roomId = UUID.fromString(roomIdStr);
        roomService.setCurrentVideoUrl(roomId, videoUrlDto.getCurrentVideoUrl());
        roomService.setCurrentSeek(roomId,0);
        return ResponseEntity.ok().build();

    }

}

