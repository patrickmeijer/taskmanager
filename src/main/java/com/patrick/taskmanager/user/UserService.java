package com.patrick.taskmanager.user;

import com.patrick.taskmanager.exception.InvalidCredentialsException;
import com.patrick.taskmanager.exception.conflict.EmailAlreadyExistsException;
import com.patrick.taskmanager.exception.conflict.UsernameAlreadyTakenException;
import com.patrick.taskmanager.exception.notfound.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, UserMapper userMapper,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        logger.info("Admin fetching paginated list of users");
        return userRepository.findAll(pageable).map(userMapper::toResponseDTO);
    }

    @Transactional
    public UserResponseDTO save(UserRequestDTO request) {
        validateUniqueness(request, null);

        User user = userMapper.toEntity(request);
        user.setRole(UserRole.ROLE_USER);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);
        logger.info("New user registered with id {} ('{}') with role {}", savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
        return userMapper.toResponseDTO(savedUser);
    }

    @Transactional
    public UserResponseDTO update(Long userId, UserRequestDTO request) {
        User existingUser = findUserByIdOrThrow(userId);

        validateUniqueness(request, userId);
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());

        User updatedUser = userRepository.save(existingUser);
        logger.info("User profile updated for user ID '{}' ('{}')", userId, updatedUser.getUsername());
        return userMapper.toResponseDTO(updatedUser);
    }

    public User findUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    public UserResponseDTO getUserById(Long userId) {
        User user = findUserByIdOrThrow(userId);
        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequestDTO request) {
        User user = findUserByIdOrThrow(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            logger.warn("Failed password change attempt for user ID '{}' ('{}')", userId, user.getUsername());
            throw new InvalidCredentialsException();
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for user ID '{}' ('{}')", userId, user.getUsername());
    }

    @Transactional
    public void deleteUserById(Long userId) {
        User user = findUserByIdOrThrow(userId);
        userRepository.delete(user);
        logger.warn("User with id '{}' ('{}') was deleted", userId, user.getUsername());
    }

    public boolean isSelf(Long userId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findUserByIdOrThrow(userId);
        return user.getUsername().equals(currentUsername);
    }

    private User findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void validateUniqueness(UserRequestDTO request, Long currentId) {
        userRepository.findByUsername(request.getUsername())
                .filter(foundUser -> currentId == null || !currentId.equals(foundUser.getId()))
                .ifPresent(user -> { throw new UsernameAlreadyTakenException(request.getUsername());
                });

        userRepository.findByEmail(request.getEmail())
                .filter(foundUser -> currentId == null || !currentId.equals(foundUser.getId()))
                .ifPresent(user -> { throw new EmailAlreadyExistsException(request.getEmail());
                });
    }
}
