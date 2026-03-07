package com.patrick.taskmanager.task;

import com.patrick.taskmanager.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Portfolio Project");
        testTask.setDescription("Testing the portfolio project");
        testTask.setPlannedAt(LocalDate.of(2026, 3, 20));
        testTask.setDeadline(LocalDate.of(2026, 3, 25));
        testTask.setStartTime(LocalDateTime.of(2026, 3, 20, 9, 0));
        testTask.setEndTime(LocalDateTime.of(2026, 3, 20, 11, 30));
    }

    @Test
    void whenDeadLineIsBeforePlannedAt_thenThrowException() {
        testTask.setDeadline(testTask.getPlannedAt().minusDays(1));
        assertThrows(IllegalStateException.class, () -> taskService.save(testTask));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void whenEndTimeIsBeforeStartTime_thenThrowException() {
        testTask.setEndTime(testTask.getStartTime().minusHours(1));
        assertThrows(IllegalStateException.class, () -> taskService.save(testTask));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void whenTaskIsValid_thenTaskIsSaved() {
        when(taskRepository.save(testTask)).thenReturn(testTask);
        Task result = taskService.save(testTask);
        assertNotNull(result);
        assertEquals("Portfolio Project", result.getTitle());
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void whenIdExistsAndDataIsValid_thenTaskIsUpdated() {
        Long taskId = 1L;
        Task updateDetails = new Task();
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setStatus("IN_PROGRESS");
        updateDetails.setPriority("HIGH");
        updateDetails.setPlannedAt(LocalDate.of(2026, 3, 20));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.updateTask(taskId, updateDetails);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals("HIGH", result.getPriority());
        assertEquals(LocalDate.of(2026, 3, 20), result.getPlannedAt());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void whenIdDoesNotExist_thenThrowNotFoundException() {
        Long nonExistingId = 999L;
        when(taskRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(nonExistingId, testTask));
        verify(taskRepository, never()).save(any(Task.class));
    }

}