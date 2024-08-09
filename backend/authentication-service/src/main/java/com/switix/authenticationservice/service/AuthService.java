package com.switix.authenticationservice.service;

import com.switix.authenticationservice.exception.BadCredentialsException;
import com.switix.authenticationservice.exception.ExpiredRefreshToken;
import com.switix.authenticationservice.exception.UserAlreadyExistsException;
import com.switix.authenticationservice.model.AuthRequest;
import com.switix.authenticationservice.model.AuthResponse;
import com.switix.authenticationservice.model.UserVO;
import com.switix.authenticationservice.proxy.UserProxy;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserProxy userProxy;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(AuthRequest request) {

        //to check for TEMP or NORMAL user
        if (request.getUsername() != null) {
            try {
                ResponseEntity<Void> response = userProxy.userExists(request.getUsername());

                if (response.getStatusCode().is2xxSuccessful()) {
                    throw new UserAlreadyExistsException("User already exists");
                }

            } catch (FeignException ignored) {
                request.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }
        UserVO registeredUser = userProxy.createUser(request);

        String accessToken = jwtUtil.createToken(registeredUser.getId(), registeredUser.getRole(), "ACCESS");
        String refreshToken = jwtUtil.createToken(registeredUser.getId(), registeredUser.getRole(), "REFRESH");

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse login(AuthRequest request) {

        try {
            ResponseEntity<UserVO> response = userProxy.getUserByUsername(request.getUsername());
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
            throw new ExpiredRefreshToken("EXPIRED_REFRESH_TOKEN");
        }

        String userId = jwtUtil.getUserId(refreshToken);
        String userRole = jwtUtil.getUserRole(refreshToken);
        String newAccessToken = jwtUtil.createToken(userId, userRole, "ACCESS");
        String newRefreshToken = jwtUtil.createToken(userId, userRole, "REFRESH");
        return new AuthResponse(newAccessToken, newRefreshToken);
    }
}
