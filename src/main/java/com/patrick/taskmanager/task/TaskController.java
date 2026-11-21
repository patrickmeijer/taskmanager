package com.patrick.taskmanager.task;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
class TaskController {

    private final TaskService taskService;

    TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    Page<TaskResponseDTO> searchTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String title,
            Pageable pageable) {
        return taskService.searchTasks(status, priority, title, pageable);
    }

    @GetMapping("/admin/all-tasks")
    @PreAuthorize("hasRole('ADMIN')")
    Page<TaskResponseDTO> getAllTasksForAdmin(Pageable pageable) {
        return taskService.getAllTasksForAdmin(pageable);
    }

    @GetMapping("/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    TaskResponseDTO getTaskById(@PathVariable Long taskId) {
        return taskService.getTaskById(taskId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TaskResponseDTO createTask(@Valid @RequestBody TaskRequestDTO request) {
        return taskService.save(request);
    }

    @PutMapping("/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    TaskResponseDTO updateTask(@PathVariable Long taskId, @Valid @RequestBody TaskRequestDTO request) {
        return taskService.update(taskId, request);
    }

    @PatchMapping("/{taskId}/status")
    @ResponseStatus(HttpStatus.OK)
    TaskResponseDTO updateTaskStatus(@PathVariable Long taskId, @Valid @RequestBody TaskStatusUpdateRequestDTO request) {
        return taskService.updateStatus(taskId, request.getStatus());
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTaskById(taskId);
    }
}
