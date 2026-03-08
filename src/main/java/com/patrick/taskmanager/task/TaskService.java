package com.patrick.taskmanager.task;

import com.patrick.taskmanager.exception.TaskNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void checkTaskExists(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
    }

    public List<Task> searchTasks(TaskStatus status, TaskPriority priority, String title, Sort sort) {
        return taskRepository.findAllByFilters(status, priority, title, sort);
    }

    public Task save(Task task) {
        validateTask(task);
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, Task taskDetails) {
        Task existingTask = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setStatus(taskDetails.getStatus());
        existingTask.setPriority(taskDetails.getPriority());

        validateTask(existingTask);
        return taskRepository.save(existingTask);
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

    public Optional<Task> getTaskById(Long taskId) {
        checkTaskExists(taskId);
        return taskRepository.findById(taskId);
    }

    public void deleteTaskById(Long taskId) {
        checkTaskExists(taskId);
        taskRepository.deleteById(taskId);
    }

    public List<Task> getTasksByOrderByCreatedAtDesc() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }
}
