package com.patrick.taskmanager.exception.conflict;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
      super("Email address '" + email + "' is already in use");
    }
}
