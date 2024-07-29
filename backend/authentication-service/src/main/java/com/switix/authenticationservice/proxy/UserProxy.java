package com.switix.authenticationservice.proxy;

import com.switix.authenticationservice.model.AuthRequest;
import com.switix.authenticationservice.model.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserProxy {

    @GetMapping("/users/exists")
    ResponseEntity<Void> userExists(@RequestParam("username") String username);

    @GetMapping("/users")
    ResponseEntity<UserVO> getUserByUsername(@RequestParam("username") String username);

    @PostMapping("/users")
    UserVO createUser(AuthRequest authRequest);
}
