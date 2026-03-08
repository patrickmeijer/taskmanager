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
import java.util.List;
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

    // ------------------------------
    // getAllTasks
    // ------------------------------

    @Test
    void whenGetAllTasks_thenReturnAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(testTask));

        List<Task> result = taskService.getAllTasks();

        assertEquals(1, result.size());
        assertEquals("Portfolio Project", result.get(0).getTitle());
        verify(taskRepository, times(1)).findAll();
    }

    // ------------------------------
    // getTaskById
    // ------------------------------

    @Test
    void whenGetTaskById_andTaskExists_thenReturnTask() {
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        Optional<Task> result = taskService.getTaskById(taskId);

        assertTrue(result.isPresent());
        assertEquals("Portfolio Project", result.get().getTitle());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void whenGetTaskById_andTaskDoesNotExist_thenThrowNotFoundException() {
        Long taskId = 999L;
        when(taskRepository.existsById(taskId)).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(taskId));
        verify(taskRepository, never()).findById(anyLong());
    }

    // ------------------------------
    // save
    // ------------------------------

    @Test
    void whenTaskIsValid_thenTaskIsSaved() {
        when(taskRepository.save(testTask)).thenReturn(testTask);

        Task result = taskService.save(testTask);

        assertNotNull(result);
        assertEquals("Portfolio Project", result.getTitle());
        verify(taskRepository, times(1)).save(testTask);
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

    // ------------------------------
    // updateTask
    // ------------------------------

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

    // ------------------------------
    // deleteTaskById
    // ------------------------------

    @Test
    void whenDeleteTaskById_andTaskExists_thenTaskIsDeleted() {
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);

        taskService.deleteTaskById(taskId);
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void whenDeleteTaskById_andTaskDoesNotExist_thenThrowNotFoundException() {
        Long taskId = 999L;
        when(taskRepository.existsById(taskId)).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTaskById(taskId));
        verify(taskRepository, never()).deleteById(anyLong());
    }

    // ------------------------------
    // getTasksByStatus
    // ------------------------------

    @Test
    void whenGetTasksByStatus_thenReturnMatchingTasks() {
        when(taskRepository.findAllByStatus("IN_PROGRESS")).thenReturn(List.of(testTask));

        List<Task> result =  taskService.getTasksByStatus("IN_PROGRESS");

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findAllByStatus("IN_PROGRESS");
    }

    // ------------------------------
    // getTasksByPriority
    // ------------------------------

    @Test
    void whenGetTasksByPriority_thenReturnMatchingTasks() {
        when(taskRepository.findAllByPriority("HIGH")).thenReturn(List.of(testTask));

        List<Task> result =  taskService.getTasksByPriority("HIGH");

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findAllByPriority("HIGH");
    }
}