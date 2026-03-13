package com.patrick.taskmanager.task;

import com.patrick.taskmanager.exception.TaskNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskResponseDTO> searchTasks(TaskStatus status, TaskPriority priority, String title, Sort sort) {
        return taskRepository.findAllByFilters(status, priority, title, sort)
                .stream()
                .map(taskMapper::toResponseDTO)
                .toList();
    }

    public TaskResponseDTO save(TaskRequestDTO dto) {
        Task task = taskMapper.toEntity(dto);

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.OPEN);
        }
        validateTask(task);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }

    public TaskResponseDTO updateTask(Long taskId, TaskRequestDTO taskDetails) {
        Task existingTask = findTaskByIdOrThrow(taskId);
        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setStatus(taskDetails.getStatus());
        existingTask.setPriority(taskDetails.getPriority());
        existingTask.setPlannedAt(taskDetails.getPlannedAt());
        existingTask.setDeadline(taskDetails.getDeadline());

        validateTask(existingTask);
        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toResponseDTO(updatedTask);
    }

    private void validateTask(Task task) {
        if (task.getDeadline() != null && task.getPlannedAt() != null) {
            if (task.getDeadline().isBefore(task.getPlannedAt())) {
                throw new IllegalStateException("Deadline cannot be before planned date");
            }
        }

        if (task.getStartTime() != null && task.getEndTime() != null) {
            if (task.getEndTime().isBefore(task.getStartTime())) {
                throw new IllegalStateException("End time cannot be before start time");
            }
        }
    }

    public Task findTaskByIdOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    public TaskResponseDTO getTaskById(Long taskId) {
        Task task = findTaskByIdOrThrow(taskId);
        return taskMapper.toResponseDTO(task);
    }

    public void deleteTaskById(Long taskId) {
        taskRepository.delete(findTaskByIdOrThrow(taskId));
    }
}
