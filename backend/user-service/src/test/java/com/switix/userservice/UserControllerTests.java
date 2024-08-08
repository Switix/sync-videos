package com.switix.userservice;

import com.switix.userservice.controller.UserController;
import com.switix.userservice.exception.UserNotfoundException;
import com.switix.userservice.model.*;
import com.switix.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void save_shouldReturnSavedUser() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("testUser");
        request.setPassword("password");
        request.setColor("#FFFFFF");

        AppUser savedUser = new AppUser();
        savedUser.setId("123");
        savedUser.setUsername("testUser");
        savedUser.setPassword("password");
        savedUser.setUserColor("#FFFFFF");
        savedUser.setRole(UserRole.USER);

        given(userService.save(any(UserRegisterRequest.class))).willReturn(savedUser);

        // when & then
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\",\"password\":\"password\",\"color\":\"#FFFFFF\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.userColor").value("#FFFFFF"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUserDto() throws Exception {
        // given
        String userId = "123";
        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setUsername("testUser");
        userDto.setUserColor("#FFFFFF");
        userDto.setRole("USER");

        given(userService.findById(userId)).willReturn(userDto);

        // when & then
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.userColor").value("#FFFFFF"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        // given
        String userId = "123";
        given(userService.findById(userId)).willThrow(new UserNotfoundException("User not found"));

        // when & then
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("User not found"));
    }

    @Test
    void userExists_whenUserExists_shouldReturnOk() throws Exception {
        // given
        String username = "testUser";
        given(userService.existsByUsername(username)).willReturn(true);

        // when & then
        mockMvc.perform(get("/users/exists")
                        .param("username", username))
                .andExpect(status().isOk());
    }

    @Test
    void userExists_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        // given
        String username = "nonExistingUser";
        given(userService.existsByUsername(username)).willReturn(false);

        // when & then
        mockMvc.perform(get("/users/exists")
                        .param("username", username))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByUsername_whenUserExists_shouldReturnUserVO() throws Exception {
        // given
        String username = "testUser";
        UserVO userVO = new UserVO();
        userVO.setUsername(username);
        userVO.setPassword("password");
        userVO.setUserColor("#FFFFFF");
        userVO.setId("123");
        userVO.setRole("USER");

        given(userService.findByUsername(username)).willReturn(userVO);

        // when & then
        mockMvc.perform(get("/users")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.userColor").value("#FFFFFF"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getUserByUsername_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        // given
        String username = "nonExistingUser";
        given(userService.findByUsername(username)).willThrow(new UserNotfoundException("User not found"));

        // when & then
        mockMvc.perform(get("/users")
                        .param("username", username))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("User not found"));
    }
}
