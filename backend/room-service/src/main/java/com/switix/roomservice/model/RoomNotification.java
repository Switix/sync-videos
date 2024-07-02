package com.switix.roomservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoomNotification {
    private String message;

    public RoomNotification(String message) {
        this.message = message;
    }

}