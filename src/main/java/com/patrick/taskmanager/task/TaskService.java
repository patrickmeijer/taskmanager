package com.patrick.taskmanager.task;

import com.patrick.taskmanager.exception.TaskNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task save(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, Task taskDetails) {
        Task existingTask = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setStatus(taskDetails.getStatus());
        existingTask.setPriority(taskDetails.getPriority());

        return taskRepository.save(existingTask);
    }

    public Optional<Task> getTaskById(Long taskId) {
        checkTaskExists(taskId);
        return taskRepository.findById(taskId);
    }

    public void deleteTaskById(Long taskId) {
        checkTaskExists(taskId);
        taskRepository.deleteById(taskId);
    }

    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findAllByStatus(status);
    }

    public List<Task> getTasksByPriority(String priority) {
        return taskRepository.findAllByPriority(priority);
    }

    public List<Task> getTasksByPriorityAndStatus(String priority, String status) {
        return taskRepository.findAllByPriorityAndStatus(priority, status);
    }

    public List<Task> getTasksByTitleContaining(String title) {
        return taskRepository.findAllByTitleContaining(title);
    }

    public List<Task> getTasksByOrderByCreatedAtDesc() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }
}
