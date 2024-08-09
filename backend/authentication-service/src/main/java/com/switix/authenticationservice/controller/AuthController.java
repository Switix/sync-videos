package com.switix.authenticationservice.controller;

import com.switix.authenticationservice.exception.BadCredentialsException;
import com.switix.authenticationservice.exception.ExpiredRefreshToken;
import com.switix.authenticationservice.exception.UserAlreadyExistsException;
import com.switix.authenticationservice.model.AuthRequest;
import com.switix.authenticationservice.model.AuthResponse;
import com.switix.authenticationservice.model.TokenRefreshRequest;
import com.switix.authenticationservice.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (UserAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    @PostMapping(value = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        try {
            return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
        } catch (ExpiredRefreshToken e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }
}