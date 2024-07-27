package com.switix.userservice.service;


import com.switix.userservice.model.AppUser;
import com.switix.userservice.model.UserDto;
import com.switix.userservice.model.UserRegisterRequest;
import com.switix.userservice.model.UserVO;

public interface UserService {
    AppUser save(UserRegisterRequest userRegisterRequest);

    UserDto findById(String id);

    Boolean existsByUsername(String username);

    UserVO findByUsername(String username);

}
