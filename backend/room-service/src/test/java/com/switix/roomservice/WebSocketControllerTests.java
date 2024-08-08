package com.switix.roomservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.switix.roomservice.controller.WebSocketController;
import com.switix.roomservice.model.RoomNotification;
import com.switix.roomservice.model.RoomNotificationType;
import com.switix.roomservice.model.UserDto;
import com.switix.roomservice.model.Video;
import com.switix.roomservice.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
class WebSocketControllerTests {

    @SpyBean
    RoomService roomService;
    @LocalServerPort
    private int port;
    private WebSocketStompClient stompClient;
    private BlockingQueue<RoomNotification<?>> blockingQueue;
    private ObjectMapper objectMapper;
    @Autowired
    private WebSocketController webSocketController;


    @BeforeEach
    void setUp() {
        blockingQueue = new LinkedBlockingQueue<>();
        objectMapper = new ObjectMapper();

        ReflectionTestUtils.setField(webSocketController, "roomService", roomService);

        stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    private StompSessionHandler getSessionHandler() {
        return new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/room/room1", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return RoomNotification.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        blockingQueue.offer((RoomNotification<?>) payload);
                    }
                });
            }
        };
    }

    @Test
    void testJoinRoom() throws Exception {
        UserDto user = new UserDto();
        user.setId("user1");
        user.setUsername("User 1");

        StompSession session = stompClient.connectAsync(getWsPath(), getSessionHandler()).get(1, TimeUnit.SECONDS);
        session.send("/app/room/room1/join", user);


        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    RoomNotification<?> roomNotification = blockingQueue.poll();
                    assertThat(roomNotification)
                            .isNotNull();
                    assertThat(roomNotification.getType())
                            .isEqualTo(RoomNotificationType.USER_JOINED);
                    assertThat(roomNotification.getIssuer())
                            .usingRecursiveComparison().isEqualTo(user);
                });


    }

    @Test
    void testLeaveRoom() throws Exception {
        UserDto user = new UserDto();
        user.setId("user1");
        user.setUsername("User 1");

        StompSession session = stompClient.connectAsync(getWsPath(), getSessionHandler()).get(1, TimeUnit.SECONDS);
        session.send("/app/room/room1/leave", user);

        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    RoomNotification<?> roomNotification = blockingQueue.poll();
                    assertThat(roomNotification)
                            .isNotNull();
                    assertThat(roomNotification.getType())
                            .isEqualTo(RoomNotificationType.USER_LEFT);
                    assertThat(roomNotification.getIssuer())
                            .usingRecursiveComparison().isEqualTo(user);
                });


        ArgumentCaptor<UserDto> userCaptor = ArgumentCaptor.forClass(UserDto.class);
        verify(roomService, times(1)).removeUserFromRoom(anyString(), userCaptor.capture());
        assertThat(userCaptor.getValue()).
                usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    void testAddVideo() throws Exception {
        UserDto user = new UserDto();
        user.setId("user1");
        user.setUsername("User 1");

        Video video = new Video();
        video.setUrl("http://example.com/video");

        JsonNode payload = objectMapper.createObjectNode()
                .putPOJO("user", user)
                .putPOJO("videoData", video);

        StompSession session = stompClient.connectAsync(getWsPath(), getSessionHandler()).get(1, TimeUnit.SECONDS);
        session.send("/app/room/room1/addVideo", payload);


        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    RoomNotification<?> roomNotification = blockingQueue.poll();
                    assertThat(roomNotification)
                            .isNotNull();
                    assertThat(roomNotification.getType())
                            .isEqualTo(RoomNotificationType.VIDEO_ADDED);
                    assertThat(roomNotification.getIssuer())
                            .usingRecursiveComparison().isEqualTo(user);
                    assertThat(roomNotification.getMessage())
                            .isNotNull();
                });

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(roomService, times(1)).addVideoToQueue(anyString(), videoCaptor.capture());
        assertThat(videoCaptor.getValue()).
                isNotNull();
    }


    @Test
    void testRemoveVideo() throws Exception {
        UserDto user = new UserDto();
        user.setId("user1");
        user.setUsername("User 1");

        String url = "http://example.com/video";
        JsonNode payload = objectMapper.createObjectNode()
                .putPOJO("user", user)
                .put("url", url);

        StompSession session = stompClient.connectAsync(getWsPath(), getSessionHandler()).get(1, TimeUnit.SECONDS);
        session.send("/app/room/room1/removeVideo", payload);

        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    RoomNotification<?> roomNotification = blockingQueue.poll();
                    assertThat(roomNotification)
                            .isNotNull();
                    assertThat(roomNotification.getType())
                            .isEqualTo(RoomNotificationType.VIDEO_REMOVED);
                    assertThat(roomNotification.getIssuer())
                            .usingRecursiveComparison().isEqualTo(user);
                    assertThat(roomNotification.getMessage())
                            .isEqualTo(url);
                });

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(roomService, times(1)).removeVideoFormQueue(anyString(), urlCaptor.capture());
        assertThat(urlCaptor.getValue()).isEqualTo(url);
    }

    @Test
    void testPlayVideo() throws Exception {
        UserDto user = new UserDto();
        user.setId("user1");
        user.setUsername("User 1");

        double currentSeek = 30.0;
        boolean isPlaying = true;

        JsonNode payload = objectMapper.createObjectNode()
                .putPOJO("user", user)
                .put("currentSeek", currentSeek)
                .put("isPlaying", isPlaying);

        StompSession session = stompClient.connectAsync(getWsPath(), getSessionHandler()).get(1, TimeUnit.SECONDS);
        session.send("/app/room/room1/play", payload);

        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    RoomNotification<?> roomNotification = blockingQueue.poll();
                    assertThat(roomNotification)
                            .isNotNull();
                    assertThat(roomNotification.getType())
                            .isEqualTo(RoomNotificationType.VIDEO_PLAY);
                    assertThat(roomNotification.getIssuer())
                            .usingRecursiveComparison().isEqualTo(user);
                    assertThat(roomNotification.getMessage())
                            .isEqualTo(currentSeek);
                });

        ArgumentCaptor<Double> seekCaptor = ArgumentCaptor.forClass(Double.class);
        verify(roomService, times(1)).setCurrentSeek(anyString(), seekCaptor.capture());
        assertThat(seekCaptor.getValue())
                .isEqualTo(currentSeek);

        ArgumentCaptor<Boolean> playingCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(roomService, times(1)).setIsPlaying(anyString(), playingCaptor.capture());
        assertThat(playingCaptor.getValue())
                .isTrue();
    }

    @Test
    void testPauseVideo() throws Exception {
        UserDto user = new UserDto();
        user.setId("user1");
        user.setUsername("User 1");

        double currentSeek = 30.0;
        boolean isPlaying = false;

        JsonNode payload = objectMapper.createObjectNode()
                .putPOJO("user", user)
                .put("currentSeek", currentSeek)
                .put("isPlaying", isPlaying);

        StompSession session = stompClient.connectAsync(getWsPath(), getSessionHandler()).get(1, TimeUnit.SECONDS);
        session.send("/app/room/room1/pause", payload);

        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    RoomNotification<?> roomNotification = blockingQueue.poll();
                    assertThat(roomNotification)
                            .isNotNull();
                    assertThat(roomNotification.getType())
                            .isEqualTo(RoomNotificationType.VIDEO_PAUSE);
                    assertThat(roomNotification.getIssuer())
                            .usingRecursiveComparison().isEqualTo(user);
                    assertThat(roomNotification.getMessage())
                            .isEqualTo(currentSeek);
                });


        ArgumentCaptor<Double> seekCaptor = ArgumentCaptor.forClass(Double.class);
        verify(roomService, times(1)).setCurrentSeek(anyString(), seekCaptor.capture());
        assertThat(seekCaptor.getValue())
                .isEqualTo(currentSeek);


        ArgumentCaptor<Boolean> playingCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(roomService, times(1)).setIsPlaying(anyString(), playingCaptor.capture());
        assertThat(playingCaptor.getValue())
                .isFalse();
    }

    @Test
    void testUpdateQueue() throws Exception {
        UserDto user = new UserDto();
        user.setId("user1");
        user.setUsername("User 1");

        Video video1 = new Video();
        video1.setUrl("http://example.com/video1");
        Video video2 = new Video();
        video2.setUrl("http://example.com/video2");

        List<Video> queue = List.of(video1, video2);

        JsonNode payload = objectMapper.createObjectNode()
                .putPOJO("user", user)
                .putPOJO("queue", queue);

        StompSession session = stompClient.connectAsync(getWsPath(), getSessionHandler()).get(1, TimeUnit.SECONDS);
        session.send("/app/room/room1/updateQueue", payload);

        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    RoomNotification<?> roomNotification = blockingQueue.poll();
                    assertThat(roomNotification)
                            .isNotNull();
                    assertThat(roomNotification.getType())
                            .isEqualTo(RoomNotificationType.VIDEO_MOVED);
                    assertThat(roomNotification.getIssuer())
                            .usingRecursiveComparison().isEqualTo(user);
                    assertThat(roomNotification.getMessage())
                            .isNotNull();
                });


        ArgumentCaptor<List<Video>> queueCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(roomService, times(1)).setQueue(anyString(), queueCaptor.capture());
        assertThat(queueCaptor.getValue())
                .isNotNull();
    }


    private String getWsPath() {
        return String.format("ws://localhost:%d/ws", port);
    }
}
