package com.patrick.taskmanager.task;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:title IS NULL OR t.title LIKE %:title%)")
    List<Task> findAllByFilters(
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("title") String title,
            Sort sort
    );
    List<Task> findAllByStatus(String status);
    List<Task> findAllByPriority(String priority);
    List<Task> findAllByPriorityAndStatus(String priority, String status);
    List<Task> findAllByTitleContaining(String title);
    List<Task> findAllByOrderByCreatedAtDesc();
}
