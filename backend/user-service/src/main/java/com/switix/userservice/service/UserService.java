package com.switix.userservice.service;


import com.switix.userservice.model.AppUser;
import com.switix.userservice.model.UserDto;
import com.switix.userservice.model.UserRegisterRequest;
import com.switix.userservice.model.UserResponse;

public interface UserService {
    AppUser save(UserRegisterRequest userRegisterRequest);

    UserResponse findById(Long id);

    Boolean existsByUsername(String username);

    UserDto findByUsername(String username);

}
