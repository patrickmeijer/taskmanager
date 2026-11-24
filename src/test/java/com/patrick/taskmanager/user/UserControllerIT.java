package com.patrick.taskmanager.user;

import com.patrick.taskmanager.support.DatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class UserControllerIT {

    @Container
    static PostgreSQLContainer postgres =  new PostgreSQLContainer("postgres:16-alpine")
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
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private UserTestSupport userTestSupport;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        testUser = userTestSupport.createUser("testuser", "password", UserRole.ROLE_USER);
        otherUser = userTestSupport.createUser("otheruser", "password", UserRole.ROLE_USER);
        userTestSupport.createUser("admin", "password", UserRole.ROLE_ADMIN);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserById_WhenOwnProfile_thenReturnOk() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserById_WhenOtherUsersProfile_thenReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users/" + otherUser.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserById_WhenAdmin_thenReturnOk() throws Exception {
        mockMvc.perform(get("/api/users/" + otherUser.getId()))
                .andExpect(status().isOk());
    }
}
