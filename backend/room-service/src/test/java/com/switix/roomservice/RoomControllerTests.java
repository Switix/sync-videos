package com.switix.roomservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.switix.roomservice.controller.RoomController;
import com.switix.roomservice.exception.RoomNotFoundException;
import com.switix.roomservice.model.Room;
import com.switix.roomservice.model.RoomState;
import com.switix.roomservice.model.Video;
import com.switix.roomservice.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
class RoomControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateRoom_WhenRoomIsCreated_ThenRoomIsReturned() throws Exception {
        // Given
        Room room = new Room();
        room.setName("test");
        given(roomService.createRoom()).willReturn(room);

        // When & Then
        mockMvc.perform(post("/rooms/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test"));
    }

    @Test
    void testRoomExists_WhenRoomExists_ThenTrueIsReturned() throws Exception {
        // Given
        String roomId = "room1";
        given(roomService.getRoomById(roomId)).willReturn(new Room());

        // When & Then
        mockMvc.perform(get("/rooms/exists/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testRoomExists_WhenRoomDoesNotExist_ThenFalseIsReturned() throws Exception {
        // Given
        String roomId = "room1";
        given(roomService.getRoomById(roomId)).willThrow(new RoomNotFoundException("Room not found"));

        // When & Then
        mockMvc.perform(get("/rooms/exists/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testGetRoomState_WhenRoomIdIsProvided_ThenRoomStateIsReturned() throws Exception {
        // Given
        String roomId = "room1";
        RoomState roomState = new RoomState();
        given(roomService.getRoomState(roomId)).willReturn(roomState);

        // When & Then
        mockMvc.perform(get("/rooms/{roomId}/state", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSeek").value(0))
                .andExpect(jsonPath("$.currentVideo").isEmpty())
                .andExpect(jsonPath("$.isPlaying").value(false))
                .andExpect(jsonPath("$.users").isEmpty())
                .andExpect(jsonPath("$.queue").isEmpty());
    }

    @Test
    void testSetCurrentVideo_WhenVideoIsProvided_ThenVideoIsSet() throws Exception {
        // Given
        String roomId = "room1";
        Video video = new Video();
        video.setUrl("http://example.com/video");
        RoomState roomState = new RoomState();
        roomState.setCurrentVideo(video);
        roomState.setCurrentSeek(0);
        roomState.setIsPlaying(true);
        roomState.setQueue(new ArrayList<>());
        given(roomService.getRoomState(roomId)).willReturn(roomState);

        // Mock the service methods
        doNothing().when(roomService).setCurrentVideo(anyString(), any(Video.class));
        doNothing().when(roomService).setCurrentSeek(anyString(), anyDouble());

        // When & Then
        mockMvc.perform(post("/rooms/{roomId}/state/currentVideo", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(video)))
                .andExpect(status().isOk());

        // Verify the `POST` request and then perform the `GET` request to check the updated state
        mockMvc.perform(get("/rooms/{roomId}/state", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSeek").value(0))
                .andExpect(jsonPath("$.currentVideo.url").value(video.getUrl())); // Access the URL property
    }
}

