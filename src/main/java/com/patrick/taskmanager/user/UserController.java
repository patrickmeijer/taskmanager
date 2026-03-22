package com.patrick.taskmanager.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#userId)")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(@Valid @RequestBody UserRequestDTO request) {
        return userService.save(request);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#userId)")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO updateUser(@PathVariable Long userId, @Valid @RequestBody UserRequestDTO request) {
        return userService.update(userId, request);
    }

    @PatchMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#userId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable Long userId, @Valid @RequestBody ChangePasswordRequestDTO request) {
        userService.changePassword(userId, request);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
    }
}
