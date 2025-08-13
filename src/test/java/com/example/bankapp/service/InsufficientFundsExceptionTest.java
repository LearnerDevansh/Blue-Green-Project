package com.example.bankapp.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsufficientFundsExceptionTest {

    @Test
    void testConstructor_SetsMessageCorrectly() {
        String message = "Insufficient funds for this transaction!";
        InsufficientFundsException ex = new InsufficientFundsException(message);
        assertEquals(message, ex.getMessage());
    }
}
