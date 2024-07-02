package com.switix.roomservice.controller;

import com.switix.roomservice.model.AppUser;
import com.switix.roomservice.model.RoomNotification;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/joinRoom/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public RoomNotification joinRoom(AppUser user, @DestinationVariable("roomId") String roomId) {
        return new RoomNotification(user.getUsername() + " joined the room");
    }

}
