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
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerIntegrationTest {
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
}
