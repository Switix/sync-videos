package com.switix.userservice.service;


import com.switix.userservice.model.AppUser;
import com.switix.userservice.model.UserVO;
import com.switix.userservice.model.UserRegisterRequest;
import com.switix.userservice.model.UserDto;

public interface UserService {
    AppUser save(UserRegisterRequest userRegisterRequest);

    UserDto findById(Long id);

    Boolean existsByUsername(String username);

    UserVO findByUsername(String username);

}
