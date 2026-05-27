package com.patrick.taskmanager.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    private User testUser;
    private UserRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("testpassword");
        testUser.setEmail("test@email.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setRole(UserRole.ROLE_USER);

        testRequest = new UserRequestDTO();
        testRequest.setUsername("testuser");
        testRequest.setPassword("testpassword");
        testRequest.setEmail("test@email.com");
        testRequest.setFirstName("Test");
        testRequest.setLastName("User");
    }

    // -------------------------
    // toResponseDTO
    // -------------------------

    @Test
    void whenUserIsMapped_thenPasswordIsNotExposedInResponseDTO() {
        boolean hasPasswordGetter = Arrays.stream(UserResponseDTO.class.getMethods())
                .anyMatch(method -> method.getName().equals("getPassword"));

        assertFalse(hasPasswordGetter, "UserResponseDTO must not expose password field");
    }

    @Test
    void whenUserIsValid_thenAllFieldsMappedToResponseDTO() {
        UserResponseDTO result = userMapper.toResponseDTO(testUser);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFirstName(), result.getFirstName());
        assertEquals(testUser.getLastName(), result.getLastName());
        assertEquals(testUser.getCreatedAt(), result.getCreatedAt());
        assertEquals(testUser.getUpdatedAt(), result.getUpdatedAt());
        assertEquals(testUser.getRole(), result.getRole());
    }

    @Test
    void whenUserIsNull_thenToResponseDTOReturnNull() {
        assertNull(userMapper.toResponseDTO(null));
    }

    // -------------------------
    // toEntity
    // -------------------------

    @Test
    void whenRequestDTOIsValid_thenAllFieldsMappedToEntity() {
        User result =  userMapper.toEntity(testRequest);

        assertNotNull(result);
        assertEquals(testRequest.getUsername(), result.getUsername());
        assertEquals(testRequest.getPassword(), result.getPassword());
        assertEquals(testRequest.getEmail(), result.getEmail());
        assertEquals(testRequest.getFirstName(), result.getFirstName());
        assertEquals(testRequest.getLastName(), result.getLastName());
    }

    @Test
    void whenRequestDTOIsNull_thenToEntityReturnsNull() {
        assertNull(userMapper.toEntity(null));
    }

    @Test
    void whenRequestDTOIsMapped_thenSystemFieldsAreIgnored() {
        User result = userMapper.toEntity(testRequest);

        assertNull(result.getId());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
        assertEquals(UserRole.ROLE_USER, result.getRole());
    }
}