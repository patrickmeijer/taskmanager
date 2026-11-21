package com.patrick.taskmanager.task;

import com.patrick.taskmanager.user.User;
import com.patrick.taskmanager.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("local")
@Order(2)
class TaskDataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(TaskDataInitializer.class);
    private final TaskRepository taskRepository;
    private final UserService userService;

    TaskDataInitializer(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        User owner = userService.findUserByUsernameOrThrow("testuser");

        taskRepository.deleteAll();

        List<Task> tasks = List.of(
                // 1. High priority with a deadline
                new Task("Groceries", "Buy milk, eggs, and bread", TaskStatus.OPEN, TaskPriority.HIGH, null, LocalDate.now().plusDays(1)),
                // 2. Advanced: Specific time block for a workshop
                new Task("Java Workshop", "Deep dive into Spring Boot 3", TaskStatus.OPEN, TaskPriority.MEDIUM,
                        LocalDate.now().plusDays(3), LocalDate.now().plusDays(3),
                        LocalDateTime.now().plusDays(3).withHour(9).withMinute(0),
                        LocalDateTime.now().plusDays(3).withHour(17).withMinute(0)),
                // 3. In progress with a long-term deadline
                new Task("Spring Boot Refactor", "Migrate entities to new date model", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, LocalDate.now(), LocalDate.now().plusDays(7)),
                // 4. Completed task from yesterday
                new Task("Gym Session", "Leg day and cardio workout", TaskStatus.COMPLETED, TaskPriority.LOW, LocalDate.now().minusDays(1), LocalDate.now().minusDays(1)),
                // 5. Planned for the future without a hard deadline yet
                new Task("Tax Return 2025", "Gather all financial documents", TaskStatus.OPEN, TaskPriority.MEDIUM, LocalDate.now().plusWeeks(2), null),

                new Task("Car Wash", "Exterior cleaning and waxing", TaskStatus.OPEN, TaskPriority.LOW, null, null),
                new Task("Doctor's Appointment", "Annual physical check-up", TaskStatus.OPEN, TaskPriority.URGENT, LocalDate.now().plusDays(5), null),
                new Task("Read Clean Code", "Read chapter 4 about formatting", TaskStatus.IN_PROGRESS, TaskPriority.LOW, null, null),
                new Task("Fix Auth Bug", "Resolve NullPointerException in Login", TaskStatus.COMPLETED, TaskPriority.HIGH, null, LocalDate.now().minusDays(2)),
                new Task("Team Sync", "Weekly progress update", TaskStatus.OPEN, TaskPriority.MEDIUM, LocalDate.now().plusDays(1), LocalDate.now().plusDays(1))
        );

        tasks.forEach(task -> task.setUser(owner));
        List<Task> savedTasks = taskRepository.saveAll(tasks);

        if (!savedTasks.isEmpty()) {
            Long firstId = savedTasks.get(0).getId();
            Long lastId = savedTasks.get(savedTasks.size() - 1).getId();
            log.info("--- DB refreshed with 10 tasks for user: {} ---", owner.getUsername());
            log.info("--- New ID range: {} to {} ---", firstId, lastId);
        }
    }
}
