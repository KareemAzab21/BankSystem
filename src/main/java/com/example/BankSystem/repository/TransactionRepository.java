package com.example.BankSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.BankSystem.model.Transaction;
import com.example.BankSystem.model.Account;
import com.example.BankSystem.enums.TransactionStatus;
import com.example.BankSystem.enums.TransactionType;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;


public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    /**
     * Find a transaction by its unique transaction ID
     */
    Optional<Transaction> findByTransactionId(String transactionId);

    /**
     * Find all transactions where the specified account is the source
     */
    List<Transaction> findBySourceAccount(Account account);

    /**
     * Find all transactions where the specified account is the destination
     */
    List<Transaction> findByDestinationAccount(Account account);

    /**
     * Find all transactions involving the specified account (as source or destination)
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount = ?1 OR t.destinationAccount = ?1")
    List<Transaction> findByAccount(Account account);

    /**
     * Find all transactions involving the specified account (as source or destination) with pagination
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount = ?1 OR t.destinationAccount = ?1")
    Page<Transaction> findByAccount(Account account, Pageable pageable);

    /**
     * Find all transactions that occurred between the specified dates
     */
    List<Transaction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all transactions of a specific type
     */
    List<Transaction> findByType(TransactionType type);

    /**
     * Find all transactions with a specific status
     */
    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * Find all transactions involving an account with a specific type and status
     */
    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount = ?1 OR t.destinationAccount = ?1) AND t.type = ?2 AND t.status = ?3")
    List<Transaction> findByAccountAndTypeAndStatus(Account account, TransactionType type, TransactionStatus status);
}
