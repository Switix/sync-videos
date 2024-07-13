package com.switix.roomservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.switix.roomservice.model.RoomNotification;
import com.switix.roomservice.model.RoomNotificationType;
import com.switix.roomservice.model.UserDto;
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
    public RoomNotification<String> joinRoom(@Payload UserDto user) {
        return new RoomNotification<>(RoomNotificationType.USER_JOINED, null, user);
    }

    @MessageMapping("/room/{roomId}/addVideo")
    @SendTo("/topic/room/{roomId}")
    public RoomNotification<String> addVideo(JsonNode payload, @DestinationVariable("roomId") String roomIdStr) {
        UUID roomId = UUID.fromString(roomIdStr);
        UserDto userDto = extractUserDto(payload.get("user"));
        String videoUrl = payload.get("videoUrl").asText();
        roomService.addVideoToQueue(roomId, videoUrl);

        return new RoomNotification<>(RoomNotificationType.VIDEO_ADDED, videoUrl, userDto);
    }

    @MessageMapping("/room/{roomId}/pause")
    @SendTo("/topic/room/{roomId}")
    public RoomNotification<String> playVideo(JsonNode payload, @DestinationVariable("roomId") String roomId) {
        UserDto userDto = extractUserDto(payload.get("user"));
        return new RoomNotification<>(
                RoomNotificationType.VIDEO_PAUSE,
                payload.get("currentSeek").asText(),
                userDto
        );
    }

    @MessageMapping("/room/{roomId}/play")
    @SendTo("/topic/room/{roomId}")
    public RoomNotification<String> pauseVideo(JsonNode payload, @DestinationVariable("roomId") String roomId) {

        UserDto userDto = extractUserDto(payload.get("user"));
        return new RoomNotification<>(
                RoomNotificationType.VIDEO_PLAY,
                payload.get("currentSeek").asText(),
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
}
