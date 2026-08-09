package com.patrick.taskmanager.auth;

import com.patrick.taskmanager.exception.InvalidCredentialsException;
import com.patrick.taskmanager.exception.notfound.UserNotFoundException;
import com.patrick.taskmanager.user.User;
import com.patrick.taskmanager.user.UserRole;
import com.patrick.taskmanager.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    private AuthRequestDTO authRequest;
    private User testUser;
    private final String clientIp = "127.0.0.1";

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, jwtService, passwordEncoder, refreshTokenService, 3600000L);

        authRequest = new AuthRequestDTO("testuser", "password");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole(UserRole.ROLE_USER);
    }

    @Test
    void whenCredentialsAreValid_thenLoginSuccess() {
        String dummyAccessToken = "dummy-jwt-token";
        String dummyRefreshToken = "dummy-refresh-token";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(dummyRefreshToken);

        when(userService.findUserByUsernameOrThrow(authRequest.username())).thenReturn(testUser);
        when(passwordEncoder.matches(authRequest.password(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser.getUsername(), testUser.getRole())).thenReturn(dummyAccessToken);
        when(refreshTokenService.createRefreshToken(testUser.getId())).thenReturn(refreshToken);

        AuthResponseDTO response = authService.login(authRequest, clientIp);

        assertNotNull(response, "Login response should not be null on success");
        assertEquals(dummyAccessToken, response.accessToken());
        assertEquals(dummyRefreshToken, response.refreshToken());
        assertEquals(3600L, response.expiresInSeconds());

        verify(userService).findUserByUsernameOrThrow(authRequest.username());
        verify(passwordEncoder).matches(authRequest.password(), testUser.getPassword());
        verify(jwtService).generateToken(testUser.getUsername(), testUser.getRole());
        verify(refreshTokenService).createRefreshToken(testUser.getId());
    }

    @Test
    void whenPasswordIsIncorrect_thenThrowInvalidCredentialsException() {
        when(userService.findUserByUsernameOrThrow(authRequest.username())).thenReturn(testUser);
        when(passwordEncoder.matches(authRequest.password(), testUser.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
            () -> authService.login(authRequest, clientIp),
            "Should throw InvalidCredentialsException when password is incorrect");

        verify(jwtService, never()).generateToken(anyString(), any());
    }

    @Test
    void whenUserNotFound_thenThrowUserNotFoundException() {
        String username = authRequest.username();
        when(userService.findUserByUsernameOrThrow(username))
                .thenThrow(new UserNotFoundException(username));

        assertThrows(UserNotFoundException.class,
            () -> authService.login(authRequest, clientIp),
            "Should throw UserNotFoundException when user is not found");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
