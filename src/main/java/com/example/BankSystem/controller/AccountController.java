package com.example.BankSystem.controller;

import com.example.BankSystem.dto.AccountDto;
import com.example.BankSystem.service.AccountService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        log.info("Request to get all accounts");
        List<AccountDto> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.canAccessAccount(#id)")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) {
        log.info("Request to get account with ID: {}", id);
        AccountDto account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.canAccessAccountByNumber(#accountNumber)")
    public ResponseEntity<AccountDto> getAccountByAccountNumber(@PathVariable String accountNumber) {
        log.info("Request to get account with account number: {}", accountNumber);
        AccountDto account = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<List<AccountDto>> getAccountsByUserId(@PathVariable Long userId) {
        log.info("Request to get accounts for user with ID: {}", userId);
        List<AccountDto> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#accountDto.userId)")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto accountDto) {
        log.info("Request to create new account for user with ID: {}", accountDto.getUserId());
        AccountDto createdAccount = accountService.createAccount(accountDto);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountDto accountDto) {
        log.info("Request to update account with ID: {}", id);
        AccountDto updatedAccount = accountService.updateAccount(id, accountDto);
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.canAccessAccount(#id)")
    public ResponseEntity<Void> closeAccount(@PathVariable Long id) {
        log.info("Request to close account with ID: {}", id);
        accountService.closeAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.canAccessAccountByNumber(#accountNumber)")
    public ResponseEntity<AccountDto> deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount) {
        log.info("Request to deposit {} to account: {}", amount, accountNumber);
        AccountDto updatedAccount = accountService.deposit(accountNumber, amount);
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.canAccessAccountByNumber(#accountNumber)")
    public ResponseEntity<AccountDto> withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount) {
        log.info("Request to withdraw {} from account: {}", amount, accountNumber);
        AccountDto updatedAccount = accountService.withdraw(accountNumber, amount);
        return ResponseEntity.ok(updatedAccount);
    }
}
