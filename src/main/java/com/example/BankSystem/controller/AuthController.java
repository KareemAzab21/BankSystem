package com.example.BankSystem.controller;

import com.example.BankSystem.dto.JwtAuthResponse;
import com.example.BankSystem.dto.LoginRequest;
import com.example.BankSystem.dto.SignupRequest;
import com.example.BankSystem.dto.UserDto;
import com.example.BankSystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getUsername());
        JwtAuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("Signup request received for user: {}", signupRequest.getUsername());
        UserDto createdUser = authService.register(signupRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
}
