package com.patrick.taskmanager.exception;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found");
    }

    public UserNotFoundException(String username) {
        super("User with username " + username + " not found");
    }
}
