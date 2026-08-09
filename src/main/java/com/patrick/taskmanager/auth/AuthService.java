package com.patrick.taskmanager.auth;

import com.patrick.taskmanager.exception.InvalidCredentialsException;
import com.patrick.taskmanager.exception.InvalidRefreshTokenException;
import com.patrick.taskmanager.user.User;
import com.patrick.taskmanager.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final long accessTokenExpirationMs;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public AuthResponseDTO login(AuthRequestDTO request, String clientIp) {
        User user = userService.findUserByUsernameOrThrow(request.username());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            logger.warn("Authentication failed: incorrect password for existing user '{}' from IP {}",
                    request.username(), clientIp);
            throw new InvalidCredentialsException();
        }

        logger.info("Successful login for user {}", request.username());

        String accessToken = jwtService.generateToken(user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return buildAuthResponse(accessToken, refreshToken.getToken());
    }

    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(oldToken -> {
                    User user = oldToken.getUser();
                    RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(oldToken);
                    String newAccessToken = jwtService.generateToken(user.getUsername(), user.getRole());

                    return buildAuthResponse(newAccessToken, newRefreshToken.getToken());
                })
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));
    }

    private AuthResponseDTO buildAuthResponse(String accessToken, String refreshToken) {
        return new AuthResponseDTO(accessToken, refreshToken, accessTokenExpirationMs / 1000);
    }
}
