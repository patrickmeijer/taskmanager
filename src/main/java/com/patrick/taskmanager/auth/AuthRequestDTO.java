package com.patrick.taskmanager.auth;

import jakarta.validation.constraints.NotBlank;

record AuthRequestDTO (
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password
) {}
