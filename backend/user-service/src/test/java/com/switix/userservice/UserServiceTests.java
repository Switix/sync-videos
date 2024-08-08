package com.switix.userservice;

import com.switix.userservice.exception.UserNotfoundException;
import com.switix.userservice.model.*;
import com.switix.userservice.repository.AppUserRepository;
import com.switix.userservice.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void save_whenUsernameAndPasswordAreProvided_thenCreateUser() {
        // given
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("testUser");
        request.setPassword("password");
        request.setColor("#FFFFFF");

        AppUser savedUser = new AppUser();
        savedUser.setId(UUID.randomUUID().toString());
        savedUser.setUsername(request.getUsername());
        savedUser.setPassword(request.getPassword());
        savedUser.setUserColor(request.getColor());
        savedUser.setRole(UserRole.USER);

        given(userRepository.save(any(AppUser.class))).willReturn(savedUser);

        // when
        AppUser result = userService.save(request);

        // then
        assertThat(result.getUsername()).isEqualTo("testUser");
        assertThat(result.getUserColor()).isEqualTo("#FFFFFF");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        AppUser capturedUser = userCaptor.getValue();

        assertThat(capturedUser.getUsername()).isEqualTo("testUser");
        assertThat(capturedUser.getUserColor()).isEqualTo("#FFFFFF");
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void save_whenUsernameAndPasswordAreNotProvided_shouldCreateTemporaryUser() {
        // given
        UserRegisterRequest request = new UserRegisterRequest();

        AppUser tempUser = new AppUser();
        tempUser.setId(UUID.randomUUID().toString());
        tempUser.setUsername("user-12345678");
        tempUser.setUserColor("#123456");
        tempUser.setRole(UserRole.TEMPORARY_USER);

        given(userRepository.save(any(AppUser.class))).willReturn(tempUser);

        // when
        AppUser result = userService.save(request);

        // then
        assertThat(result.getUsername()).startsWith("user-");
        assertThat(result.getUserColor()).startsWith("#");
        assertThat(result.getRole()).isEqualTo(UserRole.TEMPORARY_USER);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        AppUser capturedUser = userCaptor.getValue();

        assertThat(capturedUser.getUsername()).startsWith("user-");
        assertThat(capturedUser.getUserColor()).startsWith("#");
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.TEMPORARY_USER);
    }

    @Test
    void findById_whenUserExists_shouldReturnUserDto() {
        // given
        String userId = UUID.randomUUID().toString();
        AppUser appUser = new AppUser();
        appUser.setId(userId);
        appUser.setUsername("testUser");
        appUser.setUserColor("#FFFFFF");
        appUser.setRole(UserRole.USER);

        given(userRepository.findById(userId)).willReturn(Optional.of(appUser));

        // when
        UserDto result = userService.findById(userId);

        // then
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testUser");
        assertThat(result.getUserColor()).isEqualTo("#FFFFFF");
        assertThat(result.getRole()).isEqualTo(UserRole.USER.toString());
    }

    @Test
    void findById_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        // given
        String userId = UUID.randomUUID().toString();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotfoundException.class, () -> userService.findById(userId));
    }

    @Test
    void existsByUsername_shouldReturnTrueIfUserExists() {
        // given
        String username = "testUser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // when
        Boolean result = userService.existsByUsername(username);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalseIfUserDoesNotExist() {
        // given
        String username = "nonExistingUser";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        // when
        Boolean result = userService.existsByUsername(username);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void findByUsername_whenUserExists_shouldReturnUserVO() {
        // given
        String username = "testUser";
        AppUser appUser = new AppUser();
        appUser.setId(UUID.randomUUID().toString());
        appUser.setUsername(username);
        appUser.setPassword("password");
        appUser.setUserColor("#FFFFFF");
        appUser.setRole(UserRole.USER);

        given(userRepository.findByUsername(username)).willReturn(Optional.of(appUser));

        // when
        UserVO result = userService.findByUsername(username);

        // then
        assertThat(result.getUsername()).isEqualTo("testUser");
        assertThat(result.getPassword()).isEqualTo("password");
        assertThat(result.getUserColor()).isEqualTo("#FFFFFF");
        assertThat(result.getRole()).isEqualTo(UserRole.USER.toString());
    }

    @Test
    void findByUsername_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        // given
        String username = "nonExistingUser";
        given(userRepository.findByUsername(username)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotfoundException.class, () -> userService.findByUsername(username));
    }
}