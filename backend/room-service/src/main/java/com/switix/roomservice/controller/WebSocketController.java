package com.switix.roomservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.switix.roomservice.model.RoomNotification;
import com.switix.roomservice.model.RoomNotificationType;
import com.switix.roomservice.model.UserDto;
import com.switix.roomservice.model.Video;
import com.switix.roomservice.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class WebSocketController {

    private final ObjectMapper objectMapper;
    private final RoomService roomService;

    @MessageMapping("/room/{roomId}/join")
    @SendTo("/topic/room/{roomId}")
    public RoomNotification<String> joinRoom(@Payload UserDto user, @DestinationVariable("roomId") String roomIdStr) {
        UUID roomId = UUID.fromString(roomIdStr);
        roomService.addUserToRoom(roomId, user);
        return new RoomNotification<>(RoomNotificationType.USER_JOINED, "", user);
    }

    @MessageMapping("/room/{roomId}/leave")
    @SendTo("/topic/room/{roomId}")
    public RoomNotification<String> leaveRoom(@Payload UserDto user, @DestinationVariable("roomId") String roomIdStr) {
        UUID roomId = UUID.fromString(roomIdStr);
        roomService.removeUserFromRoom(roomId, user);
        return new RoomNotification<>(RoomNotificationType.USER_LEFT, "", user);
    }

    @MessageMapping("/room/{roomId}/addVideo")
    @SendTo("/topic/room/{roomId}")
    public RoomNotification<Video> addVideo(JsonNode payload, @DestinationVariable("roomId") String roomIdStr) {
        UUID roomId = UUID.fromString(roomIdStr);
        UserDto userDto = extractUserDto(payload.get("user"));
        Video video = extractVideo(payload.get("videoData"));
        roomService.addVideoToQueue(roomId, video);

        return new RoomNotification<>(RoomNotificationType.VIDEO_ADDED, video, userDto);
    }

    @MessageMapping("/room/{roomId}/pause")
    @SendTo("/topic/room/{roomId}")
    public RoomNotification<Double> playVideo(JsonNode payload, @DestinationVariable("roomId") String roomIdStr) {
        UUID roomId = UUID.fromString(roomIdStr);
        UserDto userDto = extractUserDto(payload.get("user"));
        boolean isPlaying = payload.get("isPlaying").asBoolean();
        double currentSeek = payload.get("currentSeek").asDouble();

        roomService.setIsPlaying(roomId, isPlaying);
        roomService.setCurrentSeek(roomId, currentSeek);
        return new RoomNotification<>(
                RoomNotificationType.VIDEO_PAUSE,
                currentSeek,
                userDto
        );
    }

    @MessageMapping("/room/{roomId}/play")
    @SendTo("/topic/room/{roomId}")
    public RoomNotification<Double> pauseVideo(JsonNode payload, @DestinationVariable("roomId") String roomIdStr) {
        UUID roomId = UUID.fromString(roomIdStr);
        UserDto userDto = extractUserDto(payload.get("user"));
        boolean isPlaying = payload.get("isPlaying").asBoolean();
        double currentSeek = payload.get("currentSeek").asDouble();

        roomService.setIsPlaying(roomId, isPlaying);
        roomService.setCurrentSeek(roomId, currentSeek);
        return new RoomNotification<>(
                RoomNotificationType.VIDEO_PLAY,
                currentSeek,
                userDto
        );
    }

    private UserDto extractUserDto(JsonNode userJson) {
        try {
            return objectMapper.treeToValue(userJson, UserDto.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Video extractVideo(JsonNode videoJson) {
        try {
            return objectMapper.treeToValue(videoJson, Video.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
