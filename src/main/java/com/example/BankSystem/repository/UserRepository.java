package com.example.BankSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BankSystem.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    /**
     * Find a user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username already exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists
     */
    boolean existsByEmail(String email);

}
