package com.patrick.taskmanager.task;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String title
    ) {
        if (priority != null && status != null) {
            return taskService.getTasksByPriorityAndStatus(priority, status);
        }
        if (status != null) {
            return taskService.getTasksByStatus(status);
        }
        if (priority != null) {
            return taskService.getTasksByPriority(priority);
        }
        if  (title != null) {
            return taskService.getTasksByTitleContaining(title);
        }

        return taskService.getTasksByOrderByCreatedAtDesc();
    }

    @GetMapping("/{taskId}")
    public Optional<Task> getTaskById(@PathVariable("taskId") Long taskId) {
        return taskService.getTaskById(taskId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@RequestBody Task task) {
        task.setId(null);
        return taskService.save(task);
    }

    @PutMapping("{taskId}")
    public Task updateTask(@PathVariable("taskId") Long taskId, @RequestBody Task taskDetails) {
        return taskService.updateTask(taskId, taskDetails);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Object> deleteTask(@PathVariable("taskId") Long taskId) {
        taskService.deleteTaskById(taskId);
        return ResponseEntity.noContent().build();
    }
}
