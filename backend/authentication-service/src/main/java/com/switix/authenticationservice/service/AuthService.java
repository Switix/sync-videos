package com.switix.authenticationservice.service;

import com.switix.authenticationservice.exception.BadCredentialsException;
import com.switix.authenticationservice.exception.UserAlreadyExistsException;
import com.switix.authenticationservice.model.AuthRequest;
import com.switix.authenticationservice.model.AuthResponse;
import com.switix.authenticationservice.model.UserVO;
import com.switix.authenticationservice.proxy.UserProxy;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final UserProxy userProxy;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(AuthRequest request) {

        //to check for TEMP or NORMAL user
        if (request.getUsername() != null) {
            try {
                ResponseEntity<Void> response = userProxy.userExists(request.getUsername());
                //ResponseEntity<Void> response = restTemplate.getForEntity("http://user-service/users/exists?username=" + request.getUsername(), Void.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    throw new UserAlreadyExistsException("User already exists");
                }

            } catch (FeignException ignored) {
                request.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }
        UserVO registeredUser = userProxy.createUser(request);
        //UserVO registeredUser = restTemplate.postForObject("http://user-service/users", request, UserVO.class);
        String accessToken = jwtUtil.createToken(registeredUser.getId(), registeredUser.getRole(), "ACCESS");
        String refreshToken = jwtUtil.createToken(registeredUser.getId(), registeredUser.getRole(), "REFRESH");

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse login(AuthRequest request) {

        try {
            ResponseEntity<UserVO> response = userProxy.getUserByUsername(request.getUsername());
            //ResponseEntity<UserVO> response = restTemplate.getForEntity("http://user-service/users?username=" + request.getUsername(), UserVO.class);
            UserVO user = response.getBody();

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Bad credentials");
            }

            String accessToken = jwtUtil.createToken(user.getId(), user.getRole(), "ACCESS");
            String refreshToken = jwtUtil.createToken(user.getId(), user.getRole(), "REFRESH");

            return new AuthResponse(accessToken, refreshToken);
        } catch (FeignException e) {
            throw new BadCredentialsException("Bad credentials");
        }

    }

    public AuthResponse refresh(String refreshToken) {

        if (jwtUtil.isExpired(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "EXPIRED_REFRESH_TOKEN");
        }

        String userId = jwtUtil.getUserId(refreshToken);
        String userRole = jwtUtil.getUserRole(refreshToken);
        String newAccessToken = jwtUtil.createToken(userId, userRole, "ACCESS");
        String newRefreshToken = jwtUtil.createToken(userId, userRole, "REFRESH");
        return new AuthResponse(newAccessToken, newRefreshToken);
    }
}
