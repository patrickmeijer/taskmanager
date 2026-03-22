package com.patrick.taskmanager.task;

import jakarta.validation.constraints.NotNull;

public class TaskStatusUpdateRequestDTO {

    @NotNull(message = "Status is required")
    private TaskStatus status;

    public TaskStatusUpdateRequestDTO() {
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
