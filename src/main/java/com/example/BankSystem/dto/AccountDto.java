package com.example.BankSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long id;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String username; // For display purposes

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be positive or zero")
    private BigDecimal balance;

    @NotBlank(message = "Account type is required")
    private String accountType;

    @NotBlank(message = "Status is required")
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}