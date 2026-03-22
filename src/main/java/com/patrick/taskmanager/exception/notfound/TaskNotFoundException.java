package com.patrick.taskmanager.exception.notfound;

public class TaskNotFoundException extends ResourceNotFoundException {
    public TaskNotFoundException(Long id) {
        super("Task with id " + id + " not found");
    }
}
