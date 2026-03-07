package com.patrick.taskmanager.config;


import com.patrick.taskmanager.task.Task;
import com.patrick.taskmanager.task.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initDatabase(TaskRepository repository) {
        return args -> {
            repository.deleteAll();
            List<Task> tasks = repository.saveAll(List.of(
                // 1. High priority with a deadline
                new Task("Groceries", "Buy milk, eggs, and bread", "OPEN", "HIGH", null, LocalDate.now().plusDays(1)),
                // 2. Advanced: Specific time block for a workshop
                new Task("Java Workshop", "Deep dive into Spring Boot 3", "OPEN", "MEDIUM",
                        LocalDate.now().plusDays(3), LocalDate.now().plusDays(3),
                        LocalDateTime.now().plusDays(3).withHour(9).withMinute(0),
                        LocalDateTime.now().plusDays(3).withHour(17).withMinute(0)),
                // 3. In progress with a long-term deadline
                new Task("Spring Boot Refactor", "Migrate entities to new date model", "IN_PROGRESS", "HIGH", LocalDate.now(), LocalDate.now().plusDays(7)),
                // 4. Completed task from yesterday
                new Task("Gym Session", "Leg day and cardio workout", "DONE", "LOW", LocalDate.now().minusDays(1), LocalDate.now().minusDays(1)),
                // 5. Planned for the future without a hard deadline yet
                new Task("Tax Return 2025", "Gather all financial documents", "OPEN", "MEDIUM", LocalDate.now().plusWeeks(2), null),

                new Task("Car Wash", "Exterior cleaning and waxing", "OPEN", "LOW", null, null),
                new Task("Doctor's Appointment", "Annual physical check-up", "OPEN", "HIGH", LocalDate.now().plusDays(5), null),
                new Task("Read Clean Code", "Read chapter 4 about formatting", "IN_PROGRESS", "LOW", null, null),
                new Task("Fix Auth Bug", "Resolve NullPointerException in Login", "DONE", "HIGH", null, LocalDate.now().minusDays(2)),
                new Task("Team Sync", "Weekly progress update", "OPEN", "MEDIUM", LocalDate.now().plusDays(1), LocalDate.now().plusDays(1))
            ));

            Long firstId = tasks.get(0).getId();
            Long lastId = tasks.get(tasks.size() - 1).getId();
            System.out.println("--- DB refreshed with 10 tasks! New ID range " + firstId + " to " + lastId + " ---");
        };
    }
}
