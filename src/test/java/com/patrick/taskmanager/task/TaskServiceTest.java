package com.patrick.taskmanager.task;

import com.patrick.taskmanager.exception.notfound.TaskNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private TaskRequestDTO testRequest;
    private TaskResponseDTO testResponse;

    @BeforeEach
    void setUp() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Portfolio Project");
        testTask.setDescription("Testing the portfolio project");
        testTask.setStatus(TaskStatus.OPEN);
        testTask.setPriority(TaskPriority.HIGH);
        testTask.setPlannedAt(LocalDate.of(2026, 3, 20));
        testTask.setDeadline(LocalDate.of(2026, 3, 25));
        testTask.setStartTime(LocalDateTime.of(2026, 3, 20, 9, 0));
        testTask.setEndTime(LocalDateTime.of(2026, 3, 20, 11, 30));

        testRequest = new TaskRequestDTO();
        testRequest.setTitle("Portfolio Project");
        testRequest.setDescription("Testing the portfolio project");
        testRequest.setPriority(TaskPriority.HIGH);
        testRequest.setPlannedAt(LocalDate.of(2026, 3, 20));
        testRequest.setDeadline(LocalDate.of(2026, 3, 25));

        testResponse = new TaskResponseDTO();
        testResponse.setId(1L);
        testResponse.setTitle("Portfolio Project");
        testResponse.setDescription("Testing the portfolio project");
        testResponse.setStatus(TaskStatus.OPEN);
        testResponse.setPriority(TaskPriority.HIGH);
        testResponse.setPlannedAt(LocalDate.of(2026, 3, 20));
        testResponse.setDeadline(LocalDate.of(2026, 3, 25));
        testResponse.setStartTime(LocalDateTime.of(2026, 3, 20, 9, 0));
        testResponse.setEndTime(LocalDateTime.of(2026, 3, 20, 11, 30));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------
    // searchTasks
    // -------------------------

    @Test
    void whenAllFiltersProvided_thenReturnFilteredTasks() {
        Sort sort = Sort.by("createdAt").descending();
        when(taskRepository.findAllByFilters("testuser", TaskStatus.OPEN, TaskPriority.HIGH, "Portfolio Project", sort))
                .thenReturn(List.of(testTask));
        when(taskMapper.toResponseDTO(testTask)).thenReturn(testResponse);

        List<TaskResponseDTO> result = taskService.searchTasks(TaskStatus.OPEN, TaskPriority.HIGH, "Portfolio Project", sort);

        assertEquals(1, result.size());
        assertEquals("Portfolio Project", result.get(0).getTitle());
        verify(taskRepository, times(1)).findAllByFilters("testuser", TaskStatus.OPEN, TaskPriority.HIGH, "Portfolio Project", sort);
        verify(taskMapper, times(1)).toResponseDTO(testTask);
    }

    @Test
    void whenNoFilters_thenReturnAllTasksSorted() {
        Sort sort = Sort.by("createdAt").descending();
        when(taskRepository.findAllByFilters("testuser", null, null, null, sort)).thenReturn(List.of(testTask));
        when(taskMapper.toResponseDTO(testTask)).thenReturn(testResponse);

        List<TaskResponseDTO> result = taskService.searchTasks(null, null, null, sort);

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findAllByFilters("testuser", null, null, null, sort);
    }

    @Test
    void whenStatusProvided_thenReturnTasksByStatus() {
        Sort sort = Sort.unsorted();
        when(taskRepository.findAllByFilters("testuser", TaskStatus.OPEN, null, null, sort)).thenReturn(List.of(testTask));
        when(taskMapper.toResponseDTO(testTask)).thenReturn(testResponse);

        List<TaskResponseDTO> result = taskService.searchTasks(TaskStatus.OPEN, null, null, sort);

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findAllByFilters("testuser", TaskStatus.OPEN, null, null, sort);
    }

    @Test
    void whenPriorityProvided_thenReturnTasksByPriority() {
        Sort sort = Sort.unsorted();
        when(taskRepository.findAllByFilters("testuser", null, TaskPriority.HIGH, null, sort)).thenReturn(List.of(testTask));
        when(taskMapper.toResponseDTO(testTask)).thenReturn(testResponse);

        List<TaskResponseDTO> result = taskService.searchTasks(null, TaskPriority.HIGH, null, sort);

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findAllByFilters("testuser", null, TaskPriority.HIGH, null, sort);
    }

    @Test
    void whenPriorityAndStatusProvided_thenReturnTasksByBoth() {
        Sort sort = Sort.unsorted();
        when(taskRepository.findAllByFilters("testuser", TaskStatus.OPEN, TaskPriority.HIGH, null, sort)).thenReturn(List.of(testTask));
        when(taskMapper.toResponseDTO(testTask)).thenReturn(testResponse);

        List<TaskResponseDTO> result = taskService.searchTasks(TaskStatus.OPEN, TaskPriority.HIGH, null, sort);

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findAllByFilters("testuser", TaskStatus.OPEN, TaskPriority.HIGH, null, sort);
    }

    @Test
    void whenTitleProvided_thenReturnTasksByTitle() {
        Sort sort = Sort.unsorted();
        when(taskRepository.findAllByFilters("testuser", null, null, "Portfolio Project", sort)).thenReturn(List.of(testTask));
        when(taskMapper.toResponseDTO(testTask)).thenReturn(testResponse);

        List<TaskResponseDTO> result = taskService.searchTasks(null, null, "Portfolio Project", sort);

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findAllByFilters("testuser", null, null, "Portfolio Project", sort);
    }

    // -------------------------
    // getTaskById
    // -------------------------

    @Test
    void whenGetTaskById_andTaskExists_thenReturnResponseDTO() {
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskMapper.toResponseDTO(testTask)).thenReturn(testResponse);

        TaskResponseDTO result = taskService.getTaskById(taskId);

        assertNotNull(result);
        assertEquals("Portfolio Project", result.getTitle());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskMapper, times(1)).toResponseDTO(testTask);
    }

    @Test
    void whenGetTaskById_andTaskDoesNotExist_thenThrowNotFoundException() {
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(taskId));
        verify(taskMapper, never()).toResponseDTO(any());
    }

    // -------------------------
    // save
    // -------------------------

    @Test
    void whenTaskIsValid_thenTaskIsSaved() {
        when(taskMapper.toEntity(testRequest)).thenReturn(testTask);
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toResponseDTO(testTask)).thenReturn(testResponse);

        TaskResponseDTO result = taskService.save(testRequest);

        assertNotNull(result);
        assertEquals("Portfolio Project", result.getTitle());
        verify(taskRepository, times(1)).save(testTask);
        verify(taskMapper, times(1)).toResponseDTO(testTask);
    }

    @Test
    void whenTaskHasNoStatus_thenStatusDefaultsToOpen() {
        testTask.setStatus(null);
        when(taskMapper.toEntity(testRequest)).thenReturn(testTask);
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toResponseDTO(testTask)).thenReturn(testResponse);

        taskService.save(testRequest);

        assertEquals(TaskStatus.OPEN, testTask.getStatus());
    }

    @Test
    void whenDeadlineIsBeforePlannedAt_thenThrowException() {
        testTask.setDeadline(testTask.getPlannedAt().minusDays(1));
        when(taskMapper.toEntity(testRequest)).thenReturn(testTask);

        assertThrows(IllegalStateException.class, () -> taskService.save(testRequest));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void whenEndTimeIsBeforeStartTime_thenThrowException() {
        testTask.setEndTime(testTask.getStartTime().minusHours(1));
        when(taskMapper.toEntity(testRequest)).thenReturn(testTask);

        assertThrows(IllegalStateException.class, () -> taskService.save(testRequest));
        verify(taskRepository, never()).save(any(Task.class));
    }

    // -------------------------
    // update
    // -------------------------

    @Test
    void whenIdExistsAndDataIsValid_thenTaskIsUpdated() {
        Long taskId = 1L;
        TaskRequestDTO updateRequest = new TaskRequestDTO();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.HIGH);
        updateRequest.setPlannedAt(LocalDate.of(2026, 3, 20));
        updateRequest.setDeadline(LocalDate.of(2026, 3, 25));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toResponseDTO(testTask)).thenReturn(testResponse);

        TaskResponseDTO result = taskService.update(taskId, updateRequest);

        assertNotNull(result);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(testTask);
        verify(taskMapper, times(1)).toResponseDTO(testTask);
    }

    @Test
    void whenUpdateTask_andIdDoesNotExist_thenThrowNotFoundException() {
        Long nonExistingId = 999L;
        when(taskRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.update(nonExistingId, testRequest));
        verify(taskRepository, never()).save(any(Task.class));
    }

    // -------------------------
    // deleteTaskById
    // -------------------------

    @Test
    void whenDeleteTaskById_andTaskExists_thenTaskIsDeleted() {
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        taskService.deleteTaskById(taskId);

        verify(taskRepository, times(1)).delete(testTask);
    }

    @Test
    void whenDeleteTaskById_andTaskDoesNotExist_thenThrowNotFoundException() {
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTaskById(taskId));
        verify(taskRepository, never()).delete(any());
    }
}