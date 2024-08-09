package com.switix.authenticationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.switix.authenticationservice.controller.AuthController;
import com.switix.authenticationservice.exception.BadCredentialsException;
import com.switix.authenticationservice.exception.UserAlreadyExistsException;
import com.switix.authenticationservice.model.AuthRequest;
import com.switix.authenticationservice.model.AuthResponse;
import com.switix.authenticationservice.model.TokenRefreshRequest;
import com.switix.authenticationservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_whenUserAlreadyExists_shouldReturnConflict() throws Exception {
        // given
        AuthRequest request = new AuthRequest();
        request.setUsername("existingUser");
        request.setPassword("password");

        given(authService.register(any(AuthRequest.class)))
                .willThrow(new UserAlreadyExistsException("User already exists"));

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_whenValidRequest_shouldReturnAuthResponse() throws Exception {
        // given
        AuthRequest request = new AuthRequest();
        request.setUsername("newUser");
        request.setPassword("password");
        AuthResponse response = new AuthResponse("accessToken", "refreshToken");

        given(authService.register(any(AuthRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    void login_whenBadCredentials_shouldReturnUnauthorized() throws Exception {
        // given
        AuthRequest request = new AuthRequest();
        request.setUsername("invalidUser");
        request.setPassword("wrongPassword");
        given(authService.login(any(AuthRequest.class))).willThrow(new BadCredentialsException("Bad credentials"));

        // when & them
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_whenValidRequest_shouldReturnAuthResponse() throws Exception {
        // given
        AuthRequest request = new AuthRequest();
        request.setUsername("validUser");
        request.setPassword("password");
        request.setColor("#123456");
        AuthResponse response = new AuthResponse("accessToken", "refreshToken");

        given(authService.login(any(AuthRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    void refresh_whenValidRequest_shouldReturnAuthResponse() throws Exception {
        // given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("validRefreshToken");
        AuthResponse response = new AuthResponse("newAccessToken", "newRefreshToken");

        given(authService.refresh(request.getRefreshToken())).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));
    }

    @Test
    void refresh_whenTokenExpired_shouldReturnUnauthorized() throws Exception {
        // given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("expiredToken");
        given(authService.refresh(request.getRefreshToken())).willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "EXPIRED_REFRESH_TOKEN"));

        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
