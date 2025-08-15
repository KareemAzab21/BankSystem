package com.example.BankSystem.controller;

import com.example.BankSystem.dto.TransactionDto;
import com.example.BankSystem.dto.TransferRequest;
import com.example.BankSystem.dto.TransferResponse;
import com.example.BankSystem.service.TransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        log.info("Request to get all transactions");
        List<TransactionDto> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @transactionSecurity.canAccessTransaction(#id)")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        log.info("Request to get transaction with ID: {}", id);
        TransactionDto transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transaction-id/{transactionId}")
    @PreAuthorize("hasRole('ADMIN') or @transactionSecurity.canAccessTransactionByTransactionId(#transactionId)")
    public ResponseEntity<TransactionDto> getTransactionByTransactionId(@PathVariable String transactionId) {
        log.info("Request to get transaction with transaction ID: {}", transactionId);
        TransactionDto transaction = transactionService.getTransactionByTransactionId(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/account/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.canAccessAccountByNumber(#accountNumber)")
    public ResponseEntity<List<TransactionDto>> getTransactionsByAccountNumber(@PathVariable String accountNumber) {
        log.info("Request to get transactions for account: {}", accountNumber);
        List<TransactionDto> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountNumber}/paged")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.canAccessAccountByNumber(#accountNumber)")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByAccountNumberPaged(
            @PathVariable String accountNumber,
            Pageable pageable) {
        log.info("Request to get paginated transactions for account: {}", accountNumber);
        Page<TransactionDto> transactions = transactionService.getTransactionsByAccountNumber(accountNumber, pageable);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or @accountSecurity.canAccessAccountByNumber(#transferRequest.sourceAccountNumber)")
    public ResponseEntity<TransferResponse> transferFunds(@Valid @RequestBody TransferRequest transferRequest) {
        log.info("Request to transfer {} from {} to {}",
                transferRequest.getAmount(),
                transferRequest.getSourceAccountNumber(),
                transferRequest.getDestinationAccountNumber());

        TransferResponse response = transactionService.transferFunds(transferRequest);
        return ResponseEntity.ok(response);
    }
}
