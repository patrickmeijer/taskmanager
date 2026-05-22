package com.patrick.taskmanager.task;

import com.patrick.taskmanager.user.User;
import com.patrick.taskmanager.user.UserRepository;
import com.patrick.taskmanager.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class TaskControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new  PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("taskmanager-test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    public void setup() {
        taskRepository.deleteAll(); // tasks before users because of foreign key constraint
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(UserRole.ROLE_USER);
        userRepository.save(user);

        User admin = new User();
        admin.setUsername("adminuser");
        admin.setPassword("password");
        admin.setRole(UserRole.ROLE_ADMIN);
        userRepository.save(admin);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createTask_ShouldReturnCreatedTask() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("Write documentation");
        request.setDescription("This is a description");
        request.setPriority(TaskPriority.MEDIUM);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Write documentation"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createTask_WhenDeadlineBeforePlannedAt_thenReturnBadRequest() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("Invalid task");
        request.setDescription("This is a description");
        request.setPriority(TaskPriority.MEDIUM);
        request.setPlannedAt(LocalDate.of(2026, 7, 10));
        request.setDeadline(LocalDate.of(2026, 7, 5));

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "otheruser")
    void getTaskById_WhenNotOwner_thenReturnForbidden() throws Exception {
        User owner = userRepository.findByUsername("testuser").orElseThrow();
        Task task = new Task();
        task.setTitle("Private task");
        task.setUser(owner);
        task = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/" + task.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTaskById_WhenNotLoggedIn_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "adminuser", roles = "ADMIN")
    void getTaskById_WhenAdminButNotOwner_thenReturnOk() throws Exception {
        User owner = userRepository.findByUsername("testuser").orElseThrow();
        Task task = new Task();
        task.setTitle("Private task");
        task.setUser(owner);
        task = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/" + task.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateTask_thenReturnUpdatedTask() throws Exception {
        User owner = userRepository.findByUsername("testuser").orElseThrow();
        Task task = new Task();
        task.setTitle("Old title");
        task.setUser(owner);
        task = taskRepository.save(task);

        TaskRequestDTO updateRequest = new TaskRequestDTO();
        updateRequest.setTitle("New title");
        updateRequest.setPriority(TaskPriority.LOW);

        mockMvc.perform(put("/api/tasks/" + task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"));
    }

    @Test
    @WithMockUser(username = "otheruser")
    void updateTask_WhenNotOwner_thenReturnForbidden() throws Exception {
        User owner = userRepository.findByUsername("testuser").orElseThrow();
        Task task = new Task();
        task.setTitle("Protected task");
        task.setUser(owner);
        task = taskRepository.save(task);

        TaskRequestDTO updateRequest = new TaskRequestDTO();
        updateRequest.setTitle("Hacked title");
        updateRequest.setPriority(TaskPriority.LOW);

        mockMvc.perform(put("/api/tasks/" + task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateTaskStatus_thenReturnUpdatedTaskStatus() throws Exception {
        User owner = userRepository.findByUsername("testuser").orElseThrow();
        Task task = new Task();
        task.setTitle("Patch test task");
        task.setUser(owner);
        task = taskRepository.save(task);

        TaskStatusUpdateRequestDTO updateRequest = new TaskStatusUpdateRequestDTO();
        updateRequest.setStatus(TaskStatus.COMPLETED);

        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(TaskStatus.COMPLETED, updatedTask.getStatus());
    }

    @Test
    @WithMockUser(username = "otheruser")
    void deleteTask_WhenNotOwner_thenReturnForbidden() throws Exception {
        User owner = userRepository.findByUsername("testuser").orElseThrow();
        Task task = new Task();
        task.setTitle("Safe task");
        task.setUser(owner);
        task = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + task.getId()))
                .andExpect(status().isForbidden());
        assertTrue(taskRepository.existsById(task.getId()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteTask_WhenOwner_thenReturnNoContent() throws Exception {
        User owner = userRepository.findByUsername("testuser").orElseThrow();
        Task task = new Task();
        task.setTitle("Task to delete");
        task.setUser(owner);
        task = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + task.getId()))
                .andExpect(status().isNoContent());
        assertFalse(taskRepository.existsById(task.getId()));
    }
}
