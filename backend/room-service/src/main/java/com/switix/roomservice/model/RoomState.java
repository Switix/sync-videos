package com.switix.roomservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class RoomState {
    private Video currentVideo;
    private double currentSeek;
    @JsonIgnore
    private Instant seekUpdatedAt;
    private Boolean isPlaying;
    private List<UserDto> users;
    private List<Video> queue;

    public RoomState() {
        this.users = new ArrayList<>();
        this.queue = new LinkedList<>();
        this.isPlaying = false;
    }

    public void setCurrentSeek(double currentSeek) {
        this.seekUpdatedAt = Instant.now();
        this.currentSeek = currentSeek;
    }
}
