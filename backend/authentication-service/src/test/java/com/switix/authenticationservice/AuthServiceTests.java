package com.switix.authenticationservice;

import com.switix.authenticationservice.exception.BadCredentialsException;
import com.switix.authenticationservice.exception.UserAlreadyExistsException;
import com.switix.authenticationservice.model.AuthRequest;
import com.switix.authenticationservice.model.AuthResponse;
import com.switix.authenticationservice.model.UserVO;
import com.switix.authenticationservice.proxy.UserProxy;
import com.switix.authenticationservice.service.AuthService;
import com.switix.authenticationservice.service.JwtUtil;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {


    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserProxy userProxy;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_whenUserExists_shouldThrowUserAlreadyExistsException() {
        // given
        AuthRequest request = new AuthRequest();
        request.setUsername("existingUser");
        request.setPassword("password");

        given(userProxy.userExists(request.getUsername())).willReturn(ResponseEntity.ok().build());

        // when & then
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void register_whenUserDoesNotExist_shouldReturnAuthResponse() {
        // given
        AuthRequest request = new AuthRequest();
        request.setUsername("newUser");
        request.setPassword("password");

        UserVO newUser = new UserVO();
        newUser.setId("userId");
        newUser.setRole("USER");

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(userProxy.userExists(request.getUsername())).willThrow(FeignException.class);
        given(userProxy.createUser(any(AuthRequest.class))).willReturn(newUser);
        given(jwtUtil.createToken("userId", "USER", "ACCESS")).willReturn(accessToken);
        given(jwtUtil.createToken("userId", "USER", "REFRESH")).willReturn(refreshToken);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");

        // when
        AuthResponse response = authService.register(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void login_whenValidCredentials_shouldReturnAuthResponse() {
        // given
        AuthRequest request = new AuthRequest();
        request.setUsername("validUser");
        request.setPassword("password");

        UserVO user = new UserVO();
        user.setId("userId");
        user.setRole("USER");
        user.setPassword("encodedPassword");

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(userProxy.getUserByUsername(request.getUsername())).willReturn(ResponseEntity.ok(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(eq("userId"), eq("USER"), eq("ACCESS"))).willReturn(accessToken);
        given(jwtUtil.createToken(eq("userId"), eq("USER"), eq("REFRESH"))).willReturn(refreshToken);

        // when
        AuthResponse response = authService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void login_whenInvalidCredentials_shouldThrowBadCredentialsException() {
        // given
        AuthRequest request = new AuthRequest();
        request.setUsername("invalidUser");
        request.setPassword("wrongPassword");

        given(userProxy.getUserByUsername(request.getUsername())).willThrow(FeignException.class);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");
    }

    @Test
    void refresh_whenTokenIsExpired_shouldThrowResponseStatusException() {
        // given
        String expiredToken = "expiredToken";

        given(jwtUtil.isExpired(expiredToken)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.refresh(expiredToken))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("EXPIRED_REFRESH_TOKEN")
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refresh_whenTokenIsValid_shouldReturnNewAuthResponse() {
        // given
        String validRefreshToken = "validRefreshToken";
        String userId = "userId";
        String userRole = "USER";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        given(jwtUtil.isExpired(validRefreshToken)).willReturn(false);
        given(jwtUtil.getUserId(validRefreshToken)).willReturn(userId);
        given(jwtUtil.getUserRole(validRefreshToken)).willReturn(userRole);
        given(jwtUtil.createToken(userId, userRole, "ACCESS")).willReturn(newAccessToken);
        given(jwtUtil.createToken(userId, userRole, "REFRESH")).willReturn(newRefreshToken);

        // when
        AuthResponse response = authService.refresh(validRefreshToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
    }
}

