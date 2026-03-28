package com.patrick.taskmanager.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperTest {

    private TaskMapper taskMapper;

    private Task testTask;
    private TaskRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper();

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
        testTask.setCreatedAt(LocalDateTime.of(2026, 3, 20, 8, 0));

        testRequest = new TaskRequestDTO();
        testRequest.setTitle("Portfolio Project");
        testRequest.setDescription("Testing the portfolio project");
        testRequest.setPriority(TaskPriority.HIGH);
        testRequest.setPlannedAt(LocalDate.of(2026, 3, 20));
        testRequest.setDeadline(LocalDate.of(2026, 3, 25));
    }

    // -------------------------
    // toResponseDTO
    // -------------------------

    @Test
    void whenTaskIsValid_thenAllFieldsMappedToResponseDTO() {
        TaskResponseDTO result = taskMapper.toResponseDTO(testTask);

        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(testTask.getTitle(), result.getTitle());
        assertEquals(testTask.getDescription(), result.getDescription());
        assertEquals(testTask.getStatus(), result.getStatus());
        assertEquals(testTask.getPriority(), result.getPriority());
        assertEquals(testTask.getPlannedAt(), result.getPlannedAt());
        assertEquals(testTask.getDeadline(), result.getDeadline());
        assertEquals(testTask.getStartTime(), result.getStartTime());
        assertEquals(testTask.getEndTime(), result.getEndTime());
        assertEquals(testTask.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    void whenTaskIsNull_thenToResponseDTOReturnsNull() {
        assertNull(taskMapper.toResponseDTO(null));
    }

    // -------------------------
    // toEntity
    // -------------------------

    @Test
    void whenRequestDTOIsValid_thenAllFieldsMappedToEntity() {
        Task result = taskMapper.toEntity(testRequest);

        assertNotNull(result);
        assertEquals(testRequest.getTitle(), result.getTitle());
        assertEquals(testRequest.getDescription(), result.getDescription());
        assertEquals(testRequest.getPriority(), result.getPriority());
        assertEquals(testRequest.getPlannedAt(), result.getPlannedAt());
        assertEquals(testRequest.getDeadline(), result.getDeadline());
    }

    @Test
    void whenRequestDTOIsNull_thenToEntityReturnsNull() {
        assertNull(taskMapper.toEntity(null));
    }

    @Test
    void whenRequestDTOIsMapped_thenIdAndTimestampsAreNotSet() {
        Task result = taskMapper.toEntity(testRequest);

        assertNull(result.getId());
        assertNull(result.getStartTime());
        assertNull(result.getEndTime());
        assertNull(result.getCreatedAt());
    }
}