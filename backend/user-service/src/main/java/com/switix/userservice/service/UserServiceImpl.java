package com.switix.userservice.service;

import com.switix.userservice.exception.UserNotfoundException;
import com.switix.userservice.model.*;
import com.switix.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public AppUser save(UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest.getUsername() == null || userRegisterRequest.getPassword() == null) {
            return createTemporaryUser();
        } else {
            return createUser(userRegisterRequest);
        }
    }

    @Override
    public UserResponse findById(Long id) {

        AppUser appUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotfoundException("User not found"));
        UserResponse userResponse = new UserResponse();
        userResponse.setUsername(appUser.getUsername());
        userResponse.setUserColor(appUser.getUserColor());
        userResponse.setId(appUser.getId().toString());
        userResponse.setRole(appUser.getRole().toString());
        return userResponse;
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public UserDto findByUsername(String username) {
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotfoundException("User not found"));
        UserDto userDto = new UserDto();
        userDto.setUsername(appUser.getUsername());
        userDto.setPassword(appUser.getPassword());
        userDto.setUserColor(appUser.getUserColor());
        userDto.setId(appUser.getId().toString());
        userDto.setRole(appUser.getRole().toString());
        return userDto;
    }

    private AppUser createUser(UserRegisterRequest userRegisterRequest) {
        AppUser appUser = new AppUser();
        appUser.setUsername(userRegisterRequest.getUsername());
        appUser.setPassword(userRegisterRequest.getPassword());
        appUser.setCreatedAt(LocalDateTime.now());
        appUser.setUserColor(generateRandomColor());
        appUser.setRole(UserRole.USER);
        return userRepository.save(appUser);
    }

    private AppUser createTemporaryUser() {
        AppUser tempUser = new AppUser();
        tempUser.setUsername(generateRandomUsername());
        tempUser.setCreatedAt(LocalDateTime.now());
        tempUser.setUserColor(generateRandomColor());
        tempUser.setRole(UserRole.TEMPORARY_USER);
        return userRepository.save(tempUser);
    }

    private String generateRandomUsername() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateRandomColor() {
        return String.format("#%06x", (int) (Math.random() * 0xFFFFFF));
    }
}
