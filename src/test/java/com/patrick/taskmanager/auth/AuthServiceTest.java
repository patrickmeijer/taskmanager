package com.patrick.taskmanager.auth;

import com.patrick.taskmanager.exception.InvalidCredentialsException;
import com.patrick.taskmanager.exception.notfound.UserNotFoundException;
import com.patrick.taskmanager.user.User;
import com.patrick.taskmanager.user.UserRole;
import com.patrick.taskmanager.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @InjectMocks
    private AuthService authService;

    private AuthRequestDTO authRequest;
    private User testUser;
    private final String clientIp = "127.0.0.1";

    @BeforeEach
    void setUp() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn("testuser");
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        authRequest = new AuthRequestDTO();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole(UserRole.ROLE_USER);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void whenCredentialsAreValid_thenLoginSuccess() {
        String dummyToken = "dummy-jwt-token";
        when(userService.findUserByUsernameOrThrow(authRequest.getUsername())).thenReturn(testUser);
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser.getUsername(), testUser.getRole())).thenReturn(dummyToken);

        AuthResponseDTO response = authService.login(authRequest, clientIp);

        assertNotNull(response, "Login response should not be null on success");
        assertEquals(dummyToken, response.getToken(), "Response token should match the generated token");
        verify(userService).findUserByUsernameOrThrow(authRequest.getUsername());
        verify(passwordEncoder).matches(authRequest.getPassword(), testUser.getPassword());
        verify(jwtService).generateToken(testUser.getUsername(), testUser.getRole());
    }

    @Test
    void whenPasswordIsIncorrect_thenThrowInvalidCredentialsException() {
        when(userService.findUserByUsernameOrThrow(authRequest.getUsername())).thenReturn(testUser);
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
            () -> authService.login(authRequest, clientIp),
            "Should throw InvalidCredentialsException when password is incorrect");

        verify(jwtService, never()).generateToken(anyString(), any());
    }

    @Test
    void whenUserNotFound_thenThrowUserNotFoundException() {
        String username = authRequest.getUsername();
        when(userService.findUserByUsernameOrThrow(username))
                .thenThrow(new UserNotFoundException(username));

        assertThrows(UserNotFoundException.class,
            () -> authService.login(authRequest, clientIp),
            "Should throw UserNotFoundException when user is not found");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
