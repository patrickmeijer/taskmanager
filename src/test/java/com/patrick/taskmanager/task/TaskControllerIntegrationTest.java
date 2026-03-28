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
        userRepository.deleteAll();
        taskRepository.deleteAll();

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(UserRole.ROLE_USER);
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
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
}
