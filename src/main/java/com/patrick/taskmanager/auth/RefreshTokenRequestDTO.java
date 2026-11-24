package com.patrick.taskmanager.auth;

import jakarta.validation.constraints.NotBlank;

record RefreshTokenRequestDTO(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}
