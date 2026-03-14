package com.patrick.taskmanager.exception;

public class UsernameAlreadyTakenException extends ConflictException {
    public UsernameAlreadyTakenException(String username) {
        super("Username '" + username + "' is already taken");
    }
}
