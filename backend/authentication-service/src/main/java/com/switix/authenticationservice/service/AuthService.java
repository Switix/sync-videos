package com.switix.authenticationservice.service;

import com.switix.authenticationservice.exception.BadCredentialsException;
import com.switix.authenticationservice.exception.UserAlreadyExistsException;
import com.switix.authenticationservice.model.AuthRequest;
import com.switix.authenticationservice.model.AuthResponse;
import com.switix.authenticationservice.model.UserVO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(AuthRequest request) {

        //to check for TEMP or NORMAL user
        if (request.getUsername() != null) {
            try {
                ResponseEntity<Void> response = restTemplate.getForEntity("http://user-service/users/exists?username=" + request.getUsername(), Void.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    throw new UserAlreadyExistsException("User already exists");
                }

            } catch (HttpStatusCodeException ignored) {
                request.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }

        UserVO registeredUser = restTemplate.postForObject("http://user-service/users", request, UserVO.class);
        String accessToken = jwtUtil.createToken(registeredUser.getId(), registeredUser.getRole(), "ACCESS");
        String refreshToken = jwtUtil.createToken(registeredUser.getId(), registeredUser.getRole(), "REFRESH");

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse login(AuthRequest request) {

        try {
            ResponseEntity<UserVO> response = restTemplate.getForEntity("http://user-service/users?username=" + request.getUsername(), UserVO.class);
            UserVO user = response.getBody();

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Bad credentials");
            }

            String accessToken = jwtUtil.createToken(user.getUsername(), user.getRole(), "ACCESS");
            String refreshToken = jwtUtil.createToken(user.getUsername(), user.getRole(), "REFRESH");

            return new AuthResponse(accessToken, refreshToken);
        } catch (HttpStatusCodeException e) {
            throw new BadCredentialsException("Bad credentials");
        }

    }

}
