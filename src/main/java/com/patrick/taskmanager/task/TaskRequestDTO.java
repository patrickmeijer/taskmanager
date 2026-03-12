package com.patrick.taskmanager.task;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class TaskRequestDTO {
    @NotBlank(message = "Title is required")
    @Size(max = 100)
    private String title;

    private String description;

    private TaskStatus status;

    @NotNull(message = "Priority is mandatory")
    private TaskPriority priority;

    @Future(message = "Deadline must be in the future")
    private LocalDate plannedAt;

    @Future(message = "Deadline must be in the future")
    private LocalDate deadline;

    private Long userId;


    public TaskRequestDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getPlannedAt() {
        return plannedAt;
    }

    public void setPlannedAt(LocalDate plannedAt) {
        this.plannedAt = plannedAt;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
