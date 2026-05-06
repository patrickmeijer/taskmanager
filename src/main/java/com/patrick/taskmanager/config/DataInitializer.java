package com.patrick.taskmanager.config;


import com.patrick.taskmanager.task.Task;
import com.patrick.taskmanager.task.TaskPriority;
import com.patrick.taskmanager.task.TaskRepository;
import com.patrick.taskmanager.task.TaskStatus;
import com.patrick.taskmanager.user.User;
import com.patrick.taskmanager.user.UserRepository;
import com.patrick.taskmanager.user.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("!test")
public class DataInitializer {
    @Bean
    CommandLineRunner initDatabase(TaskRepository taskRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            taskRepository.deleteAll();
            userRepository.deleteAll();

            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole(UserRole.ROLE_ADMIN);
            User savedAdmin = userRepository.save(adminUser);

            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword(passwordEncoder.encode("user123"));
            testUser.setEmail("test@test.com");
            User savedUser = userRepository.save(testUser);

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

            tasks.forEach(task -> task.setUser(savedUser));
            List<Task> savedTasks = taskRepository.saveAll(tasks);

            if (!savedTasks.isEmpty()) {
                Long firstId = savedTasks.get(0).getId();
                Long lastId = savedTasks.get(savedTasks.size() - 1).getId();
                System.out.println("--- DB refreshed with 10 tasks for user: " + savedUser.getUsername() + " ---");
                System.out.println("--- New ID range: " + firstId + " to " + lastId + " ---");
            }
        };
    }
}
