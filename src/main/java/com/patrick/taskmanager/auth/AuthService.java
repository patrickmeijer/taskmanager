package com.patrick.taskmanager.auth;

import com.patrick.taskmanager.exception.InvalidCredentialsException;
import com.patrick.taskmanager.user.User;
import com.patrick.taskmanager.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDTO login(AuthRequestDTO request, String clientIp) {
        User user = userService.findUserByUsernameOrThrow(request.getUsername());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Authentication failed: incorrect password for existing user '{}' from IP {}",
                    request.getUsername(), clientIp);
            throw new InvalidCredentialsException();
        }

        logger.info("Successful login for user {}", request.getUsername());
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new AuthResponseDTO(token);
    }
}
