package com.patrick.taskmanager.user;

import com.patrick.taskmanager.exception.EmailAlreadyExistsException;
import com.patrick.taskmanager.exception.UserNotFoundException;
import com.patrick.taskmanager.exception.UsernameAlreadyTakenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public UserResponseDTO save(UserRequestDTO request) {
        validateUniqueness(request, null);

        User user = userMapper.toEntity(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Transactional
    public UserResponseDTO update(Long userId, UserRequestDTO request) {
        User existingUser = findUserByIdOrThrow(userId);

        validateUniqueness(request, userId);
        existingUser.setUsername(request.getUsername());
        existingUser.setPassword(request.getPassword()); // TODO Bcrypt hashing
        existingUser.setEmail(request.getEmail());
        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());

        if (request.getPassword() != null &&  !request.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toResponseDTO(updatedUser);
    }

    public User findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public User findUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    public UserResponseDTO getUserById(Long userId) {
        User user = findUserByIdOrThrow(userId);
        return userMapper.toResponseDTO(user);
    }

    public void deleteUserById(Long userId) {
        userRepository.delete(findUserByIdOrThrow(userId));
    }

    private void validateUniqueness(UserRequestDTO dto, Long currentId) {
        userRepository.findByUsername(dto.getUsername())
                .filter(foundUser -> currentId == null || !currentId.equals(foundUser.getId()))
                .ifPresent(user -> { throw new UsernameAlreadyTakenException(dto.getUsername());
                });

        userRepository.findByEmail(dto.getEmail())
                .filter(foundUser -> currentId == null || !currentId.equals(foundUser.getId()))
                .ifPresent(user -> { throw new EmailAlreadyExistsException(dto.getEmail());
                });
    }
}
