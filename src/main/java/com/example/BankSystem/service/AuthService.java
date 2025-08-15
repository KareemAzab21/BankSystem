package com.example.BankSystem.service;

import com.example.BankSystem.dto.JwtAuthResponse;
import com.example.BankSystem.dto.LoginRequest;
import com.example.BankSystem.dto.SignupRequest;
import com.example.BankSystem.dto.UserDto;
import com.example.BankSystem.exception.BadRequestException;
import com.example.BankSystem.exception.UserAlreadyExistsException;
import com.example.BankSystem.model.User;
import com.example.BankSystem.repository.UserRepository;
import com.example.BankSystem.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(
            AuthenticationManager authenticationManager,
            UserService userService,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public JwtAuthResponse login(LoginRequest loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);

            // Get user details to include in response
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new BadRequestException("User not found"));

            log.info("User authenticated successfully: {}", loginRequest.getUsername());
            return new JwtAuthResponse(token, user.getId(), user.getUsername());
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw new BadRequestException("Invalid username or password");
        }
    }

    public UserDto register(SignupRequest signupRequest) {
        log.info("Registering new user: {}", signupRequest.getUsername());

        if(userRepository.findByUsername(signupRequest.getUsername()).isPresent() ) {
            throw  new UserAlreadyExistsException("Username already exists");
        }
        // Create user DTO from signup request
        UserDto userDto = UserDto.builder()
                .username(signupRequest.getUsername())
                .password(signupRequest.getPassword())
                .email(signupRequest.getEmail())
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .roles(signupRequest.getRoles() != null ? signupRequest.getRoles() : Set.of("ROLE_USER"))
                .build();

        // Create user
        UserDto createdUser = userService.createUser(userDto);
        log.info("User registered successfully: {}", createdUser.getUsername());

        return createdUser;
    }
}
