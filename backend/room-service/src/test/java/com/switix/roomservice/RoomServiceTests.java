package com.switix.roomservice;

import com.switix.roomservice.exception.RoomNotFoundException;
import com.switix.roomservice.model.Room;
import com.switix.roomservice.model.RoomState;
import com.switix.roomservice.model.UserDto;
import com.switix.roomservice.model.Video;
import com.switix.roomservice.repository.RoomRepository;
import com.switix.roomservice.service.RoomServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTests {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomServiceImpl roomService;


    @Test
    public void testGetRoomById_WhenRoomExists_ThenRoomIsReturned() {
        // Given
        String roomId = "abcdefghij";
        Room room = new Room();
        room.setId(roomId);
        given(roomRepository.findById(roomId)).willReturn(Optional.of(room));

        // When
        Room foundRoom = roomService.getRoomById(roomId);

        // Then
        verify(roomRepository).findById(roomId);
        assertThat(foundRoom.getId())
                .isEqualTo(roomId);
        assertThat(foundRoom)
                .isEqualTo(room)
                .isNotNull()
                .isInstanceOf(Room.class);
    }

    @Test
    void testGetRoomById_WhenRoomDoesNotExist_ThenExceptionIsThrown() {
        // Given
        String roomId = "abcdefghij";
        given(roomRepository.findById(roomId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roomService.getRoomById(roomId))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessage("Room not found");
    }

    @Test
    void testGetRoomState_WhenNewRoomIdIsProvided_ThenDefaultRoomStateIsReturned() {
        // Given
        String roomId = "room1";

        // When
        RoomState roomState = roomService.getRoomState(roomId);

        // Then
        assertThat(roomState)
                .isNotNull()
                .isInstanceOf(RoomState.class);
        // Assert default state of RoomState
        assertThat(roomState.getCurrentSeek()).isEqualTo(0);
        assertThat(roomState.getCurrentVideo()).isNull();
        assertThat(roomState.getSeekUpdatedAt()).isNull();
        assertThat(roomState.getIsPlaying()).isFalse();

        assertThat(roomState.getUsers()).isNotNull();
        assertThat(roomState.getUsers().isEmpty()).isTrue();

        assertThat(roomState.getQueue()).isNotNull();
        assertThat(roomState.getQueue().isEmpty()).isTrue();

    }

    @Test
    void testGetRoomState_WhenExistingRoomIdIsProvided_ThenRoomStateIsReturned() {
        // Given
        String roomId = "abcdef";
        Video video = new Video();
        RoomState initialRoomState = roomService.getRoomState(roomId);
        initialRoomState.setIsPlaying(true);
        initialRoomState.setCurrentVideo(video);
        initialRoomState.setCurrentSeek(10);
        Instant now = Instant.now();
        initialRoomState.setSeekUpdatedAt(now);

        // When
        RoomState expectedRoomState = roomService.getRoomState(roomId);

        // Then
        assertThat(expectedRoomState)
                .isNotNull()
                .isInstanceOf(RoomState.class);

        // Assert state of RoomState
        assertThat(expectedRoomState.getCurrentSeek()).isEqualTo(10);
        assertThat(expectedRoomState.getCurrentVideo()).isEqualTo(video);
        assertThat(expectedRoomState.getSeekUpdatedAt()).isEqualTo(now);
        assertThat(expectedRoomState.getIsPlaying()).isTrue();

        assertThat(expectedRoomState.getUsers()).isNotNull();
        assertThat(expectedRoomState.getUsers().isEmpty()).isTrue();

        assertThat(expectedRoomState.getQueue()).isNotNull();
        assertThat(expectedRoomState.getQueue().isEmpty()).isTrue();
    }

    @Test
    void testAddUserToRoom_WhenUserIsAdded_ThenUserIsInRoom() {
        // Given
        String roomId = "room1";
        String userId = "user-1as12";
        UserDto user = new UserDto();
        user.setId(userId);

        // When
        roomService.addUserToRoom(roomId, user);

        // Then
        RoomState roomState = roomService.getRoomState(roomId);
        assertThat(roomState.getUsers().contains(user))
                .isTrue();
        assertThat(roomState.getUsers().size())
                .isEqualTo(1);
        assertThat(roomState.getUsers().get(0).getId())
                .isEqualTo(userId);
    }

    @Test
    void testRemoveUserFromRoom_WhenUserIsRemoved_ThenUserIsNotInRoom() {
        // Given
        String roomId = "room1";
        String userId = "user-1as12";
        UserDto user = new UserDto();
        user.setId(userId);
        roomService.addUserToRoom(roomId, user);

        // When
        roomService.removeUserFromRoom(roomId, user);

        // Then
        RoomState roomState = roomService.getRoomState(roomId);
        assertThat(roomState.getUsers().contains(user))
                .isFalse();
        assertThat(roomState.getUsers().size())
                .isEqualTo(0);
    }

    @Test
    void testSetCurrentVideo_WhenVideoIsSet_ThenVideoIsCurrentVideo() {
        // Given
        String roomId = "room1";
        Video video = new Video();
        String videoUrl = "http://example.com/video";
        video.setUrl(videoUrl);

        // When
        roomService.setCurrentVideo(roomId, video);

        // Then
        RoomState roomState = roomService.getRoomState(roomId);
        assertThat(roomState.getCurrentVideo())
                .isEqualTo(video)
                .isNotNull();
        assertThat(roomState.getCurrentVideo().getUrl())
                .isEqualTo(videoUrl)
                .isNotNull();
    }

    @Test
    void testSetIsPlaying_WhenIsPlayingIsSet_ThenIsPlayingIsUpdated() {
        // Given
        String roomId = "room1";
        Boolean isPlaying = true;

        // When
        roomService.setIsPlaying(roomId, isPlaying);

        // Then
        RoomState roomState = roomService.getRoomState(roomId);
        assertThat(roomState.getIsPlaying())
                .isTrue();
    }

    @Test
    void testSetQueue_WhenQueueIsProvided_ThenQueueIsSet() {
        // Given
        String roomId = "room1";
        List<Video> queue = new ArrayList<>();
        Video video1 = new Video();
        video1.setUrl("http://example.com/video1");
        Video video2 = new Video();
        video2.setUrl("http://example.com/video2");
        queue.add(video1);
        queue.add(video2);

        // When
        roomService.setQueue(roomId, queue);

        // Then
        RoomState roomState = roomService.getRoomState(roomId);
        assertThat(roomState).isNotNull();
        assertThat(roomState.getQueue()).isNotNull();
        assertThat(roomState.getQueue().size()).isEqualTo(2);

        assertThat(roomState.getQueue().get(0)).isEqualTo(video1);
        assertThat(roomState.getQueue().get(1)).isEqualTo(video2);
    }

    @Test
    void testSetCurrentSeek_WhenSeekIsSet_ThenSeekIsUpdated() {
        // Given
        String roomId = "room1";
        double currentSeek = 123.45;

        // When
        roomService.setCurrentSeek(roomId, currentSeek);

        // Then
        RoomState roomState = roomService.getRoomState(roomId);
        assertThat(roomState.getCurrentSeek())
                .isEqualTo(currentSeek);
    }

    @Test
    void testRemoveVideoFromQueue_WhenVideoIsRemoved_ThenVideoIsNotInQueue() {
        // Given
        String roomId = "room1";
        Video video = new Video();
        video.setUrl("http://example.com/video");
        roomService.addVideoToQueue(roomId, video);

        // When
        roomService.removeVideoFormQueue(roomId, video.getUrl());

        // Then
        RoomState roomState = roomService.getRoomState(roomId);
        assertThat(roomState.getQueue().contains(video))
                .isFalse();
        assertThat(roomState.getQueue().size())
                .isEqualTo(0);
    }


    @Test
    void testAddVideoToQueue_WhenVideoIsAdded_ThenVideoIsInQueue() {
        // Given
        String roomId = "room1";
        Video video = new Video();
        video.setUrl("http://example.com/video");

        // When
        roomService.addVideoToQueue(roomId, video);

        // Then
        RoomState roomState = roomService.getRoomState(roomId);
        assertThat(roomState.getQueue().contains(video))
                .isTrue();
        assertThat(roomState.getQueue().size())
                .isEqualTo(1);
        assertThat(roomState.getQueue().get(0))
                .isEqualTo(video);
    }

    @Test
    void testCreateRoom_WhenRoomIsCreated_ThenRoomIsPersisted() {
        // Given
        Room room = new Room();
        room.setName("test");
        given(roomRepository.save(any(Room.class))).willReturn(room);

        // When
        Room createdRoom = roomService.createRoom();

        // Then
        ArgumentCaptor<Room> roomArgumentCaptor = forClass(Room.class);
        verify(roomRepository, times(1)).save(roomArgumentCaptor.capture());

        Room capturedRoom = roomArgumentCaptor.getValue();

        assertThat(capturedRoom).usingRecursiveComparison().isEqualTo(room);

        assertThat(createdRoom).isEqualTo(room);
    }
}
