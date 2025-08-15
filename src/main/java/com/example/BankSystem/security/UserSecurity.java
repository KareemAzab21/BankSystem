package com.example.BankSystem.security;

import com.example.BankSystem.model.User;
import com.example.BankSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    @Autowired
    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return userRepository.findById(userId)
                .map(user -> user.getUsername().equals(currentUsername))
                .orElse(false);
    }

    public boolean isCurrentUsername(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return currentUsername.equals(username);
    }
}
