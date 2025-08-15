package com.example.BankSystem.security;

import com.example.BankSystem.model.Account;
import com.example.BankSystem.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("accountSecurity")
public class AccountSecurity {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountSecurity(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public boolean canAccessAccount(Long accountId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return accountRepository.findById(accountId)
                .map(account -> account.getUser().getUsername().equals(currentUsername))
                .orElse(false);
    }

    public boolean canAccessAccountByNumber(String accountNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> account.getUser().getUsername().equals(currentUsername))
                .orElse(false);
    }

    // Additional helper method to check if user can access accounts for transfers
    public boolean canTransferFromAccount(String accountNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> account.getUser().getUsername().equals(currentUsername))
                .orElse(false);
    }

    // Method to check if admin or account owner
    public boolean isAdminOrAccountOwner(String accountNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user has admin role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return true;
        }

        // If not admin, check if user is the account owner
        String currentUsername = authentication.getName();
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> account.getUser().getUsername().equals(currentUsername))
                .orElse(false);
    }
}
