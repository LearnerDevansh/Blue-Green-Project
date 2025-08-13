package com.example.bankapp.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsernameAlreadyExistsExceptionTest {

    @Test
    void testConstructor_SetsMessageCorrectly() {
        String message = "Username already exists!";
        UsernameAlreadyExistsException exception = new UsernameAlreadyExistsException(message);

        // Verify that the message stored in the exception is correct
        assertEquals(message, exception.getMessage());
    }
}
