package com.patrick.taskmanager.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByStatus(String status);
    List<Task> findAllByPriority(String priority);
    List<Task> findAllByPriorityAndStatus(String priority, String status);
    List<Task> findAllByTitleContaining(String title);
    List<Task> findAllByOrderByCreatedAtDesc();
}
