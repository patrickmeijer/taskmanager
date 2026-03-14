package com.patrick.taskmanager.exception;

public class TaskNotFoundException extends ResourceNotFoundException {
    public TaskNotFoundException(Long id) {
        super("Task with id " + id + " not found");
    }
}
