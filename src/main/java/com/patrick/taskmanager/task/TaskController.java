package com.patrick.taskmanager.task;

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
    public List<Task> findAll() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{taskId}")
    public Optional<Task> getTaskById(@PathVariable("taskId") Long taskId) {
        Optional<Task> task = taskService.getTaskById(taskId);
        if (task.isEmpty()) {
            throw new RuntimeException("Task with id " + taskId + " not found");
        }
        return task;
    }

    @PostMapping
    public Task save(@RequestBody Task task) {
        task.setId(null);
        return taskService.save(task);
    }
}
