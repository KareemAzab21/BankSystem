package com.example.BankSystem.service;

import com.example.BankSystem.dto.TransactionDto;
import com.example.BankSystem.dto.TransferRequest;
import com.example.BankSystem.dto.TransferResponse;
import com.example.BankSystem.exception.AccountClosedException;
import com.example.BankSystem.exception.BadRequestException;
import com.example.BankSystem.exception.InsufficientFundsException;
import com.example.BankSystem.exception.ResourceNotFoundException;
import com.example.BankSystem.model.Account;
import com.example.BankSystem.model.Transaction;
import com.example.BankSystem.enums.AccountStatus;
import com.example.BankSystem.enums.TransactionStatus;
import com.example.BankSystem.enums.TransactionType;
import com.example.BankSystem.repository.AccountRepository;
import com.example.BankSystem.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getAllTransactions() {
        log.info("Fetching all transactions");
        return transactionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long id) {
        log.info("Fetching transaction with ID: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return convertToDto(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionByTransactionId(String transactionId) {
        log.info("Fetching transaction with transaction ID: {}", transactionId);
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "transactionId", transactionId));
        return convertToDto(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByAccountNumber(String accountNumber) {
        log.info("Fetching transactions for account: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        return transactionRepository.findByAccount(account).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByAccountNumber(String accountNumber, Pageable pageable) {
        log.info("Fetching paginated transactions for account: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        return transactionRepository.findByAccount(account, pageable)
                .map(this::convertToDto);
    }

    @Transactional
    public TransferResponse transferFunds(TransferRequest request) {
        log.info("Initiating transfer from {} to {} for amount {}",
                request.getSourceAccountNumber(), request.getDestinationAccountNumber(), request.getAmount());

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be positive");
        }

        // Get accounts
        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", request.getSourceAccountNumber()));

        Account destinationAccount = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", request.getDestinationAccountNumber()));

        // Check if accounts are active
        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountClosedException("Source account is not active");
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountClosedException("Destination account is not active");
        }

        // Check sufficient funds
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in source account");
        }

        // Generate transaction ID
        String transactionId = UUID.randomUUID().toString();

        try {
            // Update account balances
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
            destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            // Create transaction record
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .sourceAccount(sourceAccount)
                    .destinationAccount(destinationAccount)
                    .amount(request.getAmount())
                    .type(TransactionType.TRANSFER)
                    .status(TransactionStatus.COMPLETED)
                    .description(request.getDescription())
                    .timestamp(LocalDateTime.now())
                    .build();

            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Transfer completed successfully with transaction ID: {}", savedTransaction.getTransactionId());

            return TransferResponse.builder()
                    .transactionId(savedTransaction.getTransactionId())
                    .sourceAccountNumber(sourceAccount.getAccountNumber())
                    .destinationAccountNumber(destinationAccount.getAccountNumber())
                    .amount(request.getAmount())
                    .timestamp(savedTransaction.getTimestamp())
                    .status(savedTransaction.getStatus().toString())
                    .message("Transfer completed successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error during transfer: {}", e.getMessage(), e);
            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }
    }

    // Helper methods
    private TransactionDto convertToDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .sourceAccountNumber(transaction.getSourceAccount() != null ?
                        transaction.getSourceAccount().getAccountNumber() : null)
                .destinationAccountNumber(transaction.getDestinationAccount() != null ?
                        transaction.getDestinationAccount().getAccountNumber() : null)
                .amount(transaction.getAmount())
                .type(transaction.getType().toString())
                .status(transaction.getStatus().toString())
                .description(transaction.getDescription())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
