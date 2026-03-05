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
        return taskService.getTaskById(taskId);
    }

    @PostMapping
    public Task save(@RequestBody Task task) {
        task.setId(null);
        return taskService.save(task);
    }

    @PutMapping("{taskId}")
    public Task updateTask(@PathVariable("taskId") Long taskId, @RequestBody Task task) {
        task.setId(taskId);
        return taskService.save(task);
    }

    @DeleteMapping("/{taskId}")
    public String deleteTask(@PathVariable("taskId") Long taskId) {
        taskService.deleteTaskById(taskId);
        return "Task with id " + taskId + " was deleted";
    }
}
