package com.example.BankSystem.service;

import com.example.BankSystem.dto.UserDto;
import com.example.BankSystem.exception.ResourceNotFoundException;
import com.example.BankSystem.exception.UserAlreadyExistsException;
import com.example.BankSystem.model.Role;
import com.example.BankSystem.model.User;
import com.example.BankSystem.repository.RoleRepository;
import com.example.BankSystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return convertToDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return convertToDto(user);
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Creating new user with username: {}", userDto.getUsername());

        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + userDto.getUsername());
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + userDto.getEmail());
        }

        User user = convertToEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Assign default role if none provided
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
            user.setRoles(Set.of(userRole));
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return convertToDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check if username is being changed and if it already exists
        if (!existingUser.getUsername().equals(userDto.getUsername()) &&
                userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + userDto.getUsername());
        }

        // Check if email is being changed and if it already exists
        if (!existingUser.getEmail().equals(userDto.getEmail()) &&
                userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + userDto.getEmail());
        }

        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());

        // Only update password if provided
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        // Update roles if provided
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : userDto.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
                roles.add(role);
            }
            existingUser.setRoles(roles);
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully");
        return convertToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully");
    }

    // Helper methods for DTO conversion
    private UserDto convertToDto(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private User convertToEntity(UserDto userDto) {
        Set<Role> roles = new HashSet<>();
        if (userDto.getRoles() != null) {
            for (String roleName : userDto.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
                roles.add(role);
            }
        }

        return User.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .password(userDto.getPassword()) // Will be encoded before saving
                .email(userDto.getEmail())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .roles(roles)
                .build();
    }
}
