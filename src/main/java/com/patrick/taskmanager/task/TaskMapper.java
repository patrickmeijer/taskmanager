package com.patrick.taskmanager.task;

import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponseDTO toResponseDTO(Task task) {
        if (task == null) return null;

        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setPlannedAt(task.getPlannedAt());
        dto.setDeadline(task.getDeadline());
        dto.setStartTime(task.getStartTime());
        dto.setEndTime(task.getEndTime());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        if (task.getUser() != null) {
            dto.setUserId(task.getUser().getId());
        }

        return dto;
    }

    public Task toEntity(TaskRequestDTO dto) {
        if (dto == null) return null;

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setPlannedAt(dto.getPlannedAt());
        task.setDeadline(dto.getDeadline());
        task.setUser(null);

        return task;
    }
}
