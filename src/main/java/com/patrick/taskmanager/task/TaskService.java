package com.patrick.taskmanager.task;

import com.patrick.taskmanager.exception.notfound.TaskNotFoundException;
import com.patrick.taskmanager.user.User;
import com.patrick.taskmanager.user.UserRole;
import com.patrick.taskmanager.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, UserService userService) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.userService = userService;
    }

    public List<TaskResponseDTO> searchTasks(TaskStatus status, TaskPriority priority, String title, Sort sort) {
        String currentUsername = getCurrentUsername();
        return taskRepository.findAllByFilters(currentUsername, status, priority, title, sort)
                .stream()
                .map(taskMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public TaskResponseDTO save(TaskRequestDTO request) {
        Task task = taskMapper.toEntity(request);

        String currentUsername = getCurrentUsername();
        User currentUser = userService.findUserByUsernameOrThrow(currentUsername);
        task.setUser(currentUser);

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.OPEN);
        }
        validateTask(task);
        Task savedTask = taskRepository.save(task);

        logger.info("Task ID '{}' ('{}') created by user '{}'", savedTask.getId(), savedTask.getTitle(), currentUsername);
        return taskMapper.toResponseDTO(savedTask);
    }

    @Transactional
    public TaskResponseDTO update(Long taskId, TaskRequestDTO request) {
        Task existingTask = findTaskByIdOrThrow(taskId);
        validateOwnership(existingTask);
        existingTask.setTitle(request.getTitle());
        existingTask.setDescription(request.getDescription());
        existingTask.setStatus(request.getStatus());
        existingTask.setPriority(request.getPriority());
        existingTask.setPlannedAt(request.getPlannedAt());
        existingTask.setDeadline(request.getDeadline());

        validateTask(existingTask);
        Task updatedTask = taskRepository.save(existingTask);

        logger.info("Task ID '{}' ('{}') updated by user '{}'", taskId, updatedTask.getTitle(), getCurrentUsername());
        return taskMapper.toResponseDTO(updatedTask);
    }

    @Transactional
    public TaskResponseDTO updateStatus(Long taskId, TaskStatus status) {
        Task existingTask = findTaskByIdOrThrow(taskId);

        validateOwnership(existingTask);
        existingTask.setStatus(status);
        Task updatedTask = taskRepository.save(existingTask);

        logger.info("Task ID '{}' ('{}') status updated to '{}' by user '{}'", taskId, updatedTask.getTitle(), status, getCurrentUsername());
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

    private Task findTaskByIdOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    public TaskResponseDTO getTaskById(Long taskId) {
        Task task = findTaskByIdOrThrow(taskId);
        validateOwnership(task);
        return taskMapper.toResponseDTO(task);
    }

    @Transactional
    public void deleteTaskById(Long taskId) {
        Task task = findTaskByIdOrThrow(taskId);
        validateOwnership(task);
        taskRepository.delete(task);

        logger.info("Task ID '{}' ('{}') has been deleted by user '{}'", taskId, task.getTitle(), getCurrentUsername());
    }

    private String getCurrentUsername() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
    }

    private void validateOwnership(Task task) {
        String currentUsername = getCurrentUsername();
        boolean isOwner = task.getUser().getUsername().equals(currentUsername);
        boolean isAdmin = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication())
                .getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), UserRole.ROLE_ADMIN.name()));


        if (!isOwner && !isAdmin) {
            logger.warn("Unauthorized access: User '{}' tried to access Task ID {} owned by '{}'", currentUsername, task.getId(), task.getUser().getUsername());
            throw new AccessDeniedException("You do not have permission to access this task");
        }
    }
}
