package com.example.BankSystem.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.BankSystem.model.Account;
import com.example.BankSystem.model.User;
import com.example.BankSystem.enums.AccountStatus;

import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {

    /**
     * Find an account by its account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Find all accounts owned by a specific user
     */
    List<Account> findByUser(User user);

    /**
     * Find all accounts owned by a specific user with a specific status
     */
    List<Account> findByUserAndStatus(User user, AccountStatus status);

    /**
     * Check if an account number already exists
     */
    boolean existsByAccountNumber(String accountNumber);

}
