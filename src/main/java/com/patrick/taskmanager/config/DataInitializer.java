package com.patrick.taskmanager.config;


import com.patrick.taskmanager.task.Task;
import com.patrick.taskmanager.task.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initDatabase(TaskRepository repository) {
        return args -> {
            repository.deleteAll();
            List<Task> tasks = repository.saveAll(List.of(
                new Task("Groceries", "Buy milk, eggs and bread", "OPEN", "HIGH", null, null),
                new Task("Spring Boot Project", "Implement custom query methods", "IN_PROGRESS", "HIGH", LocalDateTime.now(), null),
                new Task("Gym Session", "Leg day workout", "DONE", "LOW", LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(1)),
                new Task("Tax Return", "Complete the 2025 financial report", "OPEN", "MEDIUM", null, null),
                new Task("Car Wash", "Exterior and interior cleaning", "OPEN", "LOW", null, null),
                new Task("Homework", "Finish Java exercises chapter 4", "IN_PROGRESS", "MEDIUM", LocalDateTime.now(), LocalDateTime.now().plusHours(2)),
                new Task("Doctor Appointment", "Annual check-up", "OPEN", "HIGH", LocalDateTime.now().plusDays(2), null),
                new Task("Read Book", "Read 20 pages of Clean Code", "IN_PROGRESS", "LOW", null, null),
                new Task("Fix Bug #104", "Resolve the NullPointerException in Auth", "DONE", "HIGH", LocalDateTime.now().minusHours(5), LocalDateTime.now().minusHours(4)),
                new Task("Prepare Presentation", "Slides for the Monday meeting", "OPEN", "MEDIUM", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3))
            ));
            Long firstId = tasks.get(0).getId();
            Long lastId = tasks.get(tasks.size() - 1).getId();

            System.out.println("--- DB refreshed with 10 tasks! New ID range " + firstId + " to " + lastId + " ---");
        };
    }
}
