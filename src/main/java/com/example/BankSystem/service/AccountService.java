package com.example.BankSystem.service;

import com.example.BankSystem.dto.AccountDto;
import com.example.BankSystem.exception.AccountClosedException;
import com.example.BankSystem.exception.BadRequestException;
import com.example.BankSystem.exception.ResourceNotFoundException;
import com.example.BankSystem.model.Account;
import com.example.BankSystem.model.User;
import com.example.BankSystem.enums.AccountStatus;
import com.example.BankSystem.enums.AccountType;
import com.example.BankSystem.repository.AccountRepository;
import com.example.BankSystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAllAccounts() {
        log.info("Fetching all accounts");
        return accountRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountById(Long id) {
        log.info("Fetching account with ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        return convertToDto(account);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountByAccountNumber(String accountNumber) {
        log.info("Fetching account with account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        return convertToDto(account);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByUserId(Long userId) {
        log.info("Fetching accounts for user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return accountRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountDto createAccount(AccountDto accountDto) {
        log.info("Creating new account for user with ID: {}", accountDto.getUserId());

        User user = userRepository.findById(accountDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", accountDto.getUserId()));

        // Generate unique account number
        String accountNumber = generateAccountNumber();
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            accountNumber = generateAccountNumber();
        }

        // Validate account type
        AccountType accountType;
        try {
            accountType = AccountType.valueOf(accountDto.getAccountType());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid account type: " + accountDto.getAccountType());
        }

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .user(user)
                .balance(accountDto.getBalance() != null ? accountDto.getBalance() : BigDecimal.ZERO)
                .accountType(accountType)
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with account number: {}", savedAccount.getAccountNumber());
        return convertToDto(savedAccount);
    }

    @Transactional
    public AccountDto updateAccount(Long id, AccountDto accountDto) {
        log.info("Updating account with ID: {}", id);

        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        // Update account type if provided
        if (accountDto.getAccountType() != null) {
            try {
                AccountType accountType = AccountType.valueOf(accountDto.getAccountType());
                existingAccount.setAccountType(accountType);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid account type: " + accountDto.getAccountType());
            }
        }

        // Update status if provided
        if (accountDto.getStatus() != null) {
            try {
                AccountStatus status = AccountStatus.valueOf(accountDto.getStatus());
                existingAccount.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid account status: " + accountDto.getStatus());
            }
        }

        Account updatedAccount = accountRepository.save(existingAccount);
        log.info("Account updated successfully");
        return convertToDto(updatedAccount);
    }

    @Transactional
    public void closeAccount(Long id) {
        log.info("Closing account with ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
        log.info("Account closed successfully");
    }

    @Transactional
    public AccountDto deposit(String accountNumber, BigDecimal amount) {
        log.info("Depositing {} to account: {}", amount, accountNumber);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Deposit amount must be positive");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountClosedException("Cannot deposit to a non-active account");
        }

        account.setBalance(account.getBalance().add(amount));
        Account updatedAccount = accountRepository.save(account);
        log.info("Deposit successful. New balance: {}", updatedAccount.getBalance());

        return convertToDto(updatedAccount);
    }

    @Transactional
    public AccountDto withdraw(String accountNumber, BigDecimal amount) {
        log.info("Withdrawing {} from account: {}", amount, accountNumber);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Withdrawal amount must be positive");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountClosedException("Cannot withdraw from a non-active account");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        Account updatedAccount = accountRepository.save(account);
        log.info("Withdrawal successful. New balance: {}", updatedAccount.getBalance());

        return convertToDto(updatedAccount);
    }

    // Helper methods
    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private AccountDto convertToDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUser().getId())
                .username(account.getUser().getUsername())
                .balance(account.getBalance())
                .accountType(account.getAccountType().toString())
                .status(account.getStatus().toString())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}