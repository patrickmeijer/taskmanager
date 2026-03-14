package com.patrick.taskmanager.user;

import com.patrick.taskmanager.exception.EmailAlreadyExistsException;
import com.patrick.taskmanager.exception.UserNotFoundException;
import com.patrick.taskmanager.exception.UsernameAlreadyTakenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    public UserResponseDTO save(UserRequestDTO dto) {
        validateUniqueness(dto, null);

        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    public UserResponseDTO updateUser(Long userId, UserRequestDTO userDetails) {
        User existingUser = findUserByIdOrThrow(userId);

        validateUniqueness(userDetails, userId);
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setPassword(userDetails.getPassword()); // TODO Bcrypt hashing
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());

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
