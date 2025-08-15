package com.example.BankSystem.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AccountClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccountClosedException(String message) {
        super(message);
    }
}
