package com.patrick.taskmanager.auth;

import com.patrick.taskmanager.exception.InvalidCredentialsException;
import com.patrick.taskmanager.user.User;
import com.patrick.taskmanager.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        User user = userService.findUserByUsernameOrThrow(request.getUsername());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponseDTO(token);
    }
}
