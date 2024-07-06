package com.switix.roomservice.controller;

import com.switix.roomservice.model.AppUser;
import com.switix.roomservice.model.RoomNotification;
import com.switix.roomservice.model.RoomNotificationType;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/joinRoom/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public RoomNotification<String> joinRoom(AppUser user) {
        return new RoomNotification<>(RoomNotificationType.USER_JOINED, user.getUsername() + " joined the room");
    }

    @MessageMapping("/room/{roomId}/pause")
    public void playVideo(Map<String, String> payload, @DestinationVariable("roomId") String roomId) {

        RoomNotification<String> pauseNotification = new RoomNotification<>(RoomNotificationType.VIDEO_PAUSE, payload.get("currentSeek"));
        messagingTemplate.convertAndSend("/topic/room/" + roomId, pauseNotification);

        //RoomNotification<String> syncNotification = new RoomNotification<>(RoomNotificationType.SYNC_CHECK, payload.get("currentSeek"));
        //messagingTemplate.convertAndSend("/topic/room/" + roomId, syncNotification);

    }

    @MessageMapping("/room/{roomId}/play")
    public void pauseVideo(Map<String, String> payload, @DestinationVariable("roomId") String roomId) {

        RoomNotification<String> pauseNotification = new RoomNotification<>(RoomNotificationType.VIDEO_PLAY, payload.get("currentSeek"));
        messagingTemplate.convertAndSend("/topic/room/" + roomId, pauseNotification);

        //RoomNotification<String> syncNotification = new RoomNotification<>(RoomNotificationType.SYNC_CHECK, payload.get("currentSeek"));
        //messagingTemplate.convertAndSend("/topic/room/" + roomId, syncNotification);
    }
}
