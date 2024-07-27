package com.switix.userservice.controller;

import com.switix.userservice.exception.UserNotfoundException;
import com.switix.userservice.model.AppUser;
import com.switix.userservice.model.UserDto;
import com.switix.userservice.model.UserRegisterRequest;
import com.switix.userservice.model.UserVO;
import com.switix.userservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<AppUser> save(@RequestBody UserRegisterRequest userRegisterRequest) {
        return ResponseEntity.ok(userService.save(userRegisterRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") String id) {
        try {
            UserDto userDto = userService.findById(id);
            return ResponseEntity.ok(userDto);
        } catch (UserNotfoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Void> userExists(@RequestParam("username") String username) {
        if (userService.existsByUsername(username)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping
    public ResponseEntity<UserVO> getUserByUsername(@RequestParam("username") String username) {
        try {
            UserVO userVO = userService.findByUsername(username);
            return ResponseEntity.ok(userVO);
        } catch (UserNotfoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }

    }
}
