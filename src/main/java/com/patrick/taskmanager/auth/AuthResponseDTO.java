package com.patrick.taskmanager.auth;

public record AuthResponseDTO (
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
    public AuthResponseDTO(String accessToken, String refreshToken, long expiresInSeconds) {
        this(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }
}
