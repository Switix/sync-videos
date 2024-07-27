package com.switix.roomservice.service;

import com.switix.roomservice.exception.RoomNotFoundException;
import com.switix.roomservice.model.Room;
import com.switix.roomservice.model.RoomState;
import com.switix.roomservice.model.UserDto;
import com.switix.roomservice.model.Video;
import com.switix.roomservice.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final Map<String, RoomState> roomStates = new HashMap<>();

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomState getRoomState(String roomId) {
        RoomState roomState = roomStates.computeIfAbsent(roomId, k -> new RoomState());
        updateCurrentSeek(roomState);
        return roomState;
    }

    @Override
    public void addUserToRoom(String roomId, UserDto user) {
        getRoomState(roomId).getUsers().add(user);
    }

    @Override
    public void removeUserFromRoom(String roomId, UserDto user) {
        getRoomState(roomId).getUsers()
                .removeIf(u -> u.getId().equals(user.getId()));
    }

    @Override
    public void setCurrentVideo(String roomId, Video currentVideo) {
        RoomState roomState = getRoomState(roomId);
        roomState.setCurrentVideo(currentVideo);
        roomState.getQueue().removeIf(video -> video.getUrl().equals(currentVideo.getUrl()));
    }

    @Override
    public void setIsPlaying(String roomId, Boolean isPlaying) {
        getRoomState(roomId).setIsPlaying(isPlaying);
    }

    @Override
    public void setCurrentSeek(String roomId, double currentSeek) {
        getRoomState(roomId).setCurrentSeek(currentSeek);
    }

    @Override
    public void removeVideoFormQueue(String roomId, String url) {
        RoomState roomState = getRoomState(roomId);
        roomState.getQueue().removeIf(video -> video.getUrl().equals(url));
    }

    @Override
    public void setQueue(String roomId, List<Video> queue) {
        getRoomState(roomId).setQueue(queue);
    }

    @Override
    public void addVideoToQueue(String roomId, Video video) {
        getRoomState(roomId).getQueue().add(video);
    }

    @Override
    public Room createRoom() {
        Room room = new Room();
        room.setName("test");
        return roomRepository.save(room);
    }

    @Override
    public Room getRoomById(String id) {
        return roomRepository.findById(id).orElseThrow(() -> new RoomNotFoundException("Room not found"));
    }

    private void updateCurrentSeek(RoomState roomState) {
        double currentSeek = roomState.getCurrentSeek();
        Instant seekUpdatedAt = roomState.getSeekUpdatedAt();

        if (seekUpdatedAt != null && roomState.getIsPlaying()) {
            Duration duration = Duration.between(seekUpdatedAt, Instant.now());
            double secondsElapsed = duration.toMillis() / 1000.0; // Convert to seconds
            double newSeek = currentSeek + secondsElapsed;
            roomState.setCurrentSeek(newSeek);
        }

    }


}
