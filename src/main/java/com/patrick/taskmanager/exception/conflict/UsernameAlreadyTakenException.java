package com.patrick.taskmanager.exception.conflict;

public class UsernameAlreadyTakenException extends ConflictException {
    public UsernameAlreadyTakenException(String username) {
        super("Username '" + username + "' is already taken");
    }
}
