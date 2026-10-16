package com.patrick.taskmanager.auth;

import com.patrick.taskmanager.exception.InvalidRefreshTokenException;
import com.patrick.taskmanager.exception.notfound.UserNotFoundException;
import com.patrick.taskmanager.user.User;
import com.patrick.taskmanager.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Iterable<RefreshToken> existingTokens = refreshTokenRepository.findAllByUserId(user.getId());
        refreshTokenRepository.deleteAll(existingTokens);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUserId(user.getId());

        Instant expiry = Instant.now().plusMillis(refreshTokenExpirationMs);
        refreshToken.setExpiryDate(expiry);
        refreshToken.setRevoked(false);
        refreshToken.setTimeToLive(refreshTokenExpirationMs);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now()) || token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw new InvalidRefreshTokenException("Refresh token was expired or revoked. Please log in again.");
        }
        return token;
    }

    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(oldToken.getUserId());
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findById(token);
    }
}
