package com.patrick.taskmanager.user;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#userId)")
    @ResponseStatus(HttpStatus.OK)
    UserResponseDTO getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserResponseDTO createUser(@Valid @RequestBody UserRequestDTO request) {
        return userService.save(request);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#userId)")
    @ResponseStatus(HttpStatus.OK)
    UserResponseDTO updateUser(@PathVariable Long userId, @Valid @RequestBody UserRequestDTO request) {
        return userService.update(userId, request);
    }

    @PatchMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#userId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(@PathVariable Long userId, @Valid @RequestBody ChangePasswordRequestDTO request) {
        userService.changePassword(userId, request);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
    }
}
